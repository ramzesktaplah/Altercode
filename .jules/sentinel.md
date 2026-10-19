# Sentinel Security Journal

## 2026-03-19 - Input Validation and Type Safety Bypass in Cloudflare Worker Proxy
**Vulnerability:** The API proxy worker (`functions/index.ts`) relied on loose input checks (`messages?.length` and `Number(val) || default`) which allowed malformed inputs (non-array `messages`, elements missing string `content`, or `NaN` numeric parameters) to bypass validation or cause unexpected runtime errors.
**Learning:** In TypeScript Cloudflare Workers parsing untrusted JSON (`request.json()`), runtime type checking (`Array.isArray`, `typeof`, `Number.isFinite`) is required because TypeScript types do not validate runtime JSON structures.
**Prevention:** Always strictly validate array structures, property types, and numeric range parsing (`Number.isFinite`) for incoming API request payloads.
