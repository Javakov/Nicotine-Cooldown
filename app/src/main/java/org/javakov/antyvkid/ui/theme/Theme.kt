package org.javakov.antyvkid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SnusDarkScheme = darkColorScheme(
    primary = MintCore,
    onPrimary = MidnightDeep,
    primaryContainer = MintGlow,
    onPrimaryContainer = MidnightDeep,
    secondary = CyanGlow,
    onSecondary = MidnightDeep,
    tertiary = VioletGlow,
    onTertiary = MidnightDeep,
    background = MidnightDeep,
    onBackground = InkPrimary,
    surface = MidnightCore,
    onSurface = InkPrimary,
    surfaceVariant = MidnightSoft,
    onSurfaceVariant = InkSecondary,
    outline = GlassStroke,
    outlineVariant = GlassStrokeSoft,
    error = CrimsonAlert,
    onError = MidnightDeep
)

@Composable
fun AntyVkidTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }
    // Suppress unused warning on context (kept so dynamicColor can be re-introduced later)
    @Suppress("UNUSED_VARIABLE") val ctx = LocalContext.current
    @Suppress("UNUSED_VARIABLE") val sdk = Build.VERSION.SDK_INT

    MaterialTheme(
        colorScheme = SnusDarkScheme,
        typography = Typography,
        content = content
    )
}
