package com.example.filmswipe.model

import android.util.Log
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
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
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

    //Database
    //Auth firebase instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    //Firebase db instance
    private val db = Firebase.firestore

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

    //Live data object for user searching
    private val _userSearchResults = MutableLiveData<List<FilmswipeUser>>()
    val userSearchResults: LiveData<List<FilmswipeUser>> = _userSearchResults



    fun fetchPopularMovies() {
        _loading.value = true
        viewModelScope.launch {
            try {
                //API call with random page number
                //TODO: TEST IF THIS IS A VALID RANGE
                val pageNumber = Random.nextInt(1,10)
                val response = RetrofitInstance.api.getPopularMovies(apiKey, pageNumber, _uiState.value.watchProviderFilter)

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
        //If either email or password blank
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _uiState.update {
                currentState -> currentState.copy(
                incorrectLogin = true,
                isLoggedIn = false
                )
            }
        }

        auth.signInWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login successful
                    val user = auth.currentUser
                    //Sends username and email to user logs in function
                    user?.let {
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                val username = document.getString("username") ?: "Unknown User"
                                userLogsIn(user.email ?: "", username, user.uid)
                            }
                            .addOnFailureListener { exception ->
                                _error.postValue("Failed to retrieve username: ${exception.message}")
                            }
                    }
                } else {
                    // Login failed
                    _uiState.update {
                        currentState -> currentState.copy(
                        incorrectLogin = true,
                        isLoggedIn = false
                        )}
                    _error.postValue("Login failed: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Exception: ${exception.message}")
            }
            .addOnCompleteListener {
                _loading.postValue(false)
            }
    }

    fun userLogsOut(){
        _uiState.update{
                currentState -> currentState.copy(
            isLoggedIn = false,
            loggedInEmail = "",
            loggedInUsername = "",
            loggedInUID = "",
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

    private fun userLogsIn(currentEmailInput:String, currentUsernameInput: String, userUID: String){
        _uiState.update{
            currentState -> currentState.copy(
                isLoggedIn = true,
                loggedInEmail = currentEmailInput,
                loggedInUsername =  currentUsernameInput,
                loggedInUID = userUID,
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

        auth.createUserWithEmailAndPassword(signUpEmailInput, signUpPasswordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //If account is unique will add to firebase
                    val user = auth.currentUser
                    user?.let {
                        //Maps username and email input for insertion into user collection
                        val userDetails = mapOf(
                            "username" to signUpUsernameInput,
                            "email" to signUpEmailInput
                        )
                        //Adds user info to firebase
                        db.collection("users").document(user.uid).set(userDetails)
                            .addOnSuccessListener {
                                //Update UI and text inputs
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
                            .addOnFailureListener { exception ->
                                _error.postValue("Failed to save user info: ${exception.message}")
                            }
                    }
                } else {
                    //If sign up fails display error and set UI states
                    _uiState.update { currentState ->
                        currentState.copy(
                            incorrectSignUp = true,
                            isSignedUp = false
                        )
                    }
                    //Display reason for error
                    _error.postValue("Sign-up failed: ${task.exception?.message}")
                }
            }
    }

    //Home page funcs
    //DB check to not display movies already in watchlist or watched
     fun checkMovieInWatchlist(movieID: Int){
            db.collection("users")
                .document(_uiState.value.loggedInUID)
                .collection("watchlist")
                .document(movieID.toString())
                .get()
                .addOnSuccessListener { watchlistCheck->
                    if(watchlistCheck.exists())
                    {
                        //Set UI state true
                        _uiState.update{
                                currentState -> currentState.copy(
                            movieInWatchlist = true
                        )
                        }
                    }
                    else
                    {
                        _uiState.update{
                                currentState -> currentState.copy(
                            movieInWatchlist = false
                        )
                        }
                    }
                }
                .addOnFailureListener{
                    //TODO: What to do if check fails
                    _uiState.update{
                            currentState -> currentState.copy(
                        movieInWatchlist = false
                    )
                    }
                    Log.d("Watchlist check failed", "Check has failed")

                }
    }

    fun checkMovieInWatched(movieID: Int){
        db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watched")
            .document(movieID.toString())
            .get()
            .addOnSuccessListener { watchlistCheck->
                if(watchlistCheck.exists())
                {
                    //Set UI state true
                    _uiState.update{
                            currentState -> currentState.copy(
                        movieInWatched = true
                    )
                    }
                }
                else
                {
                    _uiState.update{
                            currentState -> currentState.copy(
                        movieInWatched = false
                    )
                    }
                }
            }
            .addOnFailureListener{
                //TODO: What to do if check fails
                _uiState.update{
                        currentState -> currentState.copy(
                    movieInWatched = false
                )
                }
                Log.d("Watched check failed", "Check has failed")
            }
    }


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

    fun addMovieToWatchedList(movie: Movie) {
        //Mapping movie details to db fields
        val movieDetails = mapOf(
            "title" to movie.title,
            "overview" to movie.overview,
            "poster_url" to movie.poster_path,
        )
        //Gets path where to movie will be inserted
        val moviePath = db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watched")
            .document(movie.id.toString())

        moviePath.set(movieDetails)
            .addOnSuccessListener {
                // Success
                Log.d("Movie to Watched:","Movie added to watched")
            }
            .addOnFailureListener { exception ->
                Log.d("Movie to Watched:", "Movie failed to add to watched$exception")
            }
    }

    //Stores current movie to swipe details
    fun getCurrentMovie(movieID:Int, movieTitle:String, movieOverview:String, moviePosterPath:String?){
        _uiState.update{
                currentState -> currentState.copy(
                    currentMovieID = movieID,
                    currentMovieTitle =  movieTitle,
                    currentMovieOverview = movieOverview,
                    currentMoviePosterPath = moviePosterPath,
                    viewingMovieCrew = false
        )
        }
    }
    //Filter by streaming service
    fun removeStreamingFilter(streamingService:String){
        Log.d("UIState", "Current Watch Provider Filter: ${_uiState.value.watchProviderFilter}")

        _uiState.update{
                currentState -> currentState.copy(
                    watchProviderFilter = _uiState.value.watchProviderFilter
                        .split("|")//Splits by comma
                        .filter { it != streamingService }  //Removes the given number/streaming service
                        .joinToString("|") //Converts back into string
                        .trim() //Removes spaces
        )
        }
        Log.d("UIState", "Current Watch Provider Filter: ${_uiState.value.watchProviderFilter}")
    }

    fun addStreamingFilter(streamingService: String){
        Log.d("UIState", "Current Watch Provider Filter: ${_uiState.value.watchProviderFilter}")
        if(_uiState.value.watchProviderFilter == ""){
            _uiState.update{
                    currentState -> currentState.copy(
                watchProviderFilter = _uiState.value.watchProviderFilter + streamingService
            )
            }
        }
        else{
            _uiState.update{
                    currentState -> currentState.copy(
                watchProviderFilter = _uiState.value.watchProviderFilter + "|" + streamingService
            )
            }
        }
        Log.d("UIState", "Current Watch Provider Filter: ${_uiState.value.watchProviderFilter}")

    }

    fun setDisneyFilter(){
        if(_uiState.value.disneyFilter){
            _uiState.update{
                    currentState -> currentState.copy(
                disneyFilter = false
            )
            }
        }
        else
        {
            _uiState.update{
                    currentState -> currentState.copy(
                disneyFilter = true
            )
            }
        }
    }

    fun setPrimeFilter(){
        if(_uiState.value.primeFilter){
            _uiState.update{
                    currentState -> currentState.copy(
                primeFilter = false
            )
            }
        }
        else
        {
            _uiState.update{
                    currentState -> currentState.copy(
                primeFilter = true
            )
            }
        }
    }

    fun setNetflixFilter(){
        if(_uiState.value.netflixFilter){
            _uiState.update{
                    currentState -> currentState.copy(
                netflixFilter = false
            )
            }
        }
        else
        {
            _uiState.update{
                    currentState -> currentState.copy(
                netflixFilter = true
            )
            }
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
                viewingMovieDetails =  true,
                viewingHome = false
            )
            }
        }
        else if(screenTitles[currentScreen] == "Home"){
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false,
                viewingHome = true
            )
            }
        }
        else
        {
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false,
                viewingHome = false
            )
            }
        }
    }

    fun expandFilterMenu(){
        _uiState.update{
                currentState -> currentState.copy(
            filterMenuExpanded = true
        )
        }
    }

    fun dismissFilterMenu(){
        _uiState.update{
                currentState -> currentState.copy(
            filterMenuExpanded = false
        )
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
        _userSearchResults.postValue(emptyList())

    }

    //TODO: Reference this
    //https://inorganik.medium.com/implementing-a-simple-effective-search-in-firebase-with-just-firestore-957dd716ccdb
    //Reference for string matching with firebase
    fun performUserSearch() {
        val currentQuery = searchText
        db.collection("users")
            //Search usernames
            .whereGreaterThanOrEqualTo("username", currentQuery)
            .whereLessThanOrEqualTo("username", currentQuery + '\uf8ff')
            .whereNotEqualTo("username", _uiState.value.loggedInUsername)
            .get()
            .addOnSuccessListener { queryResults ->
                //List to store usernames
                val users = mutableListOf<FilmswipeUser>()

                Log.d("performUserSearch", "Number of users found: ${queryResults.size()}")

                // Loop through the results and extract usernames
                for (document in queryResults) {
                    //Grab current users username and profile picture
                    val username = document.getString("username")
                    val profilePicture = document.getString("profile_picture")
                    //Creating filmswipe user object
                    if (username != null) {
                        val filmswipeUser = FilmswipeUser(username, profilePicture)
                        users.add(filmswipeUser) //Add to users list
                    }
                }
                //Adds the users to the live data object
                _userSearchResults.postValue(users)
            }
            .addOnFailureListener { exception ->
                _error.postValue("Search failed: ${exception.message}")
            }
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

