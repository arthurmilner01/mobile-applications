package com.example.filmswipe.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavController
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel

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

private val LightColorScheme = lightColorScheme(
    //TODO: Change to lighter colours
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

@Composable
fun FilmSwipeTheme(
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
        typography = Typography, //TODO: Default typography, can change later
        content = content
    )
}