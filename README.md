# VoiceAIKit Android

A drop-in Jetpack Compose voice agent SDK powered by [LiveKit](https://livekit.io). Add a fully animated voice conversation UI to any Android app with just a few lines of code.

## Features

- **One-line integration** — `VoiceAgentView()` gives you a complete voice agent UI
- **12-layer animated orb** — Audio-reactive Canvas visualization with organic blobs, glow effects, floating particles, and glass highlights
- **Typewriter text** — Animated taglines with blinking cursor
- **Staggered fade-in** — Sequential element animations on session start
- **Multiple agent types** — Built-in configs for MentalHealth, LegalAdviser, FinanceGuru (or define your own)
- **Full theming** — `AgentTheme` with complete color customization (purple/teal default, amber/gold legal, or custom)
- **Auto retry** — Configurable connection retry logic
- **Form + Session flow** — User metadata collection before starting

## Requirements

- Android API 24+ (minSdk)
- Kotlin 1.9+
- Jetpack Compose

## Installation

Add JitPack to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add the dependency in your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.Rishisinghwindows:VoiceAIKitAndroid:1.0.0")
}
```

## Quick Start

```kotlin
import com.ubudy.voiceaikit.ui.VoiceAgentView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoiceAgentView()
        }
    }
}
```

### Custom Configuration

```kotlin
import com.ubudy.voiceaikit.models.*
import com.ubudy.voiceaikit.ui.VoiceAgentView

VoiceAgentView(
    config = VoiceAgentConfig(
        serverURL = "https://your-server.com",
        livekitURL = "wss://your-livekit.com",
        agentTypes = listOf(
            AgentTypeConfig.MentalHealth,
            AgentTypeConfig.LegalAdviser,
            AgentTypeConfig.FinanceGuru
        ),
        maxRetries = 5
    ),
    initialAgentType = "legalAdviser"
)
```

### Custom Theme

```kotlin
val myTheme = AgentTheme(
    backgroundColor = Color(0xFF1A1A2E),
    titleGradient = listOf(Color.Cyan, Color.Blue),
    accentColor = Color.Cyan,
    speakingAccentColor = Color.Green,
    idleBlobColors = listOf(/* 7 RGBA values */),
    idleRingColors = listOf(/* 6 Colors */),
    activeBlobColors = listOf(/* 7 RGBA values */),
    activeRingColors = listOf(/* 6 Colors */),
    idleGlowColor = RGBA(0.0f, 1.0f, 1.0f, 0.22f),
    activeGlowColor = RGBA(0.0f, 1.0f, 0.5f, 0.22f),
    orbDarkBase = Color(0xFF080812),
    idleOrbBg = RGBA(0.05f, 0.10f, 0.15f, 1f),
    activeOrbBg = RGBA(0.03f, 0.12f, 0.08f, 1f),
)

val myAgent = AgentTypeConfig(
    id = "custom",
    displayName = "My Agent",
    title = "Custom Assistant",
    subtitle = "Powered by AI",
    footerText = "Available 24/7",
    subjectPlaceholder = "What would you like to discuss?",
    taglines = listOf("Hello!", "How can I help?"),
    theme = myTheme
)

VoiceAgentView(
    config = VoiceAgentConfig(
        serverURL = "https://your-server.com",
        livekitURL = "wss://your-livekit.com",
        agentTypes = listOf(myAgent)
    )
)
```

## Architecture

```
voiceaikit/
└── src/main/java/com/ubudy/voiceaikit/
    ├── models/
    │   ├── ConnectionState.kt      # IDLE, CONNECTING, LISTENING, SPEAKING, DISCONNECTED
    │   ├── UserInfo.kt             # User metadata (name, subject, language, type)
    │   ├── RGBA.kt                 # Color interpolation helper
    │   ├── AgentTheme.kt           # Full color theming (Default + Legal presets)
    │   ├── AgentTypeConfig.kt      # Per-agent config (title, taglines, theme)
    │   └── VoiceAgentConfig.kt     # Server URLs, agent types, retry settings
    ├── services/
    │   ├── AudioAnalyzer.kt        # Real-time RMS audio level from WebRTC
    │   └── TokenService.kt         # Retrofit-based LiveKit token fetcher
    ├── viewmodels/
    │   └── VoiceAgentViewModel.kt  # LiveKit room, state machine, audio monitoring
    └── ui/
        ├── VoiceAgentView.kt       # Drop-in composable (form + session)
        ├── OrbView.kt              # 12-layer animated orb (Canvas)
        └── TypewriterText.kt       # Character-by-character text animation
```

## iOS Equivalent

Looking for the iOS version? Check out [VoiceAIKit](https://github.com/Rishisinghwindows/VoiceAIKit) — the same voice agent as a Swift Package Manager library.

## License

MIT
