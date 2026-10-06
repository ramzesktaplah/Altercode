# Bolt Performance Journal

## 2026-03-22 - Pre-computing Compose Syntax Highlighter Keyword Maps
**Learning:** `SyntaxHighlighter` in Android Compose is evaluated during live editing (on every keystroke in `CodeEditor`) and re-renders in `CodeBlock`. Allocating keyword `Set`s and line comment `List`s on every `highlight()` pass triggers frequent garbage collection pauses during active typing.
**Action:** Pre-compute language keyword sets and comment tokens in a static `Map<CodeLanguage, ...>` during initialization and add direct `Char` overloads to avoid string allocations.
