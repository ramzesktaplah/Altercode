# Bolt's Performance Journal

## 2026-03-30 - Cache language keyword sets in SyntaxHighlighter
**Learning:** `SyntaxHighlighter.highlight()` runs on every editor keystroke or code rendering. Calling `keywords(language)` and `lineCommentTokens(language)` on each character/token loop or invocation created temporary `Set<String>` and `List<String>` instances dynamically.
**Action:** Precompute language keyword sets and line comment token maps at `SyntaxHighlighter` initialization time.
