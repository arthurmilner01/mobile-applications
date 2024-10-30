package com.example.filmswipe.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.filmswipe.model.AppViewModel
import com.example.filmswipe.screens.HomeScreen
import com.example.filmswipe.screens.LoginScreen
import com.example.filmswipe.screens.ProfileScreen
import com.example.filmswipe.screens.SearchScreen
import com.example.filmswipe.screens.SettingsScreen
import com.example.filmswipe.screens.SignUpScreen

@Composable
fun AppNavigator(modifier: Modifier = Modifier, navController: NavController, appViewModel: AppViewModel){
    NavHost(
        navController = navController as NavHostController,
        startDestination = "loginscreen",
        modifier= modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    )
    {
        composable("loginscreen") { LoginScreen(navController, appViewModel, modifier) }
        composable("signupscreen") { SignUpScreen(navController, appViewModel, modifier) }
        composable("homescreen") { HomeScreen(navController, appViewModel, modifier) }
        composable("profilescreen") { ProfileScreen(navController, appViewModel, modifier) }
        composable("settingsscreen") { SettingsScreen(navController, appViewModel, modifier) }
        composable("searchscreen") { SearchScreen(navController, appViewModel, modifier) }
    }
}