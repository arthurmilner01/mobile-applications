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

    //Home
    val currentMovieID:Int = 0,
    val currentMovieTitle:String = "",
    val currentMovieOverview:String = "",
    val currentMoviePosterPath:String? = "",
    val watchProviderFilter:String = "",
    val netflixFilter:Boolean = false,
    val primeFilter:Boolean = false,
    val disneyFilter:Boolean = false,
    val viewingHome:Boolean = false,
    val filterMenuExpanded:Boolean = false,

    //Profile
    val viewingWatchedMovies:Boolean = false,

    //Movie Details
    val viewingMovieDetails:Boolean = false,
    val viewingMovieCrew:Boolean = false,

    //Search
    val searchingUsers:Boolean = false

)