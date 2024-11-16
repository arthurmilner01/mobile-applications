package com.example.filmswipe.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
import com.example.filmswipe.screens.*

@Composable
fun AppNavigator(modifier: Modifier = Modifier, navController: NavController, appViewModel: AppViewModel){
    //Init navhost
    NavHost(
        navController = navController as NavHostController,
        //TODO: Check if user logged in to decide start destination???
        startDestination = "loginscreen",
        modifier= modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    )
    {
        composable("loginscreen",
            //Animation when navigating
            exitTransition = {
                slideOutOfContainer(
                    //Will slide down on navigation
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    //Animation speed
                    animationSpec = tween(700)
                )
            })
        { LoginScreen(navController, appViewModel, modifier) }
        composable("signupscreen") { SignUpScreen(navController, appViewModel, modifier) }
        composable("homescreen") { HomeScreen(navController, appViewModel, modifier) }
        composable("profilescreen/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            ProfileScreen(navController, appViewModel, modifier, email)
        }
        composable("profilescreen") { ProfileScreen(navController, appViewModel, modifier) }
        composable("settingsscreen") { SettingsScreen(navController, appViewModel, modifier) }
        composable("searchscreen") { SearchScreen(navController, appViewModel, modifier) }
        composable("moviedetailsscreen",
            //Animation when navigating to the movie details screen
            enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )},
            //Animation when exiting the movie details screen
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            }) { MovieDetailsScreen(navController, appViewModel, modifier)}
        composable("changepasswordscreen") { ChangePasswordScreen(navController, appViewModel, modifier) }

    }
}