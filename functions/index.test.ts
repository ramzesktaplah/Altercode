import { describe, expect, it } from "bun:test";
import worker from "./index";

describe("AlterCode AI proxy Worker - Security & Rate Limit Validation", () => {
  it("rejects invalid JSON body", async () => {
    const req = new Request("https://example.com/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: "not-json",
    });
    const env = { GROQ_API_KEY: "fake", GOOGLE_AI_KEY: "fake", DO: { fetch: async () => new Response() } };
    const res = await worker.fetch(req, env as any);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid JSON body");
  });

  it("sanitizes malicious or oversized X-Device-Id in rate limit key", async () => {
    let capturedDoId = "";
    const mockDO = {
      fetch: async (url: string, init?: RequestInit) => {
        const headers = init?.headers as Record<string, string>;
        capturedDoId = headers?.["X-Rork-DO-Id"] ?? "";
        return new Response(JSON.stringify({ ok: true }), { status: 200 });
      },
    };
    const env = {
      GROQ_API_KEY: "fake",
      GOOGLE_AI_KEY: "fake",
      DO: mockDO as any,
    };

    // Case 1: Oversized device ID or injected delimiter characters
    const maliciousReq = new Request("https://example.com/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "CF-Connecting-IP": "192.168.1.1",
        "X-Device-Id": "bad:id:with:colons:and?params=" + "a".repeat(100),
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "const a = 1;" }],
        temperature: 0.2,
        maxTokens: 100,
      }),
    });

    const res = await worker.fetch(maliciousReq, env);
    expect(res.status).toBe(200);
    expect(capturedDoId).toBe("192.168.1.1:unknown");

    // Case 2: Valid UUID device ID
    const validUuid = "c9a646d3-9c61-4079-8d16-cb7d32d83bbf";
    const validReq = new Request("https://example.com/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "CF-Connecting-IP": "10.0.0.1",
        "X-Device-Id": validUuid,
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "const a = 1;" }],
        temperature: 0.2,
        maxTokens: 100,
      }),
    });

    const res2 = await worker.fetch(validReq, env);
    expect(res2.status).toBe(200);
    expect(capturedDoId).toBe(`10.0.0.1:${validUuid}`);

    // Case 3: Missing device ID
    const missingReq = new Request("https://example.com/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "CF-Connecting-IP": "10.0.0.2",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "const a = 1;" }],
        temperature: 0.2,
        maxTokens: 100,
      }),
    });

    const res3 = await worker.fetch(missingReq, env);
    expect(res3.status).toBe(200);
    expect(capturedDoId).toBe("10.0.0.2:unknown");
  });
});
