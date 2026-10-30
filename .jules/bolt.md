## 2026-10-30 - Syntax Highlighter Allocation & Tokenization Overhead

**Learning:** In Compose code editors (`CodeEditor` / `BasicTextField`), `VisualTransformation` runs `SyntaxHighlighter.highlight` on every cursor movement or text change. Creating intermediate `Set` instances for keywords (`commonKeywords + setOf(...)`) and appending single-character punctuation/whitespace tokens creates high GC pressure and unnecessary `SpanStyle` allocations in `AnnotatedString`. Pre-calculating language keyword maps and batching contiguous punctuation/whitespace tokens reduces allocation churn during live typing.

**Action:** Pre-compute static language mappings and batch non-syntax contiguous character runs when building `AnnotatedString`s in Compose visual transformations.
