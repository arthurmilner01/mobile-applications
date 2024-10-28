package com.example.filmswipe.model

data class AppUiState(
    val incorrectLogin:Boolean = false,
    val isLoggedIn:Boolean = false,
    val loggedInEmail:String = "",
    val loggedInUsername:String = "",
    val navSelectedItem:Int = 0,
    val navScreenTitle:String = "",
    val enableNotifs:Boolean = true,
    val darkMode:Boolean = false
)