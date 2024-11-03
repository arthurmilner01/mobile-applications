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
import kotlin.random.Random

class AppViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

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
    var searchCheckbox by mutableStateOf(false)

    //API
    val apiKey = "bee0c37b9c1a2d1c1ecf80b6cce631a5"

    //Live data object for movies
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> get() = _movies

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    //Live data object for cast
    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> get() = _cast


    //Live data for crew
    private val _crew = MutableLiveData<List<CrewMember>>()
    val crew: LiveData<List<CrewMember>> get() = _crew

    private val _creditsLoading = MutableLiveData<Boolean>()
    val creditsLoading: LiveData<Boolean> get() = _creditsLoading

    private val _creditsError = MutableLiveData<String?>()
    val creditsError: LiveData<String?> get() = _creditsError


    //Live data object for searching
    private val _searchResults = MutableLiveData<List<Movie>>()
    val searchResults: LiveData<List<Movie>> get() = _searchResults



    fun fetchPopularMovies() {
        _loading.value = true
        viewModelScope.launch {
            try {
                //API call with random page number
                //TODO: TEST IF THIS IS A VALID RANGE
                val pageNumber = Random.nextInt(1,500)
                val response = RetrofitInstance.api.getPopularMovies(apiKey, pageNumber)

                if (response.isSuccessful) {
                    _movies.postValue(response.body()?.results ?: emptyList())
                    _error.postValue(null)
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

    fun fetchMovieCredits(movieId: Int) {
        _creditsLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieCredits(movieId, apiKey) // Adjust your API call as needed
                if (response.isSuccessful) {
                    response.body()?.let { credits ->
                        _cast.postValue(credits.cast) // Update cast LiveData
                        // You can also create a new LiveData for crew or handle it as needed
                        _crew.postValue(credits.crew) // If you have a separate LiveData for crew
                        _creditsError.postValue(null) // Clear any previous errors
                    }
                } else {
                    _creditsError.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _creditsError.postValue("Exception: ${e.message}")
            } finally {
                _creditsLoading.postValue(false)
            }
        }
    }

    fun searchMoviesByTitle() {
        if (searchText.isBlank()) {
            _searchResults.postValue(emptyList())
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchMoviesByTitle(apiKey, searchText)

                if (response.isSuccessful) {
                    _searchResults.postValue(response.body()?.results ?: emptyList())
                    _error.postValue(null)
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
    fun getCurrentMovie(movieID:Int, movieTitle:String, movieOverview:String, moviePosterPath:String?){
        _uiState.update{
                currentState -> currentState.copy(
                    currentMovieID = movieID,
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
        _searchResults.postValue(emptyList())
    }

    fun performUserSearch() {
        val currentQuery = searchText
        //TODO: Get users
    }

    //Toggle box changes search type
    fun changeSearchType(checkBoxValue:Boolean){
        if(checkBoxValue){
            _uiState.update{
                    currentState -> currentState.copy(
                searchingUsers = true
            )}
            searchText = ""
            _searchResults.postValue(emptyList())
        }
        else{
            _uiState.update{
                    currentState -> currentState.copy(
                searchingUsers = false
            )}
            searchText = ""
            _searchResults.postValue(emptyList())
        }
    }

    //Movie details funcs
    fun showMovieCrew(){
        _uiState.update{
                currentState -> currentState.copy(
            viewingMovieCrew = true
        )
        }
    }

    fun showMovieCast(){
        _uiState.update{
                currentState -> currentState.copy(
            viewingMovieCrew = false
        )
        }
    }

}

