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
   * Sliding-window check. Counts existing rows in three windows; if any
   * limit is exceeded, returns 429. Otherwise inserts the new timestamp
   * and returns 200. Rows older than 24h are lazily purged on each call.
   */
  private async check(): Promise<Response> {
    const now = Date.now();
    const cutoff = now - WINDOW_DAY;

    const sql = this.state.storage.sql as SqlStorage;

    sql.exec(
      `CREATE TABLE IF NOT EXISTS requests (timestamp INTEGER)`,
    );

    // Lazy cleanup: remove rows older than 24h.
    sql.exec(`DELETE FROM requests WHERE timestamp < ?`, cutoff);

    const countMinute = this.countSince(sql, now - WINDOW_MINUTE);
    if (countMinute >= LIMIT_MINUTE) {
      return Response.json(
        { error: "rate_limited", window: "minute", retryAfter: 60 },
        { status: 429 },
      );
    }

    const countHour = this.countSince(sql, now - WINDOW_HOUR);
    if (countHour >= LIMIT_HOUR) {
      return Response.json(
        { error: "rate_limited", window: "hour", retryAfter: 3600 },
        { status: 429 },
      );
    }

    const countDay = this.countSince(sql, cutoff);
    if (countDay >= LIMIT_DAY) {
      return Response.json(
        { error: "rate_limited", window: "day", retryAfter: 86400 },
        { status: 429 },
      );
    }

    sql.exec(`INSERT INTO requests (timestamp) VALUES (?)`, now);

    return Response.json({ ok: true });
  }

  /** Count rows with timestamp >= [since]. */
  private countSince(sql: SqlStorage, since: number): number {
    const cursor = sql.exec(
      `SELECT COUNT(*) as n FROM requests WHERE timestamp >= ?`,
      since,
    );
    for (const row of cursor) {
      const value = (row as Record<string, unknown>).n;
      if (typeof value === "number") return value;
      if (typeof value === "bigint") return Number(value);
      return 0;
    }
    return 0;
  }
}
