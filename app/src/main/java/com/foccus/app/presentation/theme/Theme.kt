package com.foccus.app.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FoccusDarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryPurple,
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = TertiaryBlue,
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = Color(0xFFCCCCCC),
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onError = Color(0xFF1A1A1A),
    onErrorContainer = OnErrorContainer,
    background = BackgroundDark,
    onBackground = OnBackground,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = PrimaryTealDark,
    surfaceTint = Accent,
    scrim = Color(0xFF000000)
)

@Composable
fun FoccusTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FoccusDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FoccusTypography,
        content = content
    )
}
