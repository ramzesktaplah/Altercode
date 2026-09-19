// functions/index.ts — AlterCode AI proxy Worker.
//
// Routes AI requests to different providers based on the action:
//   - FIX / EXPLAIN → Groq (fast inference, good for analysis)
//   - CONVERT / REFACTOR → Google Gemini (strong code generation)
//
// API keys are stored as private server env vars and never exposed to the client.
//
// Security notes:
// - No CORS headers are emitted: browsers cannot call this proxy, so a
//   malicious webpage can't burn the Groq/Gemini quota. The Android app is a
//   native client and is unaffected by CORS.
// - Only `system`/`user`/`assistant` message roles are forwarded (max 2
//   messages), so callers can't inject arbitrary developer/tool instructions.
// - Upstream provider error bodies are never relayed to clients.

// Re-export the RateLimiter Durable Object class so the platform
// registers it for dispatch via env.DO.
export { RateLimiter } from "./rate-limiter";

/** Actions that are allowed through the proxy. */
const VALID_ACTIONS = new Set(["convert", "refactor", "fix", "explain"]);

/** Actions that use Groq (Fix Bugs, Explain Code). */
const GROQ_ACTIONS = new Set(["fix", "explain"]);

/** Groq model for code analysis tasks (Fix Bugs, Explain).
 *  Previous: meta-llama/llama-4-maverick-17b-128e-instruct — deprecated March 9, 2026.
 *  Recommended migration: openai/gpt-oss-120b. */
const GROQ_MODEL = "openai/gpt-oss-120b";

/** Google Gemini model for code generation tasks. */
const GEMINI_MODEL = "gemini-3.6-flash";

/** Maximum total characters across all messages. */
const MAX_CONTENT_CHARS = 30_000;

/** Maximum tokens the client can request. */
const MAX_MAX_TOKENS = 8_000;

type Env = {
  GROQ_API_KEY: string;
  GOOGLE_AI_KEY: string;
  /** Rork-managed Durable Object binding for dispatching to DO classes. */
  DO: Fetcher;
};

interface ChatMessage {
  role: string;
  content: string;
}

interface ClientRequest {
  action: string;
  messages: ChatMessage[];
  temperature: number;
  maxTokens: number;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    if (url.pathname === "/ping") {
      return Response.json({ ok: true, now: new Date().toISOString() });
    }

    if (url.pathname === "/v1/chat" && request.method === "POST") {
      return handleChat(request, env);
    }

    return new Response("not found", { status: 404 });
  },
};

async function handleChat(request: Request, env: Env): Promise<Response> {
  let body: ClientRequest;
  try {
    body = (await request.json()) as ClientRequest;
  } catch {
    return Response.json(
      { error: "Invalid JSON body" },
      { status: 400 },
    );
  }

  const { action, messages, temperature, maxTokens } = body;

  if (!action || !Array.isArray(messages) || messages.length === 0) {
    return Response.json(
      { error: "Missing 'action' or 'messages'" },
      { status: 400 },
    );
  }

  // Validate action against allow-list.
  if (!VALID_ACTIONS.has(action)) {
    return Response.json(
      { error: `Unknown action '${action}'` },
      { status: 400 },
    );
  }

  // Strictly check message shape: max 2 messages, allowed roles, and string content.
  // Prevents smuggling extra developer/tool instructions or non-string inputs.
  const isAllowedShape =
    messages.length <= 2 &&
    messages.every(
      (m) =>
        m &&
        typeof m.content === "string" &&
        (m.role === "user" || m.role === "assistant" || m.role === "system"),
    );
  if (!isAllowedShape) {
    return Response.json(
      { error: "Invalid message roles or count" },
      { status: 400 },
    );
  }

  // ---- Server-side rate limiting ----
  // Key by client IP + device fingerprint so each device+IP combo
  // gets its own independent counter. Cloudflare sets CF-Connecting-IP
  // automatically; fall back to X-Forwarded-For if absent.
  const clientIp =
    request.headers.get("CF-Connecting-IP") ??
    request.headers.get("X-Forwarded-For")?.split(",")[0]?.trim() ??
    "unknown";
  const deviceId = request.headers.get("X-Device-Id") ?? "unknown";
  const rateLimitKey = `${clientIp}:${deviceId}`;

  const rateLimitResponse = await env.DO.fetch(
    "https://do/check",
    {
      method: "POST",
      headers: {
        "X-Rork-DO-Class": "RateLimiter",
        "X-Rork-DO-Id": rateLimitKey,
      },
    },
  );

  if (rateLimitResponse.status === 429) {
    const retryAfter = rateLimitResponse.headers.get("Retry-After") ?? "60";
    return Response.json(
      { error: "Too many requests. Please try again later." },
      {
        status: 429,
        headers: {
          "Retry-After": retryAfter,
        },
      },
    );
  }

  // Cap total message content to prevent abuse.
  const totalChars = messages.reduce(
    (sum, msg) => sum + (msg.content?.length ?? 0),
    0,
  );
  if (totalChars > MAX_CONTENT_CHARS) {
    return Response.json(
      { error: "Request too large" },
      { status: 413 },
    );
  }

  // Clamp temperature and maxTokens to safe ranges safely handling 0 and NaN.
  const parsedTemp = Number(temperature);
  const safeTemp = Math.max(
    0,
    Math.min(2, Number.isFinite(parsedTemp) ? parsedTemp : 0.2),
  );

  const parsedMaxTokens = Number(maxTokens);
  const safeMaxTokens = Math.max(
    1,
    Math.min(
      MAX_MAX_TOKENS,
      Number.isFinite(parsedMaxTokens) ? Math.floor(parsedMaxTokens) : 4000,
    ),
  );

  const useGroq = GROQ_ACTIONS.has(action);

  // Pick provider and build the upstream request
  if (useGroq) {
    return callGroq(env.GROQ_API_KEY, messages, safeTemp, safeMaxTokens);
  }
  return callGemini(env.GOOGLE_AI_KEY, messages, safeTemp, safeMaxTokens);
}

/** Call Groq's OpenAI-compatible chat completions endpoint. */
async function callGroq(
  apiKey: string,
  messages: ChatMessage[],
  temperature: number,
  maxTokens: number,
): Promise<Response> {
  const upstream = await fetch(
    "https://api.groq.com/openai/v1/chat/completions",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${apiKey}`,
      },
      body: JSON.stringify({
        model: GROQ_MODEL,
        messages,
        temperature,
        max_tokens: maxTokens,
      }),
    },
  );

  return relayResponse(upstream);
}

/** Call Google Gemini's OpenAI-compatible chat completions endpoint. */
async function callGemini(
  apiKey: string,
  messages: ChatMessage[],
  temperature: number,
  maxTokens: number,
): Promise<Response> {
  const upstream = await fetch(
    "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${apiKey}`,
      },
      body: JSON.stringify({
        model: GEMINI_MODEL,
        messages,
        temperature,
        max_tokens: maxTokens,
      }),
    },
  );

  return relayResponse(upstream);
}

/** Relay the upstream response to the client without CORS headers.
 *  Error bodies from the provider are logged server-side only; clients
 *  receive a generic message plus the upstream status code. */
async function relayResponse(upstream: Response): Promise<Response> {
  if (!upstream.ok) {
    const detail = await upstream.text();
    console.warn(
      `Upstream AI provider returned ${upstream.status}: ${detail.slice(0, 500)}`,
    );
    return Response.json(
      { error: "AI provider request failed" },
      { status: upstream.status },
    );
  }
  return new Response(upstream.body, {
    status: upstream.status,
    headers: {
      "Content-Type": "application/json",
    },
  });
}
