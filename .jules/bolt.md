## 2026-03-30 - SQLite Durable Object RateLimiter Indexing & Schema Guard
**Learning:** Executing `CREATE TABLE IF NOT EXISTS` on every request in Cloudflare Durable Objects adds repeated DDL parsing overhead, and lacking indexes on timestamp-based range queries (`DELETE` & `COUNT(*)`) causes $O(N)$ full table scans on SQLite storage.
**Action:** Always guard DDL statements with an in-memory instance flag and ensure proper indexes are created for timestamp/filtering columns in Durable Object SQLite schemas.
