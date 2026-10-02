## 2026-03-30 - Compose Syntax Highlighter Set Allocation Bottleneck
**Learning:** In Jetpack Compose code editor components, `SyntaxHighlighter.highlight` runs on every recomposition/keystroke. Re-allocating `Set` instances and `Set` unions (`commonKeywords + setOf(...)`) or `List` instances in tokenizer functions causes excessive object churn and potential GC lag on mobile devices.
**Action:** Pre-compute static keyword maps and comment token lists per `CodeLanguage` during `SyntaxHighlighter` object initialization.
