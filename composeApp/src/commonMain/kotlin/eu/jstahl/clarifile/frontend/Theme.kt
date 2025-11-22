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

// Light color scheme
private val LightColorScheme = lightColorScheme()

// Dark color scheme
private val DarkColorScheme = darkColorScheme()

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

@Stable
class ThemeState {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
    
    @Composable
    fun isDarkMode(): Boolean {
        val systemInDarkTheme = isSystemInDarkTheme()
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> systemInDarkTheme
        }
    }
    
    @Composable
    fun toggle() {
        val systemInDarkTheme = isSystemInDarkTheme()
        themeMode = when (themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> if (systemInDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
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
