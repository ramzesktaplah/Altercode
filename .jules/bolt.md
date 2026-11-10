# Bolt's Journal

## 2026-03-31 - Precomputing Syntax Tokenizer Sets in Jetpack Compose
**Learning:** In Jetpack Compose, `VisualTransformation` executes synchronously on the UI thread on every keystroke and recomposition. Dynamically concatenating sets (`commonKeywords + setOf(...)`) or constructing token lists inside tokenizer helper functions allocates new `LinkedHashSet` instances with 70+ string items on every typed character, causing unnecessary heap churn and garbage collection pauses on the main thread.
**Action:** Precompute immutable token and keyword collections in static/singleton mappings for all supported languages so syntax highlighting remains zero-allocation on the typing hot path.
