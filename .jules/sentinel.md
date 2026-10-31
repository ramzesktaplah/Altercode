# Sentinel Security Journal

## 2026-10-31 - HTTP Header Injection and Payload Sanitization in Durable Object Proxy
**Vulnerability:**
The Cloudflare Worker proxy (`functions/index.ts`) accepted raw, unvalidated client headers (`X-Device-Id` and forwarded IP) and forwarded them directly in subrequest headers (`X-Rork-DO-Id`) when invoking the `RateLimiter` Durable Object. Additionally, chat messages in `handleChat` did not validate that each message object contained valid string `content` or that `messages` was strictly an array.

**Learning:**
When constructing internal dispatch headers or persistence keys for stateful backends (such as Durable Objects) from incoming HTTP headers, untrusted values must be strictly whitelisted and length-capped. Otherwise, unexpected characters (such as CR/LF or non-alphanumeric tokens) can lead to header injection/smuggling, corrupted rate-limiting partitions, or bypassed quotas.

**Prevention:**
Always sanitize and bound identifier strings before placing them in HTTP request headers or using them as storage partition keys. Enforce strict JSON schema validations on array types and object properties at the API gateway layer before any processing.
