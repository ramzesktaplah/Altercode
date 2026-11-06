## 2026-11-02 - Unvalidated Rate-Limiting Keys Causing DO Exhaustion
**Vulnerability:** The Cloudflare Worker constructed Durable Object instance keys (`rateLimitKey`) directly from client-supplied headers `X-Device-Id` and `CF-Connecting-IP` / `X-Forwarded-For` without sanitizing character sets or restricting string length.
**Learning:** When using Durable Objects dynamically keyed by request headers, unbounded or unsanitized client inputs allow attackers to trigger unbounded Durable Object instance allocations and pollute rate-limiting state.
**Prevention:** Always strictly validate and sanitize rate-limiting identifier headers with regex allowlists and length limits, falling back to a safe constant (e.g. `unknown`) if input does not match.
