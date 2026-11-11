import { describe, expect, test } from "bun:test";
import worker from "./index";

describe("AlterCode AI proxy Worker input validation and sanitization", () => {
  const mockEnv = {
    GROQ_API_KEY: "test-groq-key",
    GOOGLE_AI_KEY: "test-google-key",
    DO: {
      async fetch(_url: string, init?: RequestInit) {
        const id = (init?.headers as Record<string, string>)?.["X-Rork-DO-Id"];
        return new Response(JSON.stringify({ ok: true, id }), { status: 200 });
      },
    } as any,
  };

  test("rejects malformed message shapes (non-string content)", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "fix",
        messages: [{ role: "user", content: 12345 }],
      }),
    });

    const res = await worker.fetch(req, mockEnv);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid message roles or count");
  });

  test("rejects null or missing message content", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "fix",
        messages: [{ role: "user" }],
      }),
    });

    const res = await worker.fetch(req, mockEnv);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid message roles or count");
  });

  test("rejects invalid action", async () => {
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "malicious_action",
        messages: [{ role: "user", content: "test" }],
      }),
    });

    const res = await worker.fetch(req, mockEnv);
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toContain("Unknown action");
  });

  test("sanitizes invalid or oversized X-Device-Id in dry-run request", async () => {
    let capturedDoId = "";
    const testEnv = {
      ...mockEnv,
      DO: {
        async fetch(_url: string, init?: RequestInit) {
          capturedDoId = (init?.headers as Record<string, string>)?.["X-Rork-DO-Id"] || "";
          return new Response(JSON.stringify({ ok: true }), { status: 200 });
        },
      } as any,
    };

    // Device ID with special characters or excessive length
    const req = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Device-Id": "bad/id:with$special@characters",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "code snippet" }],
      }),
    });

    const res = await worker.fetch(req, testEnv);
    expect(res.status).toBe(200);
    expect(capturedDoId.endsWith(":unknown")).toBe(true);

    // Valid UUID device ID
    const validUuid = "123e4567-e89b-12d3-a456-426614174000";
    const reqValid = new Request("https://worker.local/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Device-Id": validUuid,
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "explain",
        messages: [{ role: "user", content: "code snippet" }],
      }),
    });

    const resValid = await worker.fetch(reqValid, testEnv);
    expect(resValid.status).toBe(200);
    expect(capturedDoId.endsWith(`:${validUuid}`)).toBe(true);
  });
});
