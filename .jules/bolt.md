# Bolt's Journal

## 2026-03-30 - Cache syntax highlighting keyword sets and batch punctuation spans
**Learning:** `SyntaxHighlighter.highlight` runs on every keystroke during code editing. Re-creating keyword `Set`s and `List`s of line comment tokens on every highlight pass allocates significant memory, triggering frequent Android GC passes and dropped frames. Batching contiguous punctuation/plain chars into single `appendStyled` calls drastically reduces `AnnotatedString` `SpanStyle` node allocations.
**Action:** Pre-compute and map keywords and comment markers per `CodeLanguage`, and batch contiguous punctuation characters when tokenizing.
