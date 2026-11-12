## 2026-03-29 - Pre-compute Syntax Highlighter Keywords & Token Sets
**Learning:** In Jetpack Compose code editor fields using live `VisualTransformation`, calling set concatenation (`commonKeywords + setOf(...)`) on every keystroke/render pass creates continuous GC pressure on the main UI thread.
**Action:** Pre-compute static language keyword sets and line comment token lists into static maps (`Map<CodeLanguage, Set<String>>`) on `SyntaxHighlighter` initialization.
