package com.example.filmswipe.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.filmswipe.data.CastMember
import com.example.filmswipe.data.CrewMember
import com.example.filmswipe.data.FilmswipeUser
import com.example.filmswipe.data.Movie
import com.example.filmswipe.data.ProfileMovie
import com.example.filmswipe.network.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.random.Random

//Using AndroidViewModel application context for persistent storage using SharedPreferences
//https://blog.mansi.dev/difference-between-androidviewmodel-and-viewmodel
class AppViewModel(application: Application) : AndroidViewModel(application) {
    //TODO: Comment and see if can be split to multiple files

    //User input
    //Log In
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    //Sign Up
    var signUpEmailInput by mutableStateOf("")
    var signUpUsernameInput by mutableStateOf("")
    var signUpPasswordInput by mutableStateOf("")
    //Change password
    var changePasswordCurrentPasswordInput by mutableStateOf("")
    var changePasswordPasswordInput by mutableStateOf("")
    var changePasswordConfirmPasswordInput by mutableStateOf("")
    // Search
    var searchText by mutableStateOf("")

    //uiState
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    //SharedPreferences
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("userState", Context.MODE_PRIVATE)

    //Database
    //Auth firebase instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    //Firebase db instance
    private val db = Firebase.firestore

    //API
    private val apiKey = "bee0c37b9c1a2d1c1ecf80b6cce631a5"

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

    //Live data object for profile watchlist movies
    private val _watchlistMovies = MutableLiveData<List<ProfileMovie>>()
    val watchlistMovies: LiveData<List<ProfileMovie>> get() = _watchlistMovies

    //Live data object for profile watched movies
    private val _watchedMovies = MutableLiveData<List<ProfileMovie>>()
    val watchedMovies: LiveData<List<ProfileMovie>> get() = _watchedMovies



    fun fetchPopularMovies() {
        _loading.value = true
        viewModelScope.launch {
            try {
                //API call with random page number
                //TODO: TEST IF THIS IS A VALID RANGE, OR MAKE API CALL TO GET THE NUMBER OF PAGES BEFORE FETCHING MOVIES
                val pageNumber = Random.nextInt(1,100)
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

                    val filteredResults = response.body()?.results?.filter {
                        movie ->movie.overview.isNotBlank() && movie.poster_path != null
                    } ?: emptyList()

                    _searchResults.postValue(filteredResults)
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
        clearLoginState()
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
        saveLoginState(true, currentEmailInput, currentUsernameInput, userUID)
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

    fun checkSignUpDetails() {
        _uiState.update { currentState ->
            currentState.copy(
                signUpEmailError = "",
                signUpUsernameError = "",
                signUpPasswordError = ""
            )
        }

        //https://regexr.com/3e48o
        val emailPattern = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")

        if (!emailPattern.matches(signUpEmailInput)) {
            _uiState.update { it.copy(signUpEmailError = "Invalid email format.") }
        }

        if (signUpUsernameInput.length < 3) {
            _uiState.update { it.copy(signUpUsernameError = "Username must be at least 3 characters long.") }
        }

        if (signUpPasswordInput.length < 6) {
            _uiState.update { it.copy(signUpPasswordError = "Password must be at least 6 characters long.") }
        }

        if (_uiState.value.signUpEmailError != "" ||
            _uiState.value.signUpUsernameError != "" ||
            _uiState.value.signUpPasswordError != "") {
            return
        }



        auth.createUserWithEmailAndPassword(signUpEmailInput, signUpPasswordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userDetails = mapOf(
                            "username" to signUpUsernameInput,
                            "email" to signUpEmailInput
                        )
                        db.collection("users").document(user.uid).set(userDetails)
                            .addOnSuccessListener {
                                signUpEmailInput = ""
                                signUpPasswordInput = ""
                                signUpUsernameInput = ""
                                emailInput = ""
                                passwordInput = ""

                                _uiState.update { currentState ->
                                    currentState.copy(
                                        isSignedUp = true
                                    )
                                }
                            }
                            .addOnFailureListener { exception ->
                                _error.postValue("Failed to save user info: ${exception.message}")
                            }
                    }
                } else {
                    val exceptionMessage = task.exception?.message ?: ""
                    if (exceptionMessage.contains("email address is already in use", ignoreCase = true)) {
                        _uiState.update { it.copy(signUpEmailError = "Email is already in use.") }
                    } else {
                        _uiState.update { it.copy(signUpPasswordError = "An unknown error has occured. Please try again.") }
                    }
                }
            }
    }

    //Shared Preferences funcs
    //Save login state to persistence storage
    fun saveLoginState(isLoggedIn: Boolean, email: String, username: String, uid: String) {
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", isLoggedIn)
            putString("loggedInEmail", email)
            putString("loggedInUsername", username)
            putString("loggedInUID", uid)
            apply()
        }
    }

    //Clear login persistent state(on logout)
    fun clearLoginState() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }

    //Restore application state from SharedPreferences on app initialized
    fun restoreLoginState() {
        val loggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val email = sharedPreferences.getString("loggedInEmail", "") ?: ""
        val username = sharedPreferences.getString("loggedInUsername", "") ?: ""
        val uid = sharedPreferences.getString("loggedInUID", "") ?: ""

        _uiState.update { currentState ->
            currentState.copy(
                isLoggedIn = loggedIn,
                loggedInEmail = email,
                loggedInUsername = username,
                loggedInUID = uid
            )
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

    //Runs on swipe up
    fun addMovieToWatched(id:Int, title:String, overview:String, poster_path:String?) {
        //Mapping movie details to db fields
        val movieDetails = mapOf(
            "title" to title,
            "overview" to overview,
            "poster_path" to poster_path,
        )
        //Gets path where to movie will be inserted
        val moviePath = db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watched")
            .document(id.toString())

        moviePath.set(movieDetails)
            .addOnSuccessListener {
                // Success
                Log.d("Movie to Watched:","Movie added to watched")
            }
            .addOnFailureListener { exception ->
                Log.d("Movie to Watched:", "Movie failed to add to watched$exception")
            }
    }

    fun addMovieToWatchlist(id:Int, title:String, overview:String, poster_path:String?) {
        //Mapping movie details to db fields
        val movieDetails = mapOf(
            "title" to title,
            "overview" to overview,
            "poster_path" to poster_path,
        )
        //Gets path where to movie will be inserted
        val moviePath = db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watchlist")
            .document(id.toString())

        moviePath.set(movieDetails)
            .addOnSuccessListener {
                // Success
                Log.d("Movie to Watchlist:","Movie added to Watchlist")
            }
            .addOnFailureListener { exception ->
                Log.d("Movie to Watchlist:", "Movie failed to add to Watchlist$exception")
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

    fun fetchUserProfileByEmail(email: String, callback: (FilmswipeUser?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { queryResults ->
                if (!queryResults.isEmpty) {
                    val document = queryResults.documents[0]
                    val username = document.getString("username") ?: "Unknown User"
                    val profilePicture = document.getString("profile_picture")
                    val user = FilmswipeUser(username, profilePicture, email)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("fetchUserProfileByEmail", "Failed to retrieve user: ${exception.message}")
                callback(null)
            }
    }

    //https://stackoverflow.com/questions/9224056/android-bitmap-to-base64-string
    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    //https://stackoverflow.com/questions/4837110/how-to-convert-a-base64-string-into-a-bitmap-image-to-show-it-in-a-imageview
    fun convertBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun updateUserProfilePicture(email: String, base64String: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { queryResults ->
                if (!queryResults.isEmpty) {
                    val document = queryResults.documents[0]
                    val userId = document.id

                    // Update the profile picture in the Firestore document
                    db.collection("users").document(userId)
                        .update("profile_picture", base64String)
                        .addOnSuccessListener {
                            Log.d("updateUserProfilePicture", "Profile picture updated successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.d("updateUserProfilePicture", "Failed to update profile picture: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("updateUserProfilePicture", "Failed to find user: ${exception.message}")
            }
    }


    fun usersWatchedMovies(email: String) {
        //Path to the users watched movies
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { queryResults ->
                if (!queryResults.isEmpty) {
                    val document = queryResults.documents[0]
                    val userId = document.id

                    val watchedRef = db.collection("users")
                        .document(userId)
                        .collection("watched")

                    watchedRef.get()
                        .addOnSuccessListener { watchedResults ->
                            //toObjects simply maps the returned data to the
                            //same Movie data class used by returned API data
                            //uses class.java because of Firestore
                            val watchedMovies = watchedResults.documents.mapNotNull { doc ->
                                val movie = doc.toObject(ProfileMovie::class.java)
                                movie?.copy(id = doc.id) //Assign the movie ID from document ID
                            }
                            _watchedMovies.value = watchedMovies
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Error getting watched movies:", "Failed$exception")
                            _watchedMovies.value = emptyList() //Set empty on failure
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Error finding user:", "Failed$exception")
            }
    }


    fun usersWatchlistedMovies(email: String) {
        //Path to the users watched movies
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { queryResults ->
                if (!queryResults.isEmpty) {
                    val document = queryResults.documents[0]
                    val userId = document.id

                    val watchlistRef = db.collection("users")
                        .document(userId)
                        .collection("watchlist")

                    watchlistRef.get()
                        .addOnSuccessListener { watchlistResults ->
                            //toObjects simply maps the returned data to the
                            //same Movie data class used by returned API data
                            //uses class.java because of Firestore
                            val watchlistedMovies = watchlistResults.documents.mapNotNull { doc ->
                                val movie = doc.toObject(ProfileMovie::class.java)
                                movie?.copy(id = doc.id) //Assign the movie ID from document ID
                            }
                            _watchlistMovies.value = watchlistedMovies
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Error getting watchlist:", "Failed$exception")
                            _watchlistMovies.value = emptyList() //Set empty on failure
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Error finding user:", "Failed$exception")
            }
    }

    fun removeMovieFromWatched(id:Int) {
        //Gets path where to movie will be deleted
        val moviePath = db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watched")
            .document(id.toString())

        moviePath.delete()
            .addOnSuccessListener {
                // Success
                Log.d("Movie to Watched:","Movie removed from watched")
            }
            .addOnFailureListener { exception ->
                Log.d("Movie to Watched:", "Movie failed to remove from watched$exception")
            }
    }

    fun removeMovieFromWatchlist(id:Int) {
        //Gets path where to movie will be deleted
        val moviePath = db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watchlist")
            .document(id.toString())

        moviePath.delete()
            .addOnSuccessListener {
                // Success
                Log.d("Movie to Watchlist:","Movie removed from watchlist")
            }
            .addOnFailureListener { exception ->
                Log.d("Movie to Watchlist:", "Movie failed to remove from watchlist$exception")
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
        "moviedetailsscreen" to "",
        "changepasswordscreen" to "Change Password"
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
                viewingHome = false,
                viewingChangePassword =  false
            )
            }
        }
        else if(screenTitles[currentScreen] == "Home"){
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false,
                viewingHome = true,
                viewingChangePassword =  false
            )
            }
        }
        else if(screenTitles[currentScreen] == "Change Password"){
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false,
                viewingHome = false,
                viewingChangePassword =  true
            )
            }
        }
        else
        {
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false,
                viewingHome = false,
                viewingChangePassword =  false
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
                    val email = document.getString("email")
                    //Creating filmswipe user object
                    if (username != null && email != null) {
                        val filmswipeUser = FilmswipeUser(username, profilePicture, email)
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

    fun fetchMovieDetails(movieId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieDetails(movieId, apiKey)
                if (response.isSuccessful) {
                    response.body()?.let { details ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                currentMovieGenres = details.genres.map { it.name },
                                currentMovieIMDBRating = String.format("%.1f", details.vote_average).toDouble()
                            )
                        }
                    }
                } else {
                    Log.e("fetchMovieDetails", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("fetchMovieDetails", "Exception: ${e.message}")
            }
        }
    }


    //Change Password Funcs
    fun updateChangePasswordCurrentPasswordInput(currentPasswordInput: String){
        changePasswordCurrentPasswordInput = currentPasswordInput
    }

    fun updateChangePasswordPasswordInput(password:String){
        changePasswordPasswordInput = password
    }

    fun updateChangePasswordConfirmPasswordInput(confirmPassword:String){
        changePasswordConfirmPasswordInput = confirmPassword
    }

    fun checkChangePasswordDetails() {
        // Reset error messages and success state
        _uiState.update { currentState ->
            currentState.copy(
                changePasswordCurrentPasswordError = "",
                changePasswordPasswordError = "",
                changePasswordConfirmPasswordError = "",
                isUpdatedPasswordSuccess = false
            )
        }


        if (changePasswordPasswordInput.length < 6) {
            _uiState.update { it.copy(changePasswordPasswordError = "Password must be at least 6 characters long.") }
        }
        if (changePasswordConfirmPasswordInput != changePasswordPasswordInput) {
            _uiState.update { it.copy(changePasswordConfirmPasswordError = "Passwords do not match.") }
        }

        if (_uiState.value.changePasswordCurrentPasswordError != "" ||
            _uiState.value.changePasswordPasswordError != ""  ||
            _uiState.value.changePasswordConfirmPasswordError != ""
        ) {
            return
        }

        val user = auth.currentUser
        if (user != null) {
            //Reauthenticating the user as only authorized users can change sensitive details
            val credential = EmailAuthProvider.getCredential(user.email ?: "", changePasswordCurrentPasswordInput)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {

                        user.updatePassword(changePasswordPasswordInput)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    _uiState.update { currentState ->
                                        currentState.copy(
                                            isUpdatedPasswordSuccess = true
                                        )
                                    }

                                    changePasswordCurrentPasswordInput = ""
                                    changePasswordPasswordInput = ""
                                    changePasswordConfirmPasswordInput = ""

                                } else {
                                    _uiState.update { it.copy(changePasswordPasswordError = "An unknown error has occured. Please try again.") }
                                }
                            }
                    } else {
                        _uiState.update { it.copy(changePasswordCurrentPasswordError = "Password is incorrect.") }
                    }
                }
        }
    }

}

