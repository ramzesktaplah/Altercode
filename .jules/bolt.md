# Bolt's Journal - Critical Learnings

## 2026-09-25 - Pre-computing syntax highlighter token sets in Compose code editor
**Learning:** Instantiating new sets (`commonKeywords + setOf(...)`) and lists on every invocation of live syntax highlighting during text editing causes unnecessary GC pressure and object allocations on the UI thread.
**Action:** Pre-compute static mapping tables (`Map<CodeLanguage, Set<String>>`) eagerly when tokenizing or syntax highlighting text in real-time UI components.
