package dev.cirtellock.applock.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimaryLight,
    onPrimary = OnOrangePrimaryLight,
    primaryContainer = OrangePrimaryContainerLight,
    onPrimaryContainer = OnOrangePrimaryContainerLight,
    secondary = OrangeSecondaryLight,
    onSecondary = OnOrangeSecondaryLight,
    secondaryContainer = OrangeSecondaryContainerLight,
    onSecondaryContainer = OnOrangeSecondaryContainerLight,
    tertiary = OrangeTertiaryLight,
    onTertiary = OnOrangeTertiaryLight,
    tertiaryContainer = OrangeTertiaryContainerLight,
    onTertiaryContainer = OnOrangeTertiaryContainerLight,
    error = OrangeErrorLight,
    onError = OnOrangeErrorLight,
    errorContainer = OrangeErrorContainerLight,
    onErrorContainer = OnOrangeErrorContainerLight,
    background = OrangeBackgroundLight,
    onBackground = OnOrangeBackgroundLight,
    surface = OrangeSurfaceLight,
    onSurface = OnOrangeSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = OnOrangePrimaryDark,
    primaryContainer = OrangePrimaryContainerDark,
    onPrimaryContainer = OnOrangePrimaryContainerDark,
    secondary = OrangeSecondaryDark,
    onSecondary = OnOrangeSecondaryDark,
    secondaryContainer = OrangeSecondaryContainerDark,
    onSecondaryContainer = OnOrangeSecondaryContainerDark,
    tertiary = OrangeTertiaryDark,
    onTertiary = OnOrangeTertiaryDark,
    tertiaryContainer = OrangeTertiaryContainerDark,
    onTertiaryContainer = OnOrangeTertiaryContainerDark,
    error = OrangeErrorDark,
    onError = OnOrangeErrorDark,
    errorContainer = OrangeErrorContainerDark,
    onErrorContainer = OnOrangeErrorContainerDark,
    background = OrangeBackgroundDark,
    onBackground = OnOrangeBackgroundDark,
    surface = OrangeSurfaceDark,
    onSurface = OnOrangeSurfaceDark,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppLockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default to favor the orange brand theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val shapes = Shapes(largeIncreased = RoundedCornerShape(36.0.dp))

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = shapes,
        content = content,
        motionScheme = MotionScheme.expressive()
    )
}
