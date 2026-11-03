## 2026-03-31 - Syntax Highlighter Tokenizer Memory Churn
**Learning:** `VisualTransformation` in Jetpack Compose executes on every user keystroke and selection change. Dynamically constructing keyword `Set`s and comment token `List`s inside the tokenizer causes heavy GC churn on the Android UI thread (~25-30 KB allocated per keystroke for typical source files).
**Action:** Always precompute language keyword `Set`s and syntax token `List`s statically, and use `Char` overloads when appending punctuation to `AnnotatedString.Builder` to prevent `char.toString()` string allocations.
