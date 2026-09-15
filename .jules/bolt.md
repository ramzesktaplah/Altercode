# Bolt's Journal

## 2026-03-09 - Static precomputation of syntax highlighting lookup maps
**Learning:** `SyntaxHighlighter.highlight` is called on every keystroke during code editing and recomposition. Allocating new `Set` and `List` instances via `keywords(language)` and `lineCommentTokens(language)` on every pass creates continuous GC pressure and wasted cpu cycles during active typing.
**Action:** Statically map `CodeLanguage` to pre-allocated `Set<String>` and `List<String>` at initialization time.
