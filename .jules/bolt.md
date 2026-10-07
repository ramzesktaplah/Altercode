## 2026-10-07 - Avoid Heap Allocations in Compose Hot Paths
**Learning:** In Jetpack Compose text components and custom syntax highlighters that run on every keystroke/recomposition, string helper methods like `CharSequence.lines().size`, set concatenations (`commonKeywords + setOf(...)`), and single-character conversions (`char.toString()`) generate high heap garbage collector churn.
**Action:** Pre-compute immutable keyword/lookup sets at the object level and use zero-allocation primitives like `count { it == '\n' }` and `StringBuilder.append(Char)` for high-frequency string operations.
