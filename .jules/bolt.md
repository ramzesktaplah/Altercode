## 2026-03-29 - Allocation-free line counting and syntax token batching in Jetpack Compose Code Editor

**Learning:** `String.lines().size` inside Jetpack Compose `remember` / `VisualTransformation` callbacks allocates a `List<String>` containing substring copies of every line in the document on every keystroke or recomposition. Likewise, creating `Set` unions or `char.toString()` per token in high-frequency tokenizers like `SyntaxHighlighter` generates significant garbage collection overhead on the main thread during typing.
**Action:** Use a zero-allocation `countLines` character scanner for line count calculations in editors, precompute keyword maps, and batch adjacent symbol tokens in custom Compose syntax highlighters.
