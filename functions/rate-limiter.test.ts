import { describe, expect, it } from "bun:test";
import { Database } from "bun:sqlite";

describe("RateLimiter SQLite Indexing and Behavior", () => {
  it("uses covering index for sliding window queries and range deletion", () => {
    const db = new Database(":memory:");
    db.run("CREATE TABLE IF NOT EXISTS requests (timestamp INTEGER)");
    db.run("CREATE INDEX IF NOT EXISTS idx_requests_timestamp ON requests (timestamp)");

    // Query plan for countSince: SELECT COUNT(*) as n FROM requests WHERE timestamp >= ?
    const countPlan = db
      .query("EXPLAIN QUERY PLAN SELECT COUNT(*) as n FROM requests WHERE timestamp >= ?")
      .all(Date.now());
    expect(
      countPlan.some((row: any) =>
        row.detail.includes("COVERING INDEX idx_requests_timestamp"),
      ),
    ).toBe(true);

    // Query plan for cleanup: DELETE FROM requests WHERE timestamp < ?
    const deletePlan = db
      .query("EXPLAIN QUERY PLAN DELETE FROM requests WHERE timestamp < ?")
      .all(Date.now());
    expect(
      deletePlan.some((row: any) =>
        row.detail.includes("INDEX idx_requests_timestamp"),
      ),
    ).toBe(true);
  });

  it("accurately tracks counts and purges rows older than 24h", () => {
    const db = new Database(":memory:");
    db.run("CREATE TABLE IF NOT EXISTS requests (timestamp INTEGER)");
    db.run("CREATE INDEX IF NOT EXISTS idx_requests_timestamp ON requests (timestamp)");

    const now = Date.now();
    const WINDOW_MINUTE = 60_000;
    const WINDOW_HOUR = 3_600_000;
    const WINDOW_DAY = 86_400_000;

    const insert = db.prepare("INSERT INTO requests (timestamp) VALUES (?)");
    // 5 requests in last minute
    for (let i = 0; i < 5; i++) insert.run(now - 1000 * i);
    // 10 requests 10 minutes ago
    for (let i = 0; i < 10; i++) insert.run(now - WINDOW_MINUTE * 10);
    // 20 requests 5 hours ago
    for (let i = 0; i < 20; i++) insert.run(now - WINDOW_HOUR * 5);
    // 30 requests 2 days ago (stale)
    for (let i = 0; i < 30; i++) insert.run(now - WINDOW_DAY * 2);

    // Verify cleanup
    db.run("DELETE FROM requests WHERE timestamp < ?", [now - WINDOW_DAY]);

    const countSince = (since: number): number => {
      const row = db
        .query("SELECT COUNT(*) as n FROM requests WHERE timestamp >= ?")
        .get(since) as { n: number };
      return row.n;
    };

    expect(countSince(now - WINDOW_MINUTE)).toBe(5);
    expect(countSince(now - WINDOW_HOUR)).toBe(15); // 5 + 10
    expect(countSince(now - WINDOW_DAY)).toBe(35); // 5 + 10 + 20
  });
});
