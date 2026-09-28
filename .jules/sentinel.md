## 2026-09-28 - Rate Limit Key Injection and Early Validation
**Vulnerability:** Unsanitized user headers (`X-Device-Id`, `X-Forwarded-For`) with colons or special characters allowed delimiter injection in rate limiting composite keys (`clientIp:deviceId`), causing key space collisions.
**Learning:** Composite keys constructed from user-supplied or forwarded HTTP headers must sanitize key components and use unambiguous field prefixes. Oversized request payloads should be validated prior to invoking backend Durable Object RPC calls to prevent resource consumption.
**Prevention:** Always sanitize individual header components (e.g. stripping colons/special characters) and validate body size before performing Durable Object state lookups.
