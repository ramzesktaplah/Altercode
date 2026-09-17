# Sentinel Security Journal

## 2026-03-24 - Unsanitized Rate Limit Headers in Durable Object Routing Key
**Vulnerability:** HTTP request headers (`X-Device-Id` and client IP fallbacks) were formatted directly into Cloudflare Durable Object dispatch keys (`${clientIp}:${deviceId}`) without input sanitization or length limits. An attacker sending malformed, control-character-laden, or excessively long header strings could inject additional key delimiters, manipulate key identities, or cause unexpected routing and storage key behavior.
**Learning:** External HTTP headers supplied by unauthenticated clients must never be trusted as raw inputs when constructing internal key identifiers or dispatch headers (e.g., `X-Rork-DO-Id`).
**Prevention:** Sanitize header inputs using whitelist regex patterns (`[a-zA-Z0-9_.-]`), enforce strict length caps (e.g., 64 characters max), and fallback to safe default strings before concatenating into composite rate-limiting or storage keys.
