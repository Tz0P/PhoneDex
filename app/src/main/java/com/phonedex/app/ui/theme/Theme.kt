package com.phonedex.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF4355B9), onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE0FF), onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF5B5D72), secondaryContainer = Color(0xFFE0E0F9),
    tertiary = Color(0xFF77536D), tertiaryContainer = Color(0xFFFFD7F1),
    background = Color(0xFFFBF8FF), surface = Color(0xFFFBF8FF),
    surfaceVariant = Color(0xFFE3E1EC), onSurface = Color(0xFF1B1B21),
    onSurfaceVariant = Color(0xFF46464F), outline = Color(0xFF777680)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBAC3FF), onPrimary = Color(0xFF08218A),
    primaryContainer = Color(0xFF293CA0), onPrimaryContainer = Color(0xFFDEE0FF),
    secondary = Color(0xFFC3C5DD), secondaryContainer = Color(0xFF434559),
    tertiary = Color(0xFFE6BAD7), tertiaryContainer = Color(0xFF5D3C55),
    background = Color(0xFF121318), surface = Color(0xFF121318),
    surfaceVariant = Color(0xFF46464F), onSurface = Color(0xFFE4E1E9),
    onSurfaceVariant = Color(0xFFC7C5D0), outline = Color(0xFF90909A)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun PhoneDexTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= 31 ->
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = scheme, shapes = AppShapes, content = content)
}
