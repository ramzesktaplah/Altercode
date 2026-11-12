# Bolt's Performance Journal

## 2026-10-14 - SQLite Durable Object RateLimiter Covering Index
**Learning:** In Cloudflare Durable Objects with SqlStorage, sliding-window rate limiters that purge expired records (`DELETE ... WHERE timestamp < ?`) and compute window counts (`SELECT COUNT(*) ... WHERE timestamp >= ?`) suffer from full table scans (`SCAN requests`) when tables lack an index on `timestamp`. Adding `CREATE INDEX IF NOT EXISTS idx_requests_timestamp ON requests (timestamp)` turns the purge into an index search and the counts into `SEARCH requests USING COVERING INDEX`, resulting in an ~3x throughput improvement for rate limit checks without adding memory or schema overhead.
**Action:** Always ensure timestamp and range-queried columns in SQLite Durable Objects have explicit indexes created alongside the table definition.
