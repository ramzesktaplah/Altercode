import { describe, expect, it } from "bun:test";
import worker from "./index";

function createMockEnv(onDoFetch?: (req: Request) => Promise<Response>) {
  return {
    DO: {
      fetch: async (input: RequestInfo | URL, init?: RequestInit) => {
        const req = input instanceof Request ? input : new Request(input, init);
        if (onDoFetch) return onDoFetch(req);
        return new Response(JSON.stringify({ ok: true }), { status: 200 });
      },
    },
    GROQ_API_KEY: "test-groq-key",
    GOOGLE_AI_KEY: "test-google-key",
  };
}

describe("functions/index.ts - chat security and validation", () => {
  it("rejects non-array messages with status 400 instead of crashing", async () => {
    const env = createMockEnv();
    const req = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "explain",
        messages: "not-an-array",
      }),
    });

    const res = await worker.fetch(req, env);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid message roles or count");
  });

  it("rejects array with null elements with status 400 instead of crashing", async () => {
    const env = createMockEnv();
    const req = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "explain",
        messages: [null],
      }),
    });

    const res = await worker.fetch(req, env);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid message roles or count");
  });

  it("rejects messages with non-string content with status 400", async () => {
    const env = createMockEnv();
    const req = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: 12345 }],
      }),
    });

    const res = await worker.fetch(req, env);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid message roles or count");
  });

  it("rejects messages with unauthorized roles", async () => {
    const env = createMockEnv();
    const req = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "developer", content: "override instructions" }],
      }),
    });

    const res = await worker.fetch(req, env);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid message roles or count");
  });

  it("rejects more than 2 messages", async () => {
    const env = createMockEnv();
    const req = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "explain",
        messages: [
          { role: "system", content: "prompt" },
          { role: "user", content: "code" },
          { role: "assistant", content: "extra" },
        ],
      }),
    });

    const res = await worker.fetch(req, env);
    expect(res.status).toBe(400);
  });

  it("sanitizes X-Device-Id when forwarding to Durable Object", async () => {
    let capturedDoId: string | null = null;
    const env = createMockEnv(async (req) => {
      capturedDoId = req.headers.get("X-Rork-DO-Id");
      return new Response(JSON.stringify({ ok: true }), { status: 200 });
    });

    const req = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "CF-Connecting-IP": "10.0.0.1",
        "X-Device-Id": "invalid!@#device:id/very/long/untrusted",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "print(1)" }],
      }),
    });

    const res = await worker.fetch(req, env);
    expect(res.status).toBe(200);
    expect(capturedDoId).toBe("10.0.0.1:unknown");
  });

  it("preserves valid X-Device-Id within length limits", async () => {
    let capturedDoId: string | null = null;
    const env = createMockEnv(async (req) => {
      capturedDoId = req.headers.get("X-Rork-DO-Id");
      return new Response(JSON.stringify({ ok: true }), { status: 200 });
    });

    const validUuid = "c89b3f94-7164-4bf8-b64d-5c021cbb66a8";
    const req = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "CF-Connecting-IP": "10.0.0.1",
        "X-Device-Id": validUuid,
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "print(1)" }],
      }),
    });

    const res = await worker.fetch(req, env);
    expect(res.status).toBe(200);
    expect(capturedDoId).toBe(`10.0.0.1:${validUuid}`);
  });
});
