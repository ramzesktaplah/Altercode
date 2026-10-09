## 2026-10-09 - Validate Client Headers Used as Durable Object Keys
**Vulnerability:** Unvalidated `X-Device-Id` headers were used directly in Durable Object rate-limiter keys (`${clientIp}:${deviceId}`), allowing attackers to bypass rate limits and trigger high DO instance creation by randomizing header values.
**Learning:** Device fingerprints or custom identifiers supplied in request headers must be strictly validated before being used in rate-limit keys or resource locators.
**Prevention:** Validate header formats (e.g. UUID regex) and fall back to a default rate limit bucket for unvalidated inputs.
