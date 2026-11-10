import { describe, expect, it } from "bun:test";
import worker from "./index";

const mockEnv = {
  GROQ_API_KEY: "mock-groq-key",
  GOOGLE_AI_KEY: "mock-google-key",
  DO: {
    fetch: async () => new Response(JSON.stringify({ ok: true }), { status: 200 }),
  },
};

async function sendRequest(body: unknown, headers: Record<string, string> = {}): Promise<Response> {
  const req = new Request("http://localhost/v1/chat", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Device-Id": "test-device",
      ...headers,
    },
    body: typeof body === "string" ? body : JSON.stringify(body),
  });
  return worker.fetch(req, mockEnv as any);
}

describe("functions/index Worker Chat Validation", () => {
  it("rejects non-JSON payload with 400", async () => {
    const res = await sendRequest("not-json");
    expect(res.status).toBe(400);
    const data = await res.json();
    expect(data.error).toBe("Invalid JSON body");
  });

  it("rejects missing action or empty messages with 400", async () => {
    const resNoAction = await sendRequest({
      messages: [{ role: "user", content: "hello" }],
    });
    expect(resNoAction.status).toBe(400);
    expect((await resNoAction.json()).error).toBe("Missing 'action' or 'messages'");

    const resEmptyMessages = await sendRequest({
      action: "fix",
      messages: [],
    });
    expect(resEmptyMessages.status).toBe(400);
    expect((await resEmptyMessages.json()).error).toBe("Missing 'action' or 'messages'");
  });

  it("rejects disallowed action with 400", async () => {
    const res = await sendRequest({
      action: "unauthorized_action",
      messages: [{ role: "user", content: "hello" }],
    });
    expect(res.status).toBe(400);
    expect((await res.json()).error).toBe("Unknown action 'unauthorized_action'");
  });

  it("rejects message count > 2 with 400", async () => {
    const res = await sendRequest({
      action: "explain",
      messages: [
        { role: "system", content: "sys" },
        { role: "user", content: "usr1" },
        { role: "user", content: "usr2" },
      ],
    });
    expect(res.status).toBe(400);
    expect((await res.json()).error).toBe("Invalid message roles, count, or content");
  });

  it("rejects invalid roles with 400", async () => {
    const res = await sendRequest({
      action: "fix",
      messages: [{ role: "developer", content: "malicious prompt" }],
    });
    expect(res.status).toBe(400);
    expect((await res.json()).error).toBe("Invalid message roles, count, or content");
  });

  it("rejects empty or whitespace-only message content with 400", async () => {
    const resEmpty = await sendRequest({
      action: "fix",
      messages: [{ role: "user", content: "" }],
    });
    expect(resEmpty.status).toBe(400);
    expect((await resEmpty.json()).error).toBe("Invalid message roles, count, or content");

    const resWhitespace = await sendRequest({
      action: "fix",
      messages: [{ role: "user", content: "   \n\t  " }],
    });
    expect(resWhitespace.status).toBe(400);
    expect((await resWhitespace.json()).error).toBe("Invalid message roles, count, or content");
  });

  it("rejects non-string message content with 400", async () => {
    const resNumber = await sendRequest({
      action: "fix",
      messages: [{ role: "user", content: 12345 }],
    });
    expect(resNumber.status).toBe(400);
    expect((await resNumber.json()).error).toBe("Invalid message roles, count, or content");

    const resObject = await sendRequest({
      action: "fix",
      messages: [{ role: "user", content: { text: "exploit" } }],
    });
    expect(resObject.status).toBe(400);
    expect((await resObject.json()).error).toBe("Invalid message roles, count, or content");
  });

  it("rejects oversized payloads (>30k characters) with 413", async () => {
    const res = await sendRequest({
      action: "fix",
      messages: [{ role: "user", content: "a".repeat(30_001) }],
    });
    expect(res.status).toBe(413);
    expect((await res.json()).error).toBe("Request too large");
  });

  it("accepts valid messages with dry-run header", async () => {
    const res = await sendRequest(
      {
        action: "fix",
        messages: [
          { role: "system", content: "Scaffolding" },
          { role: "user", content: "val x = 1" },
        ],
      },
      { "X-Dry-Run": "altercode-loadtest" },
    );
    expect(res.status).toBe(200);
    const data = await res.json();
    expect(data.choices[0].message.content).toBe("dry-run");
  });
});
