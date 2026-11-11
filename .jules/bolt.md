# Bolt's Performance Journal

## 2026-11-11 - Precompute Syntax Highlighter Keyword Sets
**Learning:** In Jetpack Compose code editors that apply `VisualTransformation` on every text edit, any allocations inside the highlighter's critical path execute on every single keystroke. Recreating language keyword `Set` objects dynamically on every highlight pass causes frequent heap allocations and GC pressure on mobile devices.
**Action:** Always precompute immutable token/keyword lookup tables per language statically once at class load time, and avoid allocating intermediate 1-character strings for single-char tokens or punctuation.
