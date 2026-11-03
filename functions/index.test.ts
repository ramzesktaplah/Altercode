import { describe, expect, it } from "bun:test";
import worker from "./index";

const mockEnv = {
  GROQ_API_KEY: "test-groq-key",
  GOOGLE_AI_KEY: "test-gemini-key",
  DO: {
    fetch: async () => new Response(JSON.stringify({ ok: true }), { status: 200 }),
  },
};

describe("AlterCode Worker proxy input validation", () => {
  it("rejects non-array messages payload with 400 Bad Request instead of crashing", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "fix",
        messages: { length: 1 },
      }),
    });

    const res = await worker.fetch(req, mockEnv as any);
    expect(res.status).toBe(400);
    const json = await res.json();
    expect(json.error).toBeDefined();
  });

  it("rejects messages with non-string content with 400 Bad Request", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "fix",
        messages: [{ role: "user", content: 12345 }],
      }),
    });

    const res = await worker.fetch(req, mockEnv as any);
    expect(res.status).toBe(400);
  });

  it("accepts valid messages shape with dry-run header", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "const a = 1;" }],
      }),
    });

    const res = await worker.fetch(req, mockEnv as any);
    expect(res.status).toBe(200);
    const json = await res.json();
    expect(json.choices[0].message.content).toBe("dry-run");
  });
});
