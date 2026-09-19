// scripts/loadtest.ts — staged load test for the AlterCode AI proxy Worker.
//
// Sends synthetic requests through the full validation + rate-limiting path
// (X-Dry-Run header makes the Worker skip the Groq/Gemini call), using a
// unique X-Device-Id per request so rate limiting behaves like many distinct
// users rather than one.
//
// Usage:
//   bun scripts/loadtest.ts            # run all stages sequentially
//   bun scripts/loadtest.ts 3          # run a single stage by index (0-based)
//   LOADTEST_URL=... bun scripts/loadtest.ts

const BASE_URL =
  process.env.LOADTEST_URL ?? "https://codewise-ai-y3dvf84-backend.rork.app";

interface Stage {
  label: string;
  total: number;
  concurrency: number;
}

const STAGES: Stage[] = [
  { label: "warm-up", total: 100, concurrency: 10 },
  { label: "stage-1", total: 1_000, concurrency: 25 },
  { label: "stage-2", total: 5_000, concurrency: 50 },
  { label: "stage-3", total: 10_000, concurrency: 75 },
];

let counter = 0;

async function fireOnce(): Promise<{ status: number; ms: number }> {
  const id = `loadtest-${process.pid}-${Date.now()}-${counter++}`;
  const body = JSON.stringify({
    action: "explain",
    messages: [
      { role: "system", content: "You are a concise code explainer." },
      { role: "user", content: "function add(a, b) { return a + b }" },
    ],
    temperature: 0.2,
    maxTokens: 64,
  });
  const start = performance.now();
  try {
    const res = await fetch(`${BASE_URL}/v1/chat`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Device-Id": id,
        "X-Dry-Run": "altercode-loadtest",
      },
      body,
    });
    await res.text();
    return { status: res.status, ms: performance.now() - start };
  } catch {
    return { status: 0, ms: performance.now() - start };
  }
}

function percentile(sorted: number[], p: number): number {
  if (sorted.length === 0) return 0;
  const idx = Math.min(sorted.length - 1, Math.ceil((p / 100) * sorted.length) - 1);
  return sorted[Math.max(0, idx)];
}

async function runStage(stage: Stage): Promise<void> {
  const latencies: number[] = [];
  let ok = 0;
  let throttled = 0;
  let errors = 0;
  let completed = 0;
  const wallStart = performance.now();

  async function worker(): Promise<void> {
    for (;;) {
      const slot = completed++;
      if (slot >= stage.total) return;
      const r = await fireOnce();
      latencies.push(r.ms);
      if (r.status >= 200 && r.status < 300) ok++;
      else if (r.status === 429) throttled++;
      else errors++;
    }
  }

  await Promise.all(
    Array.from({ length: stage.concurrency }, () => worker()),
  );

  const wall = (performance.now() - wallStart) / 1000;
  latencies.sort((a, b) => a - b);
  const rps = latencies.length / wall;
  console.log(
    [
      `[${stage.label}] ${stage.total} requests @ ${stage.concurrency} concurrency`,
      `  wall=${wall.toFixed(1)}s  rps=${rps.toFixed(1)}`,
      `  p50=${percentile(latencies, 50).toFixed(0)}ms` +
        `  p95=${percentile(latencies, 95).toFixed(0)}ms` +
        `  p99=${percentile(latencies, 99).toFixed(0)}ms` +
        `  max=${latencies[latencies.length - 1]?.toFixed(0) ?? "0"}ms`,
      `  2xx=${ok}  429=${throttled}  errors=${errors}`,
    ].join("\n"),
  );
}

const arg = process.argv[2];
const selected =
  arg !== undefined ? [STAGES[Number(arg)]].filter(Boolean) : STAGES;
for (const stage of selected) {
  await runStage(stage);
}
