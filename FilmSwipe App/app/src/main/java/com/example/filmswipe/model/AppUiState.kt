package com.example.filmswipe.model

data class AppUiState(
    //Login
    val incorrectLogin:Boolean = false,
    val isLoggedIn:Boolean = false,
    val loggedInEmail:String = "",
    val loggedInUsername:String = "",

    //Signup
    val incorrectSignUp:Boolean = false,
    val isSignedUp:Boolean = false,

    //Navbar
    val navSelectedItem:Int = 0,
    val navScreenTitle:String = "",

    //Notifications
    val enableNotifs:Boolean = true,
    val darkMode:Boolean = false,
)