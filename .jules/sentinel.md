# Sentinel Security Journal

## 2026-03-09 - Cloudflare Worker Input Validation & Durable Object ID Sanitization
**Vulnerability:** The AI proxy worker parsed `messages` from the JSON body without strictly verifying that `messages` was a non-empty array and that every message element had string `role` and string `content`. Additionally, the `X-Device-Id` header supplied by callers was directly concatenated into the Durable Object rate-limiting ID without sanitization or length capping, risking DO key injection and memory bloat.
**Learning:** In Cloudflare Workers interfacing with Durable Objects and external LLM APIs, unvalidated client headers used as Durable Object keys or JSON fields passed downstream can cause upstream API failures, corrupted SQLite states, or quota abuse.
**Prevention:** Strictly validate input types (`typeof`, `Array.isArray`) and sanitize any client-controlled headers used in DO IDs by stripping non-alphanumeric/hyphen/underscore characters and capping maximum length before forwarding to internal dispatch.
