# AlterCode

AlterCode is an AI-powered developer tool and code assistant for Android. Built with Kotlin and Jetpack Compose, AlterCode enables software engineers to convert, refactor, fix, and explain code snippets across multiple programming languages directly on mobile devices. All AI interactions are routed through a serverless Cloudflare Worker proxy with built-in security and rate limiting.

---

## 🚀 Features

- **Code Conversion**: Translate code snippets between programming languages (Kotlin, Python, TypeScript, C++, Go, Rust, Java, Swift, C#, PHP, JavaScript, etc.) while preserving logic and language-idiomatic style.
- **Code Refactoring**: Optimize existing code structure, readability, and performance without altering behavior.
- **Bug Fixing**: Automatically detect syntax errors, logic flaws, and edge cases, returning corrected code along with explanatory summaries.
- **Code Explanation**: Breakdown complex code blocks into structured plain-English explanations formatted for mobile reading.
- **Encrypted Local Storage**: Local history and saved snippets are stored securely using SQLCipher-encrypted SQLite databases (`SnippetDatabase`) and Android `EncryptedSharedPreferences`.
- **Sliding-Window Rate Limiting**: Backend quota protection powered by Cloudflare Durable Objects.
- **Dark Theme & Syntax Highlighting**: Styled specifically for comfortable mobile code viewing and editing.

---

## 🏗 Repository Architecture & Project Structure

This monorepo follows the **Rork** framework configuration (`rork.json`) linking the native Android client with a serverless Cloudflare Workers backend.

```
.
├── android/                 # Native Android Application (Kotlin / Jetpack Compose)
│   ├── app/
│   │   ├── build.gradle.kts # Android module configuration & BuildConfig settings
│   │   └── src/main/java/com/ai/altercode/
│   │       ├── ai/          # AiCodeService & Ktor HTTP client integration
│   │       ├── data/        # SnippetRepository, SQLCipher database, UsageTracker, DeviceFingerprint
│   │       ├── ui/          # Jetpack Compose UI components, screens, theme, and navigation
│   │       └── ads/         # Rewarded ads integration
│   ├── build.gradle.kts     # Root Gradle build script
│   └── settings.gradle.kts  # Gradle settings & repositories
├── functions/               # Backend Cloudflare Worker API Proxy
│   ├── index.ts             # Main Worker proxy & AI provider router (Groq & Gemini)
│   ├── rate-limiter.ts      # Durable Object implementing sliding-window SQLite rate limiting
│   └── package.json         # Worker package definition
└── rork.json                # Rork monorepo app schema configuration
```

---

## ⚡ Backend Architecture & AI Routing

AI requests from the Android client are sent to the Cloudflare Worker proxy (`functions/index.ts`). The worker acts as a secure intermediary and routes requests to distinct LLM providers based on the action:

- **Analysis & Fixes (`fix`, `explain`)**: Routed to **Groq** for fast inference and code analysis.
- **Code Generation & Translation (`convert`, `refactor`)**: Routed to **Google Gemini** for multi-language code generation.

### Security & Safeguards
- **CORS Protection**: No CORS headers are emitted; browser clients cannot call the proxy, preventing quota consumption from unauthorized web clients. Native Android clients remain unaffected.
- **Strict Role & Count Validation**: Restricts input payloads to a maximum of 2 chat messages (`system` prompt scaffolding + `user` code block) and sanitizes message roles to prevent prompt injection.
- **Input Capping & Sanitization**: Enforces maximum character limits (up to 10,000 input characters, 30,000 total request characters, clamped temperature and token bounds).
- **Durable Object Rate Limiting**: Keyed by client IP and device fingerprint (`RateLimiter` Durable Object with SQLite backend):
  - **10 requests / minute** (burst limit)
  - **60 requests / hour** (sustained limit)
  - **200 requests / day** (daily limit)

---

## 🛠 Tech Stack

### Android Client (`android/`)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Networking**: Ktor Client (`ktor-client-android`, `ktor-client-content-negotiation`)
- **Local Database & Security**: SQLCipher (`net.zetetic:sqlcipher-android`) and AndroidX Security Crypto (`EncryptedSharedPreferences`)
- **Architecture**: ServiceLocator pattern for dependency management
- **Monetization**: Google Mobile Ads SDK (Rewarded Ads)

### Backend Proxy (`functions/`)
- **Runtime**: Cloudflare Workers
- **Language**: TypeScript
- **State & Storage**: Cloudflare Durable Objects with per-instance SQLite storage
- **Upstream APIs**: Groq API & Google Gemini API (OpenAI-compatible chat completions endpoints)

---

## 💻 Building & Running

### Prerequisites
- JDK 11 or higher
- Android Studio or Android SDK command-line tools
- Node.js (v18+) if modifying Cloudflare Workers

### Build the Android App

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd altercode
   ```

2. **Configure Backend URL (Optional)**:
   The Android app references `EXPO_PUBLIC_RORK_FUNCTIONS_URL` injected into `BuildConfig`. If unset, it defaults to the configured backend.
   ```bash
   export EXPO_PUBLIC_RORK_FUNCTIONS_URL="https://your-custom-worker.rork.app"
   ```

3. **Build Debug APK**:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   cd android
   ./gradlew test
   ```

### Backend Setup (`functions/`)

The Cloudflare Worker requires the following secret environment variables configured:
- `GROQ_API_KEY`: API key for Groq service.
- `GOOGLE_AI_KEY`: API key for Google Gemini service.
