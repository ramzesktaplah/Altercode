# Bolt's Journal - Critical Performance Learnings

## 2026-03-30 - Durable Object SQLite Rate Limiter Indexing
**Learning:** In SQLite-backed sliding-window rate limiters executed on Cloudflare Durable Objects, executing multiple `COUNT(*)` range scans (`timestamp >= ?`) and `DELETE FROM requests WHERE timestamp < ?` without an index triggers full table scans on every check. Single-query aggregation with conditional expressions (`TOTAL(CASE WHEN...)`) causes SQLite to inspect every row in the 24-hour window, performing worse than separate indexed range lookups. Adding an index on `timestamp` provides a covering index for range count queries and speedups of >4x during sliding-window rate limiting checks.
**Action:** Always index timestamp columns in time-series and rate-limiting SQLite tables, and verify with `EXPLAIN QUERY PLAN` that queries use `COVERING INDEX` for range scans.
