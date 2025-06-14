package com.example.filmswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.filmswipe.model.AppViewModel
import com.example.filmswipe.navigation.AppNavigator
import com.example.filmswipe.navigation.BottomNavigationBar
import com.example.filmswipe.navigation.TopNavigationBar
import com.example.filmswipe.ui.theme.FilmSwipeTheme

//Sources:
//For API
//https://medium.com/@kathankraithatha/how-to-use-api-in-jetpack-compose-10d11b8f166f

//API reference
//https://developer.themoviedb.org/v4/reference/intro/getting-started

//Reference for string matching with firebase
//https://inorganik.medium.com/implementing-a-simple-effective-search-in-firebase-with-just-firestore-957dd716ccdb

//For firebase authentication/user login/logoff/sign-up, we adapted this to kotlin
//https://firebase.flutter.dev/docs/auth/usage/

//For using device camera
//https://www.youtube.com/watch?v=s1WXrB9fv8Q
//https://www.youtube.com/watch?v=UqNnpt3OfhE

//For opening link in web browser
//https://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application

//For shared preferences
//https://blog.mansi.dev/difference-between-androidviewmodel-and-viewmodel

//For email validation
//https://regexr.com/3e48o

//For getting images as base64 for insertion into firebase
//https://stackoverflow.com/questions/9224056/android-bitmap-to-base64-string

//For converting base64 to a bitmap image for displaying in the app
//https://stackoverflow.com/questions/4837110/how-to-convert-a-base64-string-into-a-bitmap-image-to-show-it-in-a-imageview

//AndroidViewModel application context for persistent storage using SharedPreferences
//https://blog.mansi.dev/difference-between-androidviewmodel-and-viewmodel

//For handling back arrow/back swiping with BackHandler{}
//https://stackoverflow.com/questions/68560948/how-to-handle-back-navigation-with-jetpack-compose-navigation-without-fragmen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //Create instance of nav controller and view model
            val navController = rememberNavController()
            val appViewModel: AppViewModel = viewModel()

            FilmSwipeTheme(navController = navController, appViewModel = appViewModel){
                //Restoring login session with SharedPreferences
                LaunchedEffect(Unit) {
                    appViewModel.restoreLoginState()
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    //Passing nav controller/view model to top and bottom nav bars
                    topBar = { TopNavigationBar(navController, appViewModel)},
                    bottomBar = {BottomNavigationBar(navController, appViewModel)}) { innerPadding ->
                    Box(modifier=Modifier.padding(innerPadding)) {
                        //Passing nav controller and app view model to app navigator/navhost
                        AppNavigator(
                            modifier = Modifier,
                            navController = navController,
                            appViewModel = appViewModel
                        )
                    }
                }
            }
        }
    }
}
