package com.ubudy.voiceaikit.models

import androidx.compose.ui.graphics.Color

data class AgentTheme(
    val backgroundColor: Color,
    val titleGradient: List<Color>,
    val accentColor: Color,
    val speakingAccentColor: Color,
    val subtleTextColor: Color = Color.White.copy(alpha = 0.25f),
    val dimmedTextColor: Color = Color.White.copy(alpha = 0.1f),

    // Orb idle colors
    val idleBlobColors: List<RGBA>,
    val idleRingColors: List<Color>,

    // Orb speaking colors
    val activeBlobColors: List<RGBA>,
    val activeRingColors: List<Color>,

    // Orb glow
    val idleGlowColor: RGBA,
    val activeGlowColor: RGBA,

    // Orb body
    val orbDarkBase: Color,
    val idleOrbBg: RGBA,
    val activeOrbBg: RGBA
) {
    companion object {
        val Default = AgentTheme(
            backgroundColor = Color(0xFF0A0A0F),
            titleGradient = listOf(Color(0xFFA78BFA), Color(0xFF6366F1)),
            accentColor = Color(0xFFA78BFA).copy(alpha = 0.8f),
            speakingAccentColor = Color(0xFF14F195).copy(alpha = 0.8f),
            idleBlobColors = listOf(
                RGBA(0.545f, 0.361f, 0.965f, 0.90f),
                RGBA(0.388f, 0.400f, 0.945f, 0.80f),
                RGBA(0.231f, 0.510f, 0.965f, 0.70f),
                RGBA(0.024f, 0.714f, 0.831f, 0.65f),
                RGBA(0.655f, 0.545f, 0.980f, 0.55f),
                RGBA(0.400f, 0.300f, 0.900f, 0.60f),
                RGBA(0.500f, 0.400f, 0.950f, 0.50f),
            ),
            idleRingColors = listOf(
                Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF3B82F6),
                Color(0xFF06B6D4), Color(0xFFA78BFA), Color(0xFF8B5CF6),
            ),
            activeBlobColors = listOf(
                RGBA(0.078f, 0.945f, 0.584f, 0.90f),
                RGBA(0.024f, 0.714f, 0.831f, 0.80f),
                RGBA(0.204f, 0.827f, 0.600f, 0.70f),
                RGBA(0.133f, 0.827f, 0.933f, 0.65f),
                RGBA(0.078f, 0.945f, 0.584f, 0.55f),
                RGBA(0.050f, 0.800f, 0.700f, 0.60f),
                RGBA(0.100f, 0.900f, 0.500f, 0.50f),
            ),
            activeRingColors = listOf(
                Color(0xFF14F195), Color(0xFF06B6D4), Color(0xFF3B82F6),
                Color(0xFF22D3EE), Color(0xFF10B981), Color(0xFF14F195),
            ),
            idleGlowColor = RGBA(0.545f, 0.361f, 0.965f, 0.22f),
            activeGlowColor = RGBA(0.078f, 0.945f, 0.584f, 0.22f),
            orbDarkBase = Color(0xFF080812),
            idleOrbBg = RGBA(0.12f, 0.07f, 0.27f, 1f),
            activeOrbBg = RGBA(0.04f, 0.12f, 0.10f, 1f),
        )

        val Legal = AgentTheme(
            backgroundColor = Color(0xFF1A120A),
            titleGradient = listOf(Color(0xFFD4880A), Color(0xFFDAA520)),
            accentColor = Color(0xFFDAA520),
            speakingAccentColor = Color(0xFFF5C850),
            idleBlobColors = listOf(
                RGBA(0.824f, 0.549f, 0.157f, 0.90f),
                RGBA(0.784f, 0.510f, 0.137f, 0.80f),
                RGBA(0.855f, 0.627f, 0.196f, 0.70f),
                RGBA(0.745f, 0.471f, 0.118f, 0.65f),
                RGBA(0.906f, 0.690f, 0.275f, 0.55f),
                RGBA(0.780f, 0.490f, 0.120f, 0.60f),
                RGBA(0.860f, 0.600f, 0.200f, 0.50f),
            ),
            idleRingColors = listOf(
                Color(0xFFD4880A), Color(0xFFC87020), Color(0xFFE8A020),
                Color(0xFFB8860B), Color(0xFFDAA520), Color(0xFFD4880A),
            ),
            activeBlobColors = listOf(
                RGBA(0.961f, 0.784f, 0.314f, 0.90f),
                RGBA(0.855f, 0.667f, 0.125f, 0.80f),
                RGBA(0.941f, 0.753f, 0.235f, 0.70f),
                RGBA(0.784f, 0.627f, 0.157f, 0.65f),
                RGBA(0.961f, 0.816f, 0.376f, 0.55f),
                RGBA(0.880f, 0.700f, 0.200f, 0.60f),
                RGBA(0.940f, 0.780f, 0.280f, 0.50f),
            ),
            activeRingColors = listOf(
                Color(0xFFF5C850), Color(0xFFDAA520), Color(0xFFE8B830),
                Color(0xFFC8A020), Color(0xFFF0D060), Color(0xFFF5C850),
            ),
            idleGlowColor = RGBA(0.824f, 0.549f, 0.157f, 0.22f),
            activeGlowColor = RGBA(0.961f, 0.784f, 0.314f, 0.22f),
            orbDarkBase = Color(0xFF0D0804),
            idleOrbBg = RGBA(0.10f, 0.06f, 0.03f, 1f),
            activeOrbBg = RGBA(0.10f, 0.08f, 0.03f, 1f),
        )
    }
}
