# Sentinel's Journal

## 2026-03-24 - Relying on Omitting CORS Headers vs Actively Blocking Origin Headers
**Vulnerability:** The AI proxy worker assumed that not emitting CORS headers prevented unauthorized browser clients from abusing backend LLM quotas. In reality, CORS is browser-enforced for reading response bodies; browser forms or cross-origin requests (e.g. `mode: "no-cors"`, `navigator.sendBeacon`, or `<form>` posts) still trigger the server-side LLM call and burn paid Groq/Gemini API credits.
**Learning:** For serverless APIs strictly intended for native mobile apps (such as AlterCode Android), never rely solely on absent `Access-Control-Allow-Origin` headers. Native apps do not send an `Origin` header during standard HTTP requests, whereas modern browsers mandate attaching `Origin` on cross-origin requests.
**Prevention:** Actively reject any request containing an `Origin` header (`403 Forbidden`) at the entry gate of the API before executing rate limiting or calling upstream providers.
