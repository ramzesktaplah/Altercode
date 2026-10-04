## 2026-10-04 - Durable Object Rate Limiter SQL Query Batching and Indexing

**Learning:** Running sequential DDL (`CREATE TABLE IF NOT EXISTS`), cleanup (`DELETE`), and multiple separate window `SELECT COUNT(*)` queries on unindexed SQLite tables in Durable Objects introduces significant request latency and write contention on hot paths.
**Action:** Initialize SQLite schema (including indices on timestamp) lazily once per instance, batch window check counts into a single conditional aggregation query (`SELECT COUNT(CASE WHEN ...)...`), and run table cleanup probabilistically or conditionally.
