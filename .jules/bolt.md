# Bolt's Performance Journal

Critical learnings only.

## 2026-03-31 - Precomputing Language Syntax Tokens in SyntaxHighlighter
**Learning:** In AlterCode's Jetpack Compose editor, `SyntaxHighlighter.highlight` executes on every keystroke and visual transform pass. Constructing ad-hoc keyword `Set` collections via set concatenation (`+`) and `List` token collections per highlight run induces avoidable allocation churn on the UI thread and triggers GC pauses during typing. Precomputing immutable lookup sets per `CodeLanguage` entry eliminates per-stroke heap allocations.
**Action:** When working on text analysis or tokenizing in mobile UI rendering paths, ensure all keyword dictionaries and syntax markers are precomputed and statically cached per language.
