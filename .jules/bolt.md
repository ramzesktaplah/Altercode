## 2026-03-31 - Durable Object SQLite Rate Limiter Indexing & Schema Cache

**Learning:** In Cloudflare Workers Durable Objects using SQLite storage, executing DDL statements (`CREATE TABLE IF NOT EXISTS`) on every request causes unnecessary schema locks/parsing overhead. Furthermore, range counts (`SELECT COUNT(*) WHERE timestamp >= ?`) and purging deletions (`DELETE WHERE timestamp < ?`) without an index force O(N) full-table scans per request.

**Action:** Lazily initialize DDL schema and `idx_requests_timestamp` index once per DO instance lifetime to ensure O(log N) lookup and deletion performance across sliding-window rate limit checks.
