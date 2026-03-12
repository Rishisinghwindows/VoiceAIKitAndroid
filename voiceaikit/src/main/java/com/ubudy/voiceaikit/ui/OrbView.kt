package com.ubudy.voiceaikit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import com.ubudy.voiceaikit.models.AgentTheme
import com.ubudy.voiceaikit.models.ConnectionState
import com.ubudy.voiceaikit.models.RGBA
import kotlin.math.*

private data class BlobConfig(
    val xFreq: Float, val xPhase: Float,
    val yFreq: Float, val yPhase: Float,
    val xAmp: Float, val yAmp: Float,
    val size: Float,
    val x2Freq: Float, val y2Freq: Float,
    val x2Amp: Float, val y2Amp: Float
)

private val blobConfigs = listOf(
    BlobConfig(0.35f, 0f, 0.28f, 0.5f, 55f, 45f, 0.82f, 0.13f, 0.17f, 20f, 15f),
    BlobConfig(0.25f, 1.5f, 0.32f, 2.0f, 50f, 50f, 0.72f, 0.11f, 0.19f, 18f, 22f),
    BlobConfig(0.30f, 3.0f, 0.22f, 3.5f, 40f, 40f, 0.65f, 0.15f, 0.12f, 15f, 18f),
    BlobConfig(0.20f, 4.5f, 0.35f, 5.0f, 45f, 35f, 0.55f, 0.18f, 0.14f, 12f, 20f),
    BlobConfig(0.28f, 5.5f, 0.25f, 6.2f, 35f, 45f, 0.45f, 0.16f, 0.21f, 16f, 14f),
    BlobConfig(0.22f, 2.2f, 0.30f, 1.1f, 48f, 38f, 0.60f, 0.14f, 0.16f, 14f, 16f),
    BlobConfig(0.33f, 4.0f, 0.18f, 4.8f, 30f, 50f, 0.50f, 0.20f, 0.10f, 10f, 12f),
)

@Composable
fun OrbView(
    state: ConnectionState,
    audioLevel: Float,
    theme: AgentTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1_000_000, easing = LinearEasing)),
        label = "time"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val isSpeaking = state == ConnectionState.SPEAKING
    val isConnecting = state == ConnectionState.CONNECTING
    val isActive = state != ConnectionState.IDLE && state != ConnectionState.DISCONNECTED

    val smoothedAudio by animateFloatAsState(audioLevel, tween(80), label = "audio")
    val colorT by animateFloatAsState(if (isSpeaking) 1f else 0f, tween(800), label = "color")

    val speed = when {
        isConnecting -> 2.8f
        isSpeaking -> 1.6f + smoothedAudio * 2.0f
        else -> 1f
    }

    Canvas(modifier = modifier.size(320.dp)) {
        val cx = size.width / 2; val cy = size.height / 2
        val baseRadius = size.width * 0.34f
        val t = time * speed

        val breath = 1f + 0.012f * sin(time * 1.8f) + 0.008f * sin(time * 2.7f)
        val audioScale = 1f + smoothedAudio * 0.08f
        val orbRadius = baseRadius * breath * audioScale
        val center = Offset(cx, cy)
        val glowIntensity = glowPulse * if (isActive) 1f else 0.3f

        // LAYER 1: Deep ambient glow
        val deepGlow = theme.idleGlowColor.copy(a = 0.12f).lerp(theme.activeGlowColor.copy(a = 0.12f), colorT)
        val deepRadius = orbRadius * 2.8f
        drawCircle(
            brush = Brush.radialGradient(listOf(deepGlow.color, deepGlow.color.copy(alpha = 0.3f), Color.Transparent), center, deepRadius),
            radius = deepRadius, center = center, alpha = glowIntensity * 0.8f
        )

        // LAYER 2: Mid glow
        val midGlow = theme.idleGlowColor.lerp(theme.activeGlowColor, colorT)
        val midRadius = orbRadius * 1.8f
        drawCircle(
            brush = Brush.radialGradient(listOf(midGlow.color, midGlow.color.copy(alpha = 0.4f), Color.Transparent), center, midRadius),
            radius = midRadius, center = center, alpha = glowIntensity
        )

        // LAYER 3: Inner glow halo
        val innerGlow = theme.idleGlowColor.copy(a = 0.35f).lerp(theme.activeGlowColor.copy(a = 0.35f), colorT)
        val innerRadius = orbRadius * 1.35f
        drawCircle(
            brush = Brush.radialGradient(listOf(innerGlow.color, Color.Transparent), center, innerRadius),
            radius = innerRadius, center = center, alpha = glowIntensity * 1.2f
        )

        // LAYER 4: Rotating ring
        val ringColors = if (colorT > 0.5f) theme.activeRingColors else theme.idleRingColors
        val ringPulse = 0.5f + 0.15f * sin(time * 2f) + smoothedAudio * 0.35f
        drawCircle(Brush.sweepGradient(ringColors, center), orbRadius * 1.18f, center, alpha = 0.25f * glowIntensity)
        drawCircle(Brush.sweepGradient(ringColors, center), orbRadius * 1.06f, center, alpha = ringPulse * glowIntensity)

        // LAYER 5: Orb body
        val bgA = theme.idleOrbBg.lerp(theme.activeOrbBg, colorT)
        drawCircle(color = theme.orbDarkBase, radius = orbRadius, center = center)
        drawCircle(
            brush = Brush.radialGradient(listOf(bgA.color, theme.orbDarkBase), Offset(cx * 0.92f, cy * 0.85f), orbRadius),
            radius = orbRadius, center = center
        )

        // LAYER 6: Organic blobs (7 with compound oscillation)
        for (i in blobConfigs.indices) {
            val b = blobConfigs[i]
            val blobRadius = orbRadius * b.size * (1f + smoothedAudio * 0.25f)
            val px = sin(t * b.xFreq + b.xPhase) * b.xAmp + sin(t * b.x2Freq + b.xPhase * 2.1f) * b.x2Amp
            val py = cos(t * b.yFreq + b.yPhase) * b.yAmp + cos(t * b.y2Freq + b.yPhase * 1.7f) * b.y2Amp
            val bc = Offset(cx + px * orbRadius / 150f, cy + py * orbRadius / 150f)
            val blobColor = theme.idleBlobColors[i].lerp(theme.activeBlobColors[i], colorT)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to blobColor.color,
                        0.4f to blobColor.color.copy(alpha = blobColor.a * 0.6f),
                        0.75f to blobColor.color.copy(alpha = blobColor.a * 0.15f),
                        1f to Color.Transparent
                    ), center = bc, radius = blobRadius
                ),
                radius = blobRadius, center = bc, blendMode = BlendMode.Screen
            )
        }

        // LAYER 7: Audio-reactive energy waves
        if (smoothedAudio > 0.02f && isActive) {
            for (w in 0 until 3) {
                val wPhase = w * 2.1f + time * 3f
                val wRadius = orbRadius * (0.5f + smoothedAudio * 0.6f + sin(wPhase) * 0.15f)
                val wAlpha = smoothedAudio * 0.35f * (1f - w * 0.25f)
                val waveColor = theme.idleGlowColor.copy(a = wAlpha).lerp(theme.activeGlowColor.copy(a = wAlpha), colorT)
                drawCircle(
                    brush = Brush.radialGradient(listOf(waveColor.color, waveColor.color.copy(alpha = 0.3f), Color.Transparent), center, wRadius),
                    radius = wRadius, center = center, blendMode = BlendMode.Screen
                )
            }
        }

        // LAYER 8: Glass highlight
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to Color(0x4DFFFFFF), 0.35f to Color(0x14FFFFFF), 0.7f to Color.Transparent),
                center = Offset(cx * 0.78f, cy * 0.68f), radius = orbRadius * 0.65f
            ), radius = orbRadius, center = center
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x0FFFFFFF), Color.Transparent), Offset(cx * 1.15f, cy * 1.25f), orbRadius * 0.4f),
            radius = orbRadius, center = center
        )

        // LAYER 9: Edge vignette
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0.5f to Color.Transparent, 0.85f to Color(0x40000000), 1f to Color(0x8C000000)),
                center = center, radius = orbRadius
            ), radius = orbRadius, center = center
        )

        // LAYER 10: Rim light
        val rimColor = theme.idleGlowColor.copy(a = 0.30f).lerp(theme.activeGlowColor.copy(a = 0.30f), colorT)
        drawCircle(
            brush = Brush.radialGradient(listOf(Color.Transparent, rimColor.color, Color.Transparent), center, orbRadius * 1.25f),
            radius = orbRadius * 1.25f, center = center, alpha = 0.4f + smoothedAudio * 0.5f
        )

        // LAYER 11: Floating particles
        if (isActive) {
            for (p in 0 until 12) {
                val fp = p.toFloat()
                val angle = fp * (2f * PI.toFloat() / 12f) + time * (0.15f + fp * 0.02f)
                val dist = orbRadius * (1.15f + sin(time * 0.8f + fp * 0.7f) * 0.2f) + smoothedAudio * orbRadius * 0.15f
                val pSize = 1.5f + sin(time * 1.5f + fp * 1.3f) + smoothedAudio * 3f
                val pAlpha = (0.15f + 0.15f * sin(time * 2f + fp * 0.9f) + smoothedAudio * 0.3f).coerceIn(0f, 1f)
                val pColor = theme.idleGlowColor.copy(a = pAlpha).lerp(theme.activeGlowColor.copy(a = pAlpha), colorT)
                val pc = Offset(cx + cos(angle) * dist, cy + sin(angle) * dist)
                drawCircle(
                    brush = Brush.radialGradient(listOf(pColor.color, Color.Transparent), pc, pSize * 3f),
                    radius = pSize * 3f, center = pc, blendMode = BlendMode.Screen
                )
            }
        }

        // LAYER 12: Connecting spinner
        if (isConnecting) {
            for (d in 0 until 8) {
                val dAngle = d.toFloat() * (2f * PI.toFloat() / 8f) + time * 4f
                val dist = orbRadius * 1.22f
                val dotAlpha = (0.3f + 0.4f * (0.5f + 0.5f * sin(dAngle - time * 6f))).coerceIn(0f, 1f)
                val dc = Offset(cx + cos(dAngle) * dist, cy + sin(dAngle) * dist)
                drawCircle(
                    brush = Brush.radialGradient(listOf(theme.accentColor, Color.Transparent), dc, 3.dp.toPx()),
                    radius = 3.dp.toPx(), center = dc, alpha = dotAlpha
                )
            }
        }
    }
}
