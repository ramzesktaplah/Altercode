## 2026-03-29 - Durable Object Key Sanitization & Rate Limit Retry-After Propagation

**Vulnerability:**
Client-controlled headers like `X-Device-Id` were used raw to construct Durable Object keys (`${clientIp}:${deviceId}`). Furthermore, rate-limiting DO responses lacked standard `Retry-After` headers, causing the worker proxy to default all retry times to 60s regardless of hourly/daily quota windows.

**Learning:**
Always sanitize untrusted client headers before using them in key construction or internal routing to prevent key pollution and DoS vector inflation. Ensure rate-limiting components explicitly pass standard HTTP headers (`Retry-After`) to caller responses.

**Prevention:**
Strip unsafe characters and restrict character length on headers used for storage key generation, and ensure 429 status responses include explicit `Retry-After` headers.
