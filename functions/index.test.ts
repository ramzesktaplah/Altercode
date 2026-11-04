import { describe, expect, it } from "bun:test";
import worker from "./index";

describe("Cloudflare Worker Security & Validation", () => {
  const dummyEnv = {
    GROQ_API_KEY: "test-groq-key",
    GOOGLE_AI_KEY: "test-google-key",
    DO: {
      fetch: async (_url: string, init?: RequestInit) => {
        const id = (init?.headers as Record<string, string>)?.[
          "X-Rork-DO-Id"
        ];
        return Response.json({ ok: true, rateLimitKey: id });
      },
    } as unknown as Fetcher,
  };

  it("rejects non-string message content with 400", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "convert",
        messages: [{ role: "user", content: 12345 }],
      }),
    });

    const res = await worker.fetch(req, dummyEnv);
    expect(res.status).toBe(400);
    const json = await res.json();
    expect(json.error).toBe("Invalid message roles or count");
  });

  it("rejects disallowed actions with 400", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "delete-database",
        messages: [{ role: "user", content: "hello" }],
      }),
    });

    const res = await worker.fetch(req, dummyEnv);
    expect(res.status).toBe(400);
    const json = await res.json();
    expect(json.error).toBe("Unknown action 'delete-database'");
  });

  it("sanitizes malicious or malformed X-Device-Id and client IP headers in rate limiter key", async () => {
    let capturedDoId = "";
    const envWithSpy = {
      ...dummyEnv,
      DO: {
        fetch: async (_url: string, init?: RequestInit) => {
          capturedDoId =
            (init?.headers as Record<string, string>)?.[
              "X-Rork-DO-Id"
            ] ?? "";
          return Response.json({ ok: true });
        },
      } as unknown as Fetcher,
    };

    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Device-Id": "invalid!@#$device:::key",
        "CF-Connecting-IP": "malicious-ip;injection",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "const x = 1;" }],
      }),
    });

    const res = await worker.fetch(req, envWithSpy);
    expect(res.status).toBe(200);
    expect(capturedDoId).toBe("unknown:unknown");
  });

  it("preserves valid X-Device-Id and client IP in rate limiter key", async () => {
    let capturedDoId = "";
    const envWithSpy = {
      ...dummyEnv,
      DO: {
        fetch: async (_url: string, init?: RequestInit) => {
          capturedDoId =
            (init?.headers as Record<string, string>)?.[
              "X-Rork-DO-Id"
            ] ?? "";
          return Response.json({ ok: true });
        },
      } as unknown as Fetcher,
    };

    const validUuid = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Device-Id": validUuid,
        "CF-Connecting-IP": "192.0.2.1",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "const x = 1;" }],
      }),
    });

    const res = await worker.fetch(req, envWithSpy);
    expect(res.status).toBe(200);
    expect(capturedDoId).toBe(`192.0.2.1:${validUuid}`);
  });
});
