## 2026-03-22 - Precomputing Syntax Highlight Lookups & VisualTransformation Memoization
**Learning:** BasicTextField in Jetpack Compose invokes VisualTransformation.filter() on every cursor movement, selection change, and re-layout pass. Caching TransformedText when text content is unchanged eliminates redundant O(N) tokenization passes. Precomputing language keyword maps avoids allocating 80+ item Sets on every highlight call.
**Action:** Always memoize VisualTransformation in Jetpack Compose code editors and precompute static language keyword maps.
