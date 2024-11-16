package com.example.filmswipe.model

data class AppUiState(
    //Login
    val incorrectLogin:Boolean = false,
    val isLoggedIn:Boolean = false,
    val loggedInEmail:String = "",
    val loggedInUsername:String = "",
    val loggedInUID:String = "",

    //Signup
    val isSignedUp:Boolean = false,
    val signUpEmailError: String? = "",
    val signUpUsernameError: String? = "",
    val signUpPasswordError: String? = "",

    //Change Password
    val viewingChangePassword:Boolean = false,
    val changePasswordCurrentPasswordError: String? = "",
    val changePasswordPasswordError: String? = "",
    val changePasswordConfirmPasswordError: String? = "",
    val isUpdatedPasswordSuccess:Boolean = false,


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
    val movieInWatchlist:Boolean = false,
    val movieInWatched:Boolean = false,

    //Profile
    val viewingWatchedMovies:Boolean = false,

    //Movie Details
    val viewingMovieDetails:Boolean = false,
    val viewingMovieCrew:Boolean = false,
    val currentMovieGenres: List<String> = emptyList(),
    val currentMovieIMDBRating: Double = 0.0,

    //Search
    val searchingUsers:Boolean = false

)