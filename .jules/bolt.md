# Bolt's Journal - Critical Performance Learnings

## 2026-03-30 - Cloudflare Durable Object SQLite Indexes
**Learning:** Cloudflare Durable Object instances using SQLite storage execute SQL commands synchronously per request. When a table like `requests` (used in sliding-window rate limiters) has queries filtering on range columns like `WHERE timestamp >= ?` and `DELETE WHERE timestamp < ?`, without an index every incoming request forces sequential full table scans. Adding an index on `timestamp` converts table scans to covering index scans (`SEARCH requests USING COVERING INDEX idx_requests_timestamp`), yielding a ~3x speedup in execution time.
**Action:** Always create indexes on timestamp and lookup columns for SQLite tables inside Cloudflare Durable Objects during table initialization.
