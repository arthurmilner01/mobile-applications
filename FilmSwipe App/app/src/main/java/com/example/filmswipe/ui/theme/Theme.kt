package com.example.filmswipe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.filmswipe.model.AppViewModel

//Colours which are used if phone is in dark mode
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3b068f),
    secondary = Color(0xFFFF5733),
    tertiary = Color(0xFFAD8D2E),
    background = Color(0xFF060a2e),
    surface = Color(0xFF222d5a),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

//Colours which are used if phone is in light mode
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF62568b),
    secondary = Color(0xFFFF5733),
    tertiary = Color(0xFF775460),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFf2edf6),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun FilmSwipeTheme(
    //Check if phone is in dark mode or not
    darkTheme: Boolean = isSystemInDarkTheme(),
    navController: NavController,
    appViewModel: AppViewModel,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}