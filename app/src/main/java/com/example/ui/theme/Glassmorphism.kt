package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cohesive Glassmorphism Design System Tokens & Modifiers.
 */
object GlassTokens {
    // Glass Surface Background Gradients
    val GlassSurfaceGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x383D4758),
            Color(0x1F1E222C)
        )
    )

    val GlassSurfaceHoverGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x504D596D),
            Color(0x2E2A323E)
        )
    )

    val GlassSurfaceActiveGradient = Brush.linearGradient(
        colors = listOf(
            Color(0x52D0E4FF),
            Color(0x263D4758)
        )
    )

    val GlassSurfaceUltraThin = Brush.verticalGradient(
        colors = listOf(
            Color(0x22FFFFFF),
            Color(0x08FFFFFF)
        )
    )

    val GlassSurfaceSheet = Brush.verticalGradient(
        colors = listOf(
            Color(0xE62A2E37),
            Color(0xF5181A20)
        )
    )

    val GlassSurfaceDialog = Brush.verticalGradient(
        colors = listOf(
            Color(0xEB2E333E),
            Color(0xF81B1D24)
        )
    )

    val GlassSurfaceFloatingDock = Brush.verticalGradient(
        colors = listOf(
            Color(0xD8282D38),
            Color(0xEA16181F)
        )
    )

    // Specular Highlight Borders (Simulating directional ambient refraction)
    val GlassBorderSpecular = Brush.verticalGradient(
        colors = listOf(
            Color(0x45FFFFFF),
            Color(0x1AFFFFFF),
            Color(0x06FFFFFF)
        )
    )

    val GlassBorderNeon = Brush.verticalGradient(
        colors = listOf(
            Color(0x99D0E4FF),
            Color(0x40D0E4FF),
            Color(0x15D0E4FF)
        )
    )

    val GlassBorderSubtle = Brush.verticalGradient(
        colors = listOf(
            Color(0x28FFFFFF),
            Color(0x0AFFFFFF)
        )
    )

    val GlassBorderActive = Brush.horizontalGradient(
        colors = listOf(
            Color(0xAFD0E4FF),
            Color(0x60BACDE5),
            Color(0x30D0E4FF)
        )
    )

    // Soft Tint Overlays
    val GlassTintIce = Color(0x1FD0E4FF)
    val GlassTintPink = Color(0x1AF28B82)
    val GlassTintPurple = Color(0x1A7A8FA6)
}

/**
 * Applies a rich frosted glassmorphic card styling with specular refraction border.
 */
fun Modifier.glassmorphicCard(
    shape: Shape = RoundedCornerShape(22.dp),
    borderBrush: Brush = GlassTokens.GlassBorderSpecular,
    borderWidth: Dp = 1.dp,
    backgroundBrush: Brush = GlassTokens.GlassSurfaceGradient,
    elevation: Dp = 14.dp,
    spotColor: Color = Color(0x45000000)
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        spotColor = spotColor,
        ambientColor = Color.Black
    )
    .clip(shape)
    .background(backgroundBrush)
    .border(borderWidth, borderBrush, shape)

/**
 * Applies a glowing active glassmorphic styling (for selected tabs, active items, current track).
 */
fun Modifier.glassmorphicActive(
    shape: Shape = RoundedCornerShape(20.dp),
    glowColor: Color = EditorialIceBlue,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 16.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        spotColor = glowColor.copy(alpha = 0.4f),
        ambientColor = Color.Black
    )
    .clip(shape)
    .background(GlassTokens.GlassSurfaceActiveGradient)
    .border(borderWidth, GlassTokens.GlassBorderNeon, shape)

/**
 * Applies a sleek glassmorphic pill style (for filter chips, status badges, tiny controls).
 */
fun Modifier.glassmorphicPill(
    shape: Shape = RoundedCornerShape(50),
    isActive: Boolean = false,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        if (isActive) GlassTokens.GlassSurfaceActiveGradient else GlassTokens.GlassSurfaceUltraThin
    )
    .border(
        borderWidth,
        if (isActive) GlassTokens.GlassBorderNeon else GlassTokens.GlassBorderSubtle,
        shape
    )
