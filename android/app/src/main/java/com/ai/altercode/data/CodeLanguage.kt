package com.ai.altercode.data

import androidx.compose.ui.graphics.Color

/** Languages AlterCode can translate between, plus the auto-detect sentinel. */
enum class CodeLanguage(
    val id: String,
    val label: String,
    val badge: String,
    val accent: Color
) {
    AUTO("auto", "Auto-detect", "AI", Color(0xFF60A5FA)),
    PYTHON("python", "Python", "PY", Color(0xFF4B8BBE)),
    JAVASCRIPT("javascript", "JavaScript", "JS", Color(0xFFE9C846)),
    TYPESCRIPT("typescript", "TypeScript", "TS", Color(0xFF3178C6)),
    CPP("cpp", "C++", "C++", Color(0xFF6E9CD2)),
    JAVA("java", "Java", "JV", Color(0xFFE7803C)),
    CSHARP("csharp", "C#", "C#", Color(0xFF9B72C4)),
    GO("go", "Go", "GO", Color(0xFF00ADD8)),
    RUST("rust", "Rust", "RS", Color(0xFFD98B5F)),
    SWIFT("swift", "Swift", "SW", Color(0xFFF05138)),
    PHP("php", "PHP", "PHP", Color(0xFF8892BF)),
    KOTLIN("kotlin", "Kotlin", "KT", Color(0xFF9D7BEA));

    val isConcrete: Boolean get() = this != AUTO

    companion object {
        /** Selectable source options (auto-detect first). */
        val sourceOptions: List<CodeLanguage> = entries.toList()

        /** Selectable translation targets. */
        val targetOptions: List<CodeLanguage> = entries.filter { it.isConcrete }

        fun fromId(id: String?): CodeLanguage? {
            if (id.isNullOrBlank()) return null
            val normalized = id.trim().lowercase()
            return entries.firstOrNull { it.id == normalized }
                ?: entries.firstOrNull { it.label.lowercase() == normalized }
                ?: when (normalized) {
                    "py", "python3" -> PYTHON
                    "js", "node", "nodejs", "ecmascript" -> JAVASCRIPT
                    "ts", "tsx" -> TYPESCRIPT
                    "c++", "cplusplus", "cxx", "c" -> CPP
                    "c#", "cs", "dotnet" -> CSHARP
                    "golang" -> GO
                    "kt" -> KOTLIN
                    else -> null
                }
        }
    }
}
