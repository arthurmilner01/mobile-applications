package com.example.filmswipe.model

data class AppUiState(
    //Login
    val incorrectLogin:Boolean = false, //If user inputs incorrect credentials
    val isLoggedIn:Boolean = false, //If user is logged in
    val loggedInEmail:String = "", //Email of logged in user
    val loggedInUsername:String = "", //Username of logged in user
    val loggedInUID:String = "", //User ID of logged in user

    //Signup
    val isSignedUp:Boolean = false, //When user successfully signs up
    val signUpEmailError: String? = "", //To display errors regarding signing-up to the user
    val signUpUsernameError: String? = "", //To display errors regarding signing-up to the user
    val signUpPasswordError: String? = "", //To display errors regarding signing-up to the user

    //Change Password
    val viewingChangePassword:Boolean = false, //If user is viewing change password
    val changePasswordCurrentPasswordError: String? = "", //To display errors regarding changing password to the user
    val changePasswordPasswordError: String? = "", //To display errors regarding changing password to the user
    val changePasswordConfirmPasswordError: String? = "", //To display errors regarding changing password to the user
    val isUpdatedPasswordSuccess:Boolean = false, //If change password was successful


    //Navbars
    val navSelectedItem:Int = 0, //Currently selected bottom nav icon
    val navScreenTitle:String = "", //Title to display on top nav

    //Home
    val currentMovieID:Int = 0, //Currently displayed movie ID
    val currentMovieTitle:String = "", //Currently displayed movie title
    val currentMovieOverview:String = "", //Currently displayed movie overview
    val currentMoviePosterPath:String? = "", //Currently displayed movie poster path
    val watchProviderFilter:String = "", //Streaming service filter passed to API calls
    val netflixFilter:Boolean = false, //For netflix filter switch
    val primeFilter:Boolean = false, //For prime filter switch
    val disneyFilter:Boolean = false, //For disney filter switch
    val viewingHome:Boolean = false, //If user is viewing home
    val filterMenuExpanded:Boolean = false, //If filter menu is expanded
    val movieInWatchlist:Boolean = false, //If currently displayed movie is in watchlist
    val movieInWatched:Boolean = false, //If currently displayed movie is in watched

    //Profile
    val viewingWatchedMovies:Boolean = false, //If user is viewing watched movies or watchlist movies

    //Movie Details
    val viewingMovieDetails:Boolean = false, //If user is viewing movie details
    val viewingMovieCrew:Boolean = false, //If user is viewing movie cast or crew
    val currentMovieGenres: List<String> = emptyList(), //Movie genres of currently viewed movie
    val currentMovieIMDBRating: Double = 0.0, //Review score of currently viewed movie

    //Search
    val searchingUsers:Boolean = false //If user is searching for users or films

)