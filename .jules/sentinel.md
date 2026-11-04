# Sentinel's Journal

## 2026-03-29 - Durable Object Rate-Limiter Key Sanitization and Header Injection
**Vulnerability:** In serverless architectures routing rate-limiting checks to Durable Objects or external state stores keyed by client headers (`CF-Connecting-IP` / `X-Forwarded-For` and `X-Device-Id`), unvalidated user headers can lead to rate-limit partition exhaustion, delimiter injection, or rate-limit evasion by supplying arbitrary random or delimited strings.
**Learning:** `request.headers.get("X-Device-Id")` was previously concatenated directly with the IP address (`${clientIp}:${deviceId}`) without format checks or size bounds. If a client sent arbitrary tokens or special characters (such as `:`), it caused key collision or DO ID namespace pollution.
**Prevention:** Strictly enforce a regex whitelist (`/^[a-zA-Z0-9_-]{1,64}$/`) and length limits on client-provided identifiers before constructing rate-limiting keys, falling back to a safe constant (e.g. `"unknown"`) when invalid.
