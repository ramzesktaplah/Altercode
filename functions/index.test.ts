import { describe, expect, it } from "bun:test";
import worker from "./index";

describe("Worker handleChat rate limit key and input sanitization", () => {
  it("sanitizes invalid X-Device-Id and IP when rate limiting", async () => {
    let capturedDoId = "";
    const mockEnv = {
      GROQ_API_KEY: "test-groq-key",
      GOOGLE_AI_KEY: "test-google-key",
      DO: {
        fetch: async (url: string | URL | Request, init?: RequestInit) => {
          capturedDoId = (init?.headers as Record<string, string>)["X-Rork-DO-Id"];
          return new Response(JSON.stringify({ ok: true }), { status: 200 });
        },
      } as unknown as Fetcher,
    };

    // Test with non-whitelisted characters or overly long string in X-Device-Id and IP
    const req = new Request("https://example.com/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "CF-Connecting-IP": "192.168.1.1; DROP TABLE",
        "X-Device-Id": "device_with_forbidden_chars!@#$and_too_long_".repeat(3),
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "fix",
        messages: [{ role: "user", content: "hello" }],
        temperature: 0.2,
        maxTokens: 100,
      }),
    });

    const res = await worker.fetch(req, mockEnv);
    expect(res.status).toBe(200);
    // Should fall back to "unknown:unknown" instead of arbitrary dirty keys
    expect(capturedDoId).toBe("unknown:unknown");
  });

  it("preserves valid UUID deviceId and standard IP", async () => {
    let capturedDoId = "";
    const mockEnv = {
      GROQ_API_KEY: "test-groq-key",
      GOOGLE_AI_KEY: "test-google-key",
      DO: {
        fetch: async (url: string | URL | Request, init?: RequestInit) => {
          capturedDoId = (init?.headers as Record<string, string>)["X-Rork-DO-Id"];
          return new Response(JSON.stringify({ ok: true }), { status: 200 });
        },
      } as unknown as Fetcher,
    };

    const validDeviceId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    const req = new Request("https://example.com/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "CF-Connecting-IP": "203.0.113.195",
        "X-Device-Id": validDeviceId,
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "fix",
        messages: [{ role: "user", content: "hello" }],
        temperature: 0.2,
        maxTokens: 100,
      }),
    });

    const res = await worker.fetch(req, mockEnv);
    expect(res.status).toBe(200);
    expect(capturedDoId).toBe(`203.0.113.195:${validDeviceId}`);
  });
});
