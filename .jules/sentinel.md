# Sentinel Security Journal

## 2026-11-02 - Durable Object Rate-Limiter Key and Message Content Validation
**Vulnerability:** Unsanitized `X-Device-Id` headers were incorporated directly into Durable Object dispatch headers and rate-limiting keys (`${clientIp}:${deviceId}`), allowing attackers to provide arbitrary strings or excessively long keys. Additionally, message shapes checked roles but did not enforce `typeof m.content === "string"`.
**Learning:** Upstream Cloudflare Worker proxy endpoints should enforce strict whitelist regex checks on custom headers forwarded to internal bindings (like DO fetchers) to prevent key space pollution and header injection attacks.
**Prevention:** Whitelist device ID formats using `/^[a-zA-Z0-9_-]{1,64}$/` and validate string types on incoming chat message payloads before forwarding.
