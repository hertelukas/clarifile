package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF315DA8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7E2FF),
    onPrimaryContainer = Color(0xFF0B2A66),

    secondary = Color(0xFF566784),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E2F2),
    onSecondaryContainer = Color(0xFF12233E),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE7EAF1),
    onSurfaceVariant = Color(0xFF42474E),
)

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAEC6FF),
    onPrimary = Color(0xFF0B2A66),
    primaryContainer = Color(0xFF274783),
    onPrimaryContainer = Color(0xFFD7E2FF),

    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF22324E),
    secondaryContainer = Color(0xFF3B4A67),
    onSecondaryContainer = Color(0xFFD9E2F2),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E3E3),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2C3036),
    onSurfaceVariant = Color(0xFFC3C7CE),
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

@Stable
class ThemeState {
    private var _themeMode by mutableStateOf(ThemeMode.SYSTEM)
    val themeMode: ThemeMode
        get() = _themeMode
    
    @Composable
    fun isDarkMode(): Boolean {
        val systemInDarkTheme = isSystemInDarkTheme()
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> systemInDarkTheme
        }
    }
    
    fun toggle(currentSystemTheme: Boolean) {
        _themeMode = when (_themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> if (currentSystemTheme) ThemeMode.LIGHT else ThemeMode.DARK
        }
    }
}

@Composable
fun AppTheme(
    themeState: ThemeState = remember { ThemeState() },
    content: @Composable () -> Unit
) {
    val isDarkMode = themeState.isDarkMode()
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
