package com.example.filmswipe.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.filmswipe.data.*
import com.example.filmswipe.network.*
import com.google.android.gms.common.api.Response
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    //API
    val apiKey = "bee0c37b9c1a2d1c1ecf80b6cce631a5"

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> get() = _movies

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error



    fun fetchPopularMovies() {
        _loading.value = true
        viewModelScope.launch {
            try {
                // Call the method from the API service
                val response = RetrofitInstance.api.getPopularMovies(apiKey)

                if (response.isSuccessful) {
                    // Safely access the results from the response
                    _movies.postValue(response.body()?.results ?: emptyList())
                    _error.postValue(null) // Clear any previous errors
                } else {
                    _error.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Exception: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    //User input
    //Log In
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    //Sign Up
    var signUpEmailInput by mutableStateOf("")
    var signUpUsernameInput by mutableStateOf("")
    var signUpPasswordInput by mutableStateOf("")
    // Search
    var searchText by mutableStateOf("")


    //Log In funcs
    fun updateEmailInput(currentEmailInput:String){
        emailInput = currentEmailInput
    }

    fun updatePasswordInput(currentPasswordInput:String){
        passwordInput = currentPasswordInput
    }

    fun checkLoginDetails(){
        //TODO: Use database for validation
        if((emailInput.equals("arthur@email.com", ignoreCase=true)) && (passwordInput == "password")){
            userLogsIn(currentEmailInput = emailInput)
        }
        else{
            _uiState.update{
                    currentState -> currentState.copy(
                incorrectLogin = true,
                isLoggedIn = false
            )
            }
        }
    }

    fun userLogsOut(){
        _uiState.update{
                currentState -> currentState.copy(
            isLoggedIn = false,
            loggedInEmail = "",
            loggedInUsername = "",
            incorrectLogin = false
        )
        }
    }

    fun newSignUp(){
        _uiState.update{
                currentState -> currentState.copy(
            isSignedUp = false
        )
        }
    }

    private fun userLogsIn(currentEmailInput:String){
        _uiState.update{
            currentState -> currentState.copy(
                isLoggedIn = true,
                loggedInEmail = currentEmailInput,
                loggedInUsername = "tempusername", //TODO: Use username when implemented
                incorrectLogin = false
        )
        }
        emailInput = ""
        passwordInput = ""
    }

    //Sign Up funcs
    fun updateSignUpEmailInput(currentEmailInput:String){
        signUpEmailInput = currentEmailInput
    }

    fun updateSignUpPasswordInput(currentPasswordInput:String){
        signUpPasswordInput = currentPasswordInput
    }

    fun updateSignUpUsernameInput(currentUsernameInput:String){
        signUpUsernameInput = currentUsernameInput
    }

    fun checkSignUpDetails(){
        if(true) { //TODO: Database validation if user doesn't exist
            _uiState.update { currentState ->
                currentState.copy(
                    incorrectSignUp = false
                )
            }
            userSignsUp()
        }
        else{
            _uiState.update { currentState ->
                currentState.copy(
                    incorrectSignUp = true
                )
            }
        }
    }

    private fun userSignsUp(){
        //TODO: Add user to database

        signUpEmailInput = ""
        signUpPasswordInput = ""
        signUpUsernameInput = ""
        emailInput = ""
        passwordInput = ""

        _uiState.update { currentState ->
            currentState.copy(
                incorrectSignUp = false,
                isSignedUp = true
            )
        }
    }

    //Home page funcs
    //Swipe left
    fun removeMovie(index: Int) {
        val currentList = _movies.value?.toMutableList() ?: return
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _movies.value = currentList // Update the LiveData
        }
    }
    //Swipe right
    fun removeLikedMovie(index: Int) {
        //TODO: Save to watchlist in db before removing
        val currentList = _movies.value?.toMutableList() ?: return
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _movies.value = currentList // Update the LiveData
        }
    }
    //Swipe up
    fun removeWatchedMovie(index: Int){
        //TODO: Save as watched movie in db before removing
        val currentList = _movies.value?.toMutableList() ?: return
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _movies.value = currentList // Update the LiveData
        }
    }

    //Stores current movie to swipe details
    fun getCurrentMovie(movieTitle:String, movieOverview:String, moviePosterPath:String?){
        _uiState.update{
                currentState -> currentState.copy(
                    currentMovieTitle =  movieTitle,
                    currentMovieOverview = movieOverview,
                    currentMoviePosterPath = moviePosterPath
        )
        }
    }

    //Profile page funs
    fun showProfilesLikedMovies(){
        _uiState.update{
                currentState -> currentState.copy(
            viewingWatchedMovies = true
        )
        }
    }

    fun showProfilesWatchlist(){
        _uiState.update{
                currentState -> currentState.copy(
            viewingWatchedMovies = false
        )
        }
    }

    //Settings page funcs

    fun updateNotifSetting(notifCheckInput: Boolean){
        //TODO: Make this actually change notif settings on phone
        _uiState.update{
                currentState -> currentState.copy(
            enableNotifs =  notifCheckInput
        )
        }
    }

    fun updateDarkModeSetting(darkModeInput: Boolean){
        //TODO: Make this set dark mode on/off
        _uiState.update{
                currentState -> currentState.copy(
            darkMode =  darkModeInput
        )
        }
    }

    //Top bar funcs
    //Used to map nav host title to the displayed title
    private val screenTitles = mapOf(
        "homescreen" to "Home",
        "profilescreen" to "Profile",
        "settingsscreen" to "Settings",
        "loginscreen" to "Login",
        "searchscreen" to "Search",
        "moviedetailsscreen" to ""
    )

    fun getScreenTitle(navController: NavController){
        val currentScreen = navController.currentBackStackEntry?.destination?.route
        _uiState.update{
            currentState -> currentState.copy(
            navScreenTitle =  screenTitles[currentScreen] ?: " " //Empty title if no title
        )
        }
        if(screenTitles[currentScreen] == ""){
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  true
            )
            }
        }
        else
        {
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false
            )
            }
        }
    }

    //Bottom bar funcs
    fun changeNavSelectedItem(index:Int){
        _uiState.update{
            currentState -> currentState.copy(
                navSelectedItem = index
        )}
    }


    //Search Funcs
    fun updateSearchQuery(newSearchText:String){
        searchText = newSearchText
    }


    fun performSearch() {
        val currentQuery = searchText
        //TODO: Get Film Data from api using query
    }

}

