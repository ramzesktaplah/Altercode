// functions/rate-limiter.ts — RateLimiter Durable Object.
//
// Sliding-window rate limiter using per-object SQLite storage.
// Each instance is keyed by `${clientIp}:${deviceId}`, giving every
// device+IP combination its own independent counter.
//
// Limits:
//   10 requests / minute  (burst protection)
//   60 requests / hour    (sustained-use cap)
//   200 requests / day     (daily quota)
//
// The Worker dispatches a POST /check request before forwarding to
// the AI provider. A 200 means "allowed"; a 429 means "throttled".

export { RateLimiter };

const WINDOW_MINUTE = 60_000; // ms
const WINDOW_HOUR = 3_600_000; // ms
const WINDOW_DAY = 86_400_000; // ms

const LIMIT_MINUTE = 10;
const LIMIT_HOUR = 60;
const LIMIT_DAY = 200;

class RateLimiter implements DurableObject {
  private state: DurableObjectState;
  private initialized = false;

  constructor(state: DurableObjectState, _env: unknown) {
    this.state = state;
  }

  async fetch(request: Request): Promise<Response> {
    const url = new URL(request.url);

    if (url.pathname === "/check" && request.method === "POST") {
      return this.check();
    }

    return new Response("not found", { status: 404 });
  }

  /**
   * Ensures the database schema and index exist.
   * Executed once per Durable Object instance lifetime to avoid redundant DDL calls.
   */
  private ensureSchema(sql: SqlStorage) {
    if (this.initialized) return;
    sql.exec(
      `CREATE TABLE IF NOT EXISTS requests (timestamp INTEGER);` +
        `CREATE INDEX IF NOT EXISTS idx_requests_timestamp ON requests(timestamp);`,
    );
    this.initialized = true;
  }

  /**
   * Sliding-window check.
   *
   * Optimization details:
   * - Schema / Index: Lazily created once per DO instance lifetime to eliminate repeated DDL execution.
   * - Single Query Aggregation: Batches all 3 window counts (minute, hour, day) into a single conditional
   *   aggregation query (`COUNT(CASE WHEN ...)`), reducing SQLite queries from 3 to 1.
   * - Indexed Lookup: Uses `idx_requests_timestamp` range scan (`timestamp >= cutoff`), avoiding full table scans.
   * - Probabilistic Cleanup: Runs row purging only ~10% of the time, avoiding unnecessary write locks
   *   and disk I/O on every request.
   *
   * Expected impact: Reduces DB operations per check from 5 sequential queries to 1 read query
   * (plus 1 insert on allowed requests), significantly lowering latency and IOPS.
   */
  private async check(): Promise<Response> {
    const now = Date.now();
    const cutoff = now - WINDOW_DAY;

    const sql = this.state.storage.sql as SqlStorage;
    this.ensureSchema(sql);

    // Probabilistic cleanup (~10% of calls) to purge stale rows (>24h)
    // without incurring write overhead on every single check.
    if (Math.random() < 0.1) {
      sql.exec(`DELETE FROM requests WHERE timestamp < ?`, cutoff);
    }

    const { countMinute, countHour, countDay } = this.getWindowCounts(
      sql,
      now,
    );

    if (countMinute >= LIMIT_MINUTE) {
      return Response.json(
        { error: "rate_limited", window: "minute", retryAfter: 60 },
        { status: 429 },
      );
    }

    if (countHour >= LIMIT_HOUR) {
      return Response.json(
        { error: "rate_limited", window: "hour", retryAfter: 3600 },
        { status: 429 },
      );
    }

    if (countDay >= LIMIT_DAY) {
      return Response.json(
        { error: "rate_limited", window: "day", retryAfter: 86400 },
        { status: 429 },
      );
    }

    sql.exec(`INSERT INTO requests (timestamp) VALUES (?)`, now);

    return Response.json({ ok: true });
  }

  /**
   * Single query to compute window counts for minute, hour, and day using conditional aggregation.
   */
  private getWindowCounts(
    sql: SqlStorage,
    now: number,
  ): { countMinute: number; countHour: number; countDay: number } {
    const minCutoff = now - WINDOW_MINUTE;
    const hourCutoff = now - WINDOW_HOUR;
    const dayCutoff = now - WINDOW_DAY;

    const cursor = sql.exec(
      `SELECT
        COUNT(CASE WHEN timestamp >= ? THEN 1 END) as m,
        COUNT(CASE WHEN timestamp >= ? THEN 1 END) as h,
        COUNT(*) as d
       FROM requests
       WHERE timestamp >= ?`,
      minCutoff,
      hourCutoff,
      dayCutoff,
    );

    for (const row of cursor) {
      const r = row as Record<string, unknown>;
      return {
        countMinute: Number(r.m ?? 0),
        countHour: Number(r.h ?? 0),
        countDay: Number(r.d ?? 0),
      };
    }

    return { countMinute: 0, countHour: 0, countDay: 0 };
  }
}
