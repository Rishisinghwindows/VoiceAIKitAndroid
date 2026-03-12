package com.ubudy.voiceaikit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    phrases: List<String>,
    cursorColor: Color = Color(0x80A78BFA),
    modifier: Modifier = Modifier
) {
    var displayedText by remember { mutableStateOf("") }
    var phraseIndex by remember { mutableIntStateOf(0) }
    var charIndex by remember { mutableIntStateOf(0) }
    var isDeleting by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )

    LaunchedEffect(phraseIndex, isDeleting) {
        val phrase = phrases[phraseIndex]
        if (isDeleting) {
            while (displayedText.isNotEmpty()) {
                delay(20)
                displayedText = displayedText.dropLast(1)
            }
            isDeleting = false
            phraseIndex = (phraseIndex + 1) % phrases.size
            charIndex = 0
        } else {
            while (charIndex < phrase.length) {
                delay(40)
                displayedText = phrase.substring(0, charIndex + 1)
                charIndex++
            }
            delay(3000)
            isDeleting = true
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(20.dp)
    ) {
        Text(
            text = displayedText,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = Color(0x59FFFFFF)
            )
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(14.dp)
                .background(cursorColor.copy(alpha = cursorAlpha))
        )
    }
}
