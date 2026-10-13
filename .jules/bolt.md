## 2026-03-31 - Compose VisualTransformation Memory Allocations in Live Editor

**Learning:** In Jetpack Compose, `VisualTransformation` lambdas execute on every single keystroke during text editing in `CodeEditor`. Instantiating `Set`s, `List`s, or calling `String.lines()` inside the tokenizer or line counter causes severe GC pressure on every key event.
**Action:** Always pre-compute static language keyword sets and comment token lists via lazy maps, and use non-allocating character iteration (`count { it == '\n' } + 1`) for line counting in code editor components.
