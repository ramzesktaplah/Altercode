import { describe, expect, it } from "bun:test";
import worker from "./index";

const mockEnv = {
  GROQ_API_KEY: "mock-groq-key",
  GOOGLE_AI_KEY: "mock-gemini-key",
  DO: {
    fetch: async () => new Response(JSON.stringify({ ok: true }), { status: 200 }),
  } as unknown as Fetcher,
};

describe("Cloudflare Worker Security & Validation", () => {
  it("rejects non-GET methods on /ping", async () => {
    const res = await worker.fetch(new Request("https://worker/ping", { method: "POST" }), mockEnv);
    expect(res.status).toBe(405);
  });

  it("accepts GET /ping", async () => {
    const res = await worker.fetch(new Request("https://worker/ping", { method: "GET" }), mockEnv);
    expect(res.status).toBe(200);
    const data = await res.json() as { ok: boolean };
    expect(data.ok).toBe(true);
  });

  it("rejects invalid JSON body on /v1/chat", async () => {
    const res = await worker.fetch(
      new Request("https://worker/v1/chat", {
        method: "POST",
        body: "invalid-json",
        headers: { "Content-Type": "application/json" },
      }),
      mockEnv
    );
    expect(res.status).toBe(400);
    const data = await res.json() as { error: string };
    expect(data.error).toBe("Invalid JSON body");
  });

  it("rejects unknown action", async () => {
    const res = await worker.fetch(
      new Request("https://worker/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "malicious_action",
          messages: [{ role: "user", content: "hello" }],
        }),
      }),
      mockEnv
    );
    expect(res.status).toBe(400);
    const data = await res.json() as { error: string };
    expect(data.error).toBe("Unknown action 'malicious_action'");
  });

  it("rejects messages with non-string content or roles", async () => {
    const res = await worker.fetch(
      new Request("https://worker/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "explain",
          messages: [{ role: "user", content: 12345 }],
        }),
      }),
      mockEnv
    );
    expect(res.status).toBe(400);
  });

  it("rejects messages that is not an array or has > 2 messages", async () => {
    const res = await worker.fetch(
      new Request("https://worker/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "explain",
          messages: "not-an-array",
        }),
      }),
      mockEnv
    );
    expect(res.status).toBe(400);

    const res2 = await worker.fetch(
      new Request("https://worker/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          action: "explain",
          messages: [
            { role: "system", content: "a" },
            { role: "user", content: "b" },
            { role: "user", content: "c" },
          ],
        }),
      }),
      mockEnv
    );
    expect(res2.status).toBe(400);
  });

  it("sanitizes X-Device-Id when dispatching to rate limiter", async () => {
    let capturedDoId = "";
    const customEnv = {
      ...mockEnv,
      DO: {
        fetch: async (_url: string, init?: RequestInit) => {
          capturedDoId = (init?.headers as Record<string, string>)?.["X-Rork-DO-Id"] ?? "";
          return new Response(JSON.stringify({ ok: true }), { status: 200 });
        },
      } as unknown as Fetcher,
    };

    await worker.fetch(
      new Request("https://worker/v1/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Device-Id": "device-uuid-1234!!@@##$$bad chars",
          "X-Dry-Run": "altercode-loadtest",
        },
        body: JSON.stringify({
          action: "explain",
          messages: [{ role: "user", content: "print('hello')" }],
        }),
      }),
      customEnv
    );

    // Only alphanumeric and hyphens/underscores, capped at 64 chars
    expect(capturedDoId).toMatch(/^[^:]+:[a-zA-Z0-9_-]+$/);
  });
});
