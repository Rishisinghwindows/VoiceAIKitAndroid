package com.ubudy.voiceaikit.models

import androidx.compose.ui.graphics.Color

data class RGBA(val r: Float, val g: Float, val b: Float, val a: Float) {

    val color: Color get() = Color(r, g, b, a)

    fun lerp(to: RGBA, t: Float): RGBA = RGBA(
        r = r + (to.r - r) * t,
        g = g + (to.g - g) * t,
        b = b + (to.b - b) * t,
        a = a + (to.a - a) * t
    )

    fun copy(a: Float): RGBA = RGBA(r, g, b, a)
}
