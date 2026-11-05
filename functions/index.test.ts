import { describe, expect, it } from "bun:test";
import worker from "./index";

const mockEnv = {
  GROQ_API_KEY: "test-groq-key",
  GOOGLE_AI_KEY: "test-google-key",
  DO: {
    fetch: async () => new Response(JSON.stringify({ ok: true }), { status: 200 }),
  },
};

describe("AlterCode Proxy Security", () => {
  it("includes security headers on /ping responses", async () => {
    const request = new Request("http://localhost/ping");
    const response = await worker.fetch(request, mockEnv as any);

    expect(response.status).toBe(200);
    expect(response.headers.get("X-Content-Type-Options")).toBe("nosniff");
    expect(response.headers.get("X-Frame-Options")).toBe("DENY");
    expect(response.headers.get("Referrer-Policy")).toBe("no-referrer");
    expect(response.headers.get("Strict-Transport-Security")).toContain("max-age=");
  });

  it("includes security headers on 404 responses", async () => {
    const request = new Request("http://localhost/non-existent");
    const response = await worker.fetch(request, mockEnv as any);

    expect(response.status).toBe(404);
    expect(response.headers.get("X-Content-Type-Options")).toBe("nosniff");
    expect(response.headers.get("X-Frame-Options")).toBe("DENY");
    expect(response.headers.get("Referrer-Policy")).toBe("no-referrer");
  });

  it("includes security headers on invalid JSON input", async () => {
    const request = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: "invalid-json{",
    });
    const response = await worker.fetch(request, mockEnv as any);

    expect(response.status).toBe(400);
    expect(response.headers.get("X-Content-Type-Options")).toBe("nosniff");
    expect(response.headers.get("X-Frame-Options")).toBe("DENY");
  });

  it("rejects messages with non-string content or disallowed roles", async () => {
    const request = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        action: "fix",
        messages: [{ role: "admin", content: "foo" }],
      }),
    });
    const response = await worker.fetch(request, mockEnv as any);

    expect(response.status).toBe(400);
    const body = await response.json();
    expect(body.error).toContain("Invalid message roles");
  });

  it("sanitizes null bytes from user input when processing load-test dry-run", async () => {
    const request = new Request("http://localhost/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Dry-Run": "altercode-loadtest",
      },
      body: JSON.stringify({
        action: "fix",
        messages: [{ role: "user", content: "test\u0000code" }],
      }),
    });
    const response = await worker.fetch(request, mockEnv as any);

    expect(response.status).toBe(200);
    expect(response.headers.get("X-Content-Type-Options")).toBe("nosniff");
    expect(response.headers.get("X-Frame-Options")).toBe("DENY");
  });
});
