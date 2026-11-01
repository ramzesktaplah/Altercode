import { describe, expect, it } from "bun:test";
import worker from "./index";

const mockEnv = {
  GROQ_API_KEY: "test-groq-key",
  GOOGLE_AI_KEY: "test-google-key",
  DO: {
    fetch: async () => new Response(JSON.stringify({ ok: true }), { status: 200 }),
  },
};

describe("AlterCode AI Proxy Security Controls", () => {
  it("rejects cross-origin browser requests with 403 Forbidden", async () => {
    const req = new Request("https://worker.rork.app/v1/chat", {
      method: "POST",
      headers: {
        "Origin": "https://malicious-website.com",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "console.log(1);" }],
      }),
    });

    const res = await worker.fetch(req, mockEnv);
    expect(res.status).toBe(403);
    const body = await res.json();
    expect(body.error).toBe("Forbidden");
  });

  it("rejects malformed message items with 400 Bad Request instead of crashing", async () => {
    const testCases = [
      { action: "explain", messages: [null] },
      { action: "explain", messages: [{ role: "user" }] },
      { action: "explain", messages: [{ role: "user", content: 12345 }] },
      { action: "explain", messages: "not-an-array" },
      { action: "explain", messages: [] },
      { action: "invalid_action", messages: [{ role: "user", content: "test" }] },
    ];

    for (const testCase of testCases) {
      const req = new Request("https://worker.rork.app/v1/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Dry-Run": "altercode-loadtest",
        },
        body: JSON.stringify(testCase),
      });

      const res = await worker.fetch(req, mockEnv);
      expect(res.status).toBe(400);
      const json = await res.json();
      expect(json.error).toBeDefined();
    }
  });

  it("permits legitimate native client requests without Origin header", async () => {
    const req = new Request("https://worker.rork.app/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Device-Id": "device-uuid-test",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [
          { role: "system", content: "Scaffolding" },
          { role: "user", content: "fun main() {}" },
        ],
        temperature: 0.2,
        maxTokens: 100,
      }),
    });

    const res = await worker.fetch(req, mockEnv);
    expect(res.status).toBe(200);
    const json = await res.json();
    expect(json.choices[0].message.content).toBe("dry-run");
  });
});
