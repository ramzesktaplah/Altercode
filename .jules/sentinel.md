# Sentinel Journal

## 2026-11-02 - Rate-Limiter Device ID Key Namespace Injection & Flooding
**Vulnerability:** The Cloudflare Worker proxy keyed the `RateLimiter` Durable Object as `${clientIp}:${deviceId}`, where `deviceId` came directly from the untrusted `X-Device-Id` HTTP request header without length or format validation. Attackers could supply oversized strings or arbitrary characters to pollute Durable Object keys or bypass per-device rate limiting.
**Learning:** Client-provided identifiers used in rate limiting or caching keys must always be strictly validated and sanitized with an allow-list regex (such as standard UUID/safe token syntax) and length constraints before being incorporated into system keys.
**Prevention:** Strictly enforce a regex pattern (`/^[a-zA-Z0-9_-]{1,64}$/`) and fall back to a safe default (`"unknown"`) on any validation failure.
