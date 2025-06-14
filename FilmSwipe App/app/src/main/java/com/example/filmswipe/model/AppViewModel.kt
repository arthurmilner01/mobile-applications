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
class AppViewModel(application: Application) : AndroidViewModel(application) {
    //User input states
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

    //API key
    private val apiKey = "******"

    //Live data object for movies
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> get() = _movies
    //Used when waiting for response from API call
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    //Used when API call returns an error
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    //Live data object for cast
    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> get() = _cast


    //Live data for crew
    private val _crew = MutableLiveData<List<CrewMember>>()
    val crew: LiveData<List<CrewMember>> get() = _crew
    //Used when waiting for response from cast/crew API call
    private val _creditsLoading = MutableLiveData<Boolean>()
    val creditsLoading: LiveData<Boolean> get() = _creditsLoading
    //Used when API call returns an error
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


    //API call which fetches movies at random based on current filters
    fun fetchPopularMovies() {
        //Display loading when waiting response
        _loading.value = true
        viewModelScope.launch {
            try {
                //API call with random page number between 1-100
                val pageNumber = Random.nextInt(1,100)
                //Get response from API call, reponse will be a list of Movie objects
                val response = RetrofitInstance.api.getPopularMovies(apiKey, pageNumber, _uiState.value.watchProviderFilter)

                if (response.isSuccessful) {
                    //Add movies to the movies live data
                    _movies.postValue(response.body()?.results ?: emptyList())
                    //Clear any previous errors
                    _error.postValue(null)
                } else {
                    _error.postValue("Error getting movies.")
                }
            } catch (e: Exception) {
                _error.postValue("Error getting movies.")
            } finally {
                //Set loading false as response received
                _loading.postValue(false)
            }
        }
    }

    //API call that fetches the current movies cast/crew
    fun fetchMovieCredits(movieId: Int) {
        //Show loading wheel
        _creditsLoading.value = true
        viewModelScope.launch {
            try {
                //Get API response, will return list of cast and list of crew
                val response = RetrofitInstance.api.getMovieCredits(movieId, apiKey)
                if (response.isSuccessful) {
                    response.body()?.let { credits ->
                        //Adding the list of cast to the cast live data object
                        _cast.postValue(credits.cast)
                        //Adding the list of crew to the crew live data object
                        _crew.postValue(credits.crew)
                        //Clearing errors from previous calls
                        _creditsError.postValue(null)
                    }
                } else {
                    _creditsError.postValue("Error getting movie credits.")
                }
            } catch (e: Exception) {
                _creditsError.postValue("Error getting movie credits.")
            } finally {
                //When try finished stop loading
                _creditsLoading.postValue(false)
            }
        }
    }

    fun searchMoviesByTitle() {
        //If user attempts to search with no input
        if (searchText.isBlank()) {
            //Empty list
            _searchResults.postValue(emptyList())
            return
        }
        //Set loading to true until results received
        _loading.value = true
        viewModelScope.launch {
            try {
                //Making API call to return relevant movies based on user input
                val response = RetrofitInstance.api.searchMoviesByTitle(apiKey, searchText)

                if (response.isSuccessful) {
                    //Filter results so only movies without missing details are displayed
                    val filteredResults = response.body()?.results?.filter {
                        //Movie must have poster and an overview
                        movie ->movie.overview.isNotBlank() && movie.poster_path != null
                    } ?: emptyList()
                    //Add the movies returned to search results live data for displaying to UI
                    _searchResults.postValue(filteredResults)
                    //Clear errors as successful
                    _error.postValue(null)
                } else {
                    _error.postValue("Error searching movies.")
                }
            } catch (e: Exception) {
                _error.postValue("Error searching movies.")
            } finally {
                //Set loading to false once try is complete to hide loading wheel
                _loading.postValue(false)
            }
        }
    }



    //Log In funcs
    //Update based on users inputs on log-in page
    fun updateEmailInput(currentEmailInput:String){
        emailInput = currentEmailInput
    }
    fun updatePasswordInput(currentPasswordInput:String){
        passwordInput = currentPasswordInput
    }

    //Checking the login details provided by the user
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
        //Firebase auth sign-in
        auth.signInWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login successful
                    val user = auth.currentUser
                    //Sends username and email to user logs in function
                    user?.let {
                        //Get username and email from Firebase db
                        //Firebase auth UID will be the same as that users document ID
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                val username = document.getString("username") ?: "Unknown User"
                                //Passes email and username so relevant states can be updated
                                userLogsIn(user.email ?: "", username, user.uid)
                            }
                            .addOnFailureListener {
                                _error.postValue("Failed to retrieve username.")
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

    //When user logs out
    fun userLogsOut(){
        //Update relevant UI states
        _uiState.update{
                currentState -> currentState.copy(
            isLoggedIn = false,
            loggedInEmail = "",
            loggedInUsername = "",
            loggedInUID = "",
            incorrectLogin = false
        )
        }
        //Clear SharedPreferences so user doesn't remain logged-in when they re-open the app
        clearLoginState()
    }

    //False until user has successful sign-up
    fun newSignUp(){
        _uiState.update{
                currentState -> currentState.copy(
            isSignedUp = false
        )
        }
    }

    //On user login
    private fun userLogsIn(currentEmailInput:String, currentUsernameInput: String, userUID: String){
        //Updating relevant states
        _uiState.update{
            currentState -> currentState.copy(
                isLoggedIn = true,
                loggedInEmail = currentEmailInput,
                loggedInUsername =  currentUsernameInput,
                loggedInUID = userUID,
                incorrectLogin = false
        )
        }
        //Clear login page inputs
        emailInput = ""
        passwordInput = ""
        //Save login states to SharedPreferences for consistent login session
        saveLoginState(true, currentEmailInput, currentUsernameInput, userUID)
    }

    //Sign Up funcs

    //Updates when use inputs details on sign-up page
    fun updateSignUpEmailInput(currentEmailInput:String){
        signUpEmailInput = currentEmailInput
    }
    fun updateSignUpPasswordInput(currentPasswordInput:String){
        signUpPasswordInput = currentPasswordInput
    }
    fun updateSignUpUsernameInput(currentUsernameInput:String){
        signUpUsernameInput = currentUsernameInput
    }

    //Validates user's sign up details
    fun checkSignUpDetails() {
        _uiState.update { currentState ->
            currentState.copy(
                signUpEmailError = "",
                signUpUsernameError = "",
                signUpPasswordError = ""
            )
        }

        //Regular expression to validate email
        //https://regexr.com/3e48o
        val emailPattern = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")

        //If email doesn't match regular expression
        if (!emailPattern.matches(signUpEmailInput)) {
            //Display error to user
            _uiState.update { it.copy(signUpEmailError = "Invalid email format.") }
        }
        //If username is too short
        if (signUpUsernameInput.length < 3) {
            //Display error
            _uiState.update { it.copy(signUpUsernameError = "Username must be at least 3 characters long.") }
        }
        //If password is too short
        if (signUpPasswordInput.length < 6) {
            //Display error
            _uiState.update { it.copy(signUpPasswordError = "Password must be at least 6 characters long.") }
        }
        //If any errors flagged do not create user
        if (_uiState.value.signUpEmailError != "" ||
            _uiState.value.signUpUsernameError != "" ||
            _uiState.value.signUpPasswordError != "") {
            return
        }

        //When no errors are flagged create the user using Firebase auth
        auth.createUserWithEmailAndPassword(signUpEmailInput, signUpPasswordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        //Mapping user inputs to username/email
                        val userDetails = mapOf(
                            "username" to signUpUsernameInput,
                            "email" to signUpEmailInput
                        )
                        //Create copy of user in Firebase, not including password
                        //Using Firebase auth UID for users document ID
                        db.collection("users").document(user.uid).set(userDetails)
                            .addOnSuccessListener {
                                //When successful clear user inputs for login/sign-up
                                signUpEmailInput = ""
                                signUpPasswordInput = ""
                                signUpUsernameInput = ""
                                emailInput = ""
                                passwordInput = ""
                                //Set state to true to redirect user to log-in page
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        isSignedUp = true
                                    )
                                }
                            }
                            .addOnFailureListener {
                                _error.postValue("Failed to save user info")
                            }
                    }
                }
                else
                {
                    //Get returned error message
                    val exceptionMessage = task.exception?.message ?: ""
                    //Reformatting error message and displaying it to user
                    if (exceptionMessage.contains("email address is already in use", ignoreCase = true)) {
                        _uiState.update { it.copy(signUpEmailError = "Email is already in use.") }
                    } else {
                        _uiState.update { it.copy(signUpPasswordError = "An unknown error has occured. Please try again.") }
                    }
                }
            }
    }

    //Shared Preferences funcs
    //Save login state for consistency across opening/closing the application
    fun saveLoginState(isLoggedIn: Boolean, email: String, username: String, uid: String) {
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", isLoggedIn)
            putString("loggedInEmail", email)
            putString("loggedInUsername", username)
            putString("loggedInUID", uid)
            apply()
        }
    }

    //Clear persistent login state (on logout)
    fun clearLoginState() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }

    //Restores login state from SharedPreferences on app initialized
    fun restoreLoginState() {
        val loggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val email = sharedPreferences.getString("loggedInEmail", "") ?: ""
        val username = sharedPreferences.getString("loggedInUsername", "") ?: ""
        val uid = sharedPreferences.getString("loggedInUID", "") ?: ""
        //Update UI state from SharedPreferences values
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
            //Get watchlisted movies attached to the logged in user
            db.collection("users")
                .document(_uiState.value.loggedInUID)
                .collection("watchlist")
                .document(movieID.toString()) //Stored as string in Firebase
                .get()
                .addOnSuccessListener { watchlistCheck->
                    //If movie is found in watchlist
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
                    //If check fails just mark movie as watchlisted to ensure smooth operation
                    _uiState.update{
                            currentState -> currentState.copy(
                        movieInWatchlist = true
                    )
                    }
                }
    }

    fun checkMovieInWatched(movieID: Int){
        //Get watched movies attached to the logged in user
        db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watched")
            .document(movieID.toString())
            .get()
            .addOnSuccessListener { watchlistCheck->
                //If movie is found in watched
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
                //If check fails just mark movie as watched to ensure smooth operation
                _uiState.update{
                        currentState -> currentState.copy(
                    movieInWatched = true
                )
                }
            }
    }


    //Swipe left
    fun removeMovie(index: Int) {
        val currentList = _movies.value?.toMutableList() ?: return
        //For index of swiped movie
        if (index in currentList.indices) {
            //Remove movie at this index
            currentList.removeAt(index)
            //Update live data object to reflect removed movie
            _movies.value = currentList
        }
    }

    //Runs on swipe up or when clicked on movie details
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
                Log.d("Movie to Watched:","Movie added to watched")
            }
            .addOnFailureListener {
                Log.d("Movie to Watched:", "Movie failed to add to watched")
            }
    }

    //Runs on swipe right or when clicked on movie details
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
        //Add to DB
        moviePath.set(movieDetails)
            .addOnSuccessListener {
                Log.d("Movie to Watchlist:","Movie added to Watchlist")
            }
            .addOnFailureListener {
                Log.d("Movie to Watchlist:", "Movie failed to add to Watchlist")
            }
    }

    //Stores currently displayed movie's details
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

    //When user disables a filter remove it from watch provider filter
    fun removeStreamingFilter(streamingService:String){
        _uiState.update{
                currentState -> currentState.copy(
                    //Update state
                    watchProviderFilter = _uiState.value.watchProviderFilter
                        //Splitting string by | if there is more than one filter active
                        .split("|")
                        //Removes the given streaming service ID
                        .filter { it != streamingService }
                        //Converts back into full string
                        .joinToString("|")
                        //Remove spaces
                        .trim()
        )}
    }

    //When user enables a filter add it to the watch provider filter
    fun addStreamingFilter(streamingService: String){
        //If the watchproviderfilter is empty (no other filters are active)
        if(_uiState.value.watchProviderFilter == ""){
            _uiState.update{
                    currentState -> currentState.copy(
                //Append the streaming service ID to the watch provider filter string
                watchProviderFilter = _uiState.value.watchProviderFilter + streamingService
            )
            }
        }
        //If watchproviderfilter is not empty
        else{
            _uiState.update{
                    currentState -> currentState.copy(
                //Add | to ensure all filters are simultaneously active and append streaming service ID to end of string
                watchProviderFilter = _uiState.value.watchProviderFilter + "|" + streamingService
            )
            }
        }
    }


    //Update states based on users filter selection
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

    //Update states based on users filter selection
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

    //Update states based on users filter selection
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

    //When user selects Watched button
    fun showProfilesLikedMovies(){
        _uiState.update{
                currentState -> currentState.copy(
            viewingWatchedMovies = true
        )
        }
    }

    //When user selects Watchlist button
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
                        .addOnFailureListener {
                            Log.d("updateUserProfilePicture", "Failed to update profile picture")
                        }
                }
            }
            .addOnFailureListener {
                Log.d("updateUserProfilePicture", "Failed to find user")
            }
    }


    fun usersWatchedMovies(email: String) {
        //Path to the viewed users watched movies
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { queryResults ->
                //If result isn't empty
                if (!queryResults.isEmpty) {
                    val document = queryResults.documents[0]
                    val userId = document.id
                    //Grabbing movies attached to users watched
                    val watchedRef = db.collection("users")
                        .document(userId)
                        .collection("watched")

                    watchedRef.get()
                        .addOnSuccessListener { watchedResults ->
                            //toObject simply maps the returned data to the
                            //ProfileMovie data class
                            //Gets list of movie objects which will be displayed using ProfileMovieCard
                            val watchedMovies = watchedResults.documents.mapNotNull { doc ->
                                val movie = doc.toObject(ProfileMovie::class.java)
                                //Assign the movie ID from document ID as these are the same (movie ID is used as document ID when marked as watched)
                                movie?.copy(id = doc.id)
                            }
                            //Adding the movies returned to watched movies live data
                            _watchedMovies.value = watchedMovies
                        }
                        .addOnFailureListener {
                            Log.d("Error getting watched movies:", "Failed")
                            _watchedMovies.value = emptyList() //Set empty on failure so profile will just load with not movies under watched
                        }
                }
            }
            .addOnFailureListener {
                Log.d("Error finding user:", "Failed")
            }
    }


    fun usersWatchlistedMovies(email: String) {
        //Path to the users watchlist movies
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { queryResults ->
                //If result isn't empty
                if (!queryResults.isEmpty) {
                    val document = queryResults.documents[0]
                    val userId = document.id
                    //Grabbing movies attached to users watchlist
                    val watchlistRef = db.collection("users")
                        .document(userId)
                        .collection("watchlist")

                    watchlistRef.get()
                        .addOnSuccessListener { watchlistResults ->
                            //toObject simply maps the returned data to the
                            //ProfileMovie data class
                            //Gets list of movie objects which will be displayed using ProfileMovieCard
                            val watchlistedMovies = watchlistResults.documents.mapNotNull { doc ->
                                val movie = doc.toObject(ProfileMovie::class.java)
                                //Assign the movie ID from document ID as these are the same (movie ID is used as document ID when marked as watched)
                                movie?.copy(id = doc.id)
                            }
                            //Adding the movies returned to watchlist movies live data
                            _watchlistMovies.value = watchlistedMovies
                        }
                        .addOnFailureListener {
                            Log.d("Error getting watchlist:", "Failed")
                            _watchlistMovies.value = emptyList() //Set empty on failure so profile will just load with no movies under watched
                        }
                }
            }
            .addOnFailureListener {
                Log.d("Error finding user:", "Failed")
            }
    }

    fun removeMovieFromWatched(id:Int) {
        //Gets path where the movie will be deleted
        val moviePath = db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watched")
            .document(id.toString())
        //Deletes movie in that path from the db
        moviePath.delete()
            .addOnSuccessListener {
                Log.d("Movie to Watched:","Movie removed from watched")
            }
            .addOnFailureListener {
                Log.d("Movie to Watched:", "Movie failed to remove from watched")
            }
    }

    fun removeMovieFromWatchlist(id:Int) {
        //Gets path where the movie will be deleted
        val moviePath = db.collection("users")
            .document(_uiState.value.loggedInUID)
            .collection("watchlist")
            .document(id.toString())
        //Deletes movie in that path from the db
        moviePath.delete()
            .addOnSuccessListener {
                Log.d("Movie to Watchlist:","Movie removed from watchlist")
            }
            .addOnFailureListener {
                Log.d("Movie to Watchlist:", "Movie failed to remove from watchlist")
            }
    }

    //Top bar funcs
    //Used to map nav host title to the title which will be displayed on the UI
    private val screenTitles = mapOf(
        "homescreen" to "Home",
        "profilescreen" to "Profile",
        "settingsscreen" to "Settings",
        "loginscreen" to "Login",
        "searchscreen" to "Search",
        "moviedetailsscreen" to "",
        "changepasswordscreen" to "Change Password"
    )

    //Function to update the current screen's title so UI will update, also tracks what page user is viewing and updates states accordingly
    fun getScreenTitle(navController: NavController){
        val currentScreen = navController.currentBackStackEntry?.destination?.route
        _uiState.update{
            currentState -> currentState.copy(
            navScreenTitle =  screenTitles[currentScreen] ?: " " //Empty title if no title
        )
        }
        //If screen title is "" it means user is on movie details page so update states
        if(screenTitles[currentScreen] == ""){
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  true,
                viewingHome = false,
                viewingChangePassword =  false
            )
            }
        }
        //If screen title is "Home" it means user is on home page so update states
        else if(screenTitles[currentScreen] == "Home"){
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false,
                viewingHome = true,
                viewingChangePassword =  false
            )
            }
        }
        //If screen title is "Change Password" it means user is on change password page so update states
        else if(screenTitles[currentScreen] == "Change Password"){
            _uiState.update{
                    currentState -> currentState.copy(
                viewingMovieDetails =  false,
                viewingHome = false,
                viewingChangePassword =  true
            )
            }
        }
        //If none of these set states to false
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

    //Changes state to expand watch filter menu
    fun expandFilterMenu(){
        _uiState.update{
                currentState -> currentState.copy(
            filterMenuExpanded = true
        )
        }
    }

    //Changes state to dismiss filter menu
    fun dismissFilterMenu(){
        _uiState.update{
                currentState -> currentState.copy(
            filterMenuExpanded = false
        )
        }
    }

    //Bottom bar funcs

    //Updates bottom nav bar visually so icon for current page icon has selected page styling
    fun changeNavSelectedItem(index:Int){
        _uiState.update{
            currentState -> currentState.copy(
                navSelectedItem = index
        )}
    }


    //Search Funcs

    //Clears previous search results and gets new user input
    //Runs when search input is updated
    fun updateSearchQuery(newSearchText:String){
        searchText = newSearchText
        _searchResults.postValue(emptyList())
        _userSearchResults.postValue(emptyList())

    }

    //When user searches for users
    fun performUserSearch() {
        //Get current search input
        val currentQuery = searchText
        db.collection("users")
            //Search usernames
            //https://inorganik.medium.com/implementing-a-simple-effective-search-in-firebase-with-just-firestore-957dd716ccdb
            .whereGreaterThanOrEqualTo("username", currentQuery)
            .whereLessThanOrEqualTo("username", currentQuery + '\uf8ff')
            .whereNotEqualTo("username", _uiState.value.loggedInUsername)
            .get()
            .addOnSuccessListener { queryResults ->
                //List of FilmSwipeUser objects to store returned users
                val users = mutableListOf<FilmswipeUser>()

                //For each user in result
                for (document in queryResults) {
                    //Grab username and profile picture
                    val username = document.getString("username")
                    val profilePicture = document.getString("profile_picture")
                    val email = document.getString("email")
                    //Creating FilmSwipe user object and inserting into users list initalised above
                    if (username != null && email != null) {
                        //Create FilmswipeUser object
                        val filmswipeUser = FilmswipeUser(username, profilePicture, email)
                        users.add(filmswipeUser) //Add to users list
                    }
                }
                //Adds the users to the live data object
                _userSearchResults.postValue(users)
            }
            .addOnFailureListener {
                _error.postValue("Search failed.")
            }
    }

    //Toggle box changes search type
    fun changeSearchType(checkBoxValue:Boolean){
        //When true search for users
        if(checkBoxValue){
            _uiState.update{
                    currentState -> currentState.copy(
                searchingUsers = true
            )}
            //Clear search input and live data when changed
            searchText = ""
            _searchResults.postValue(emptyList())
        }
        //When false search for movies
        else{
            _uiState.update{
                    currentState -> currentState.copy(
                searchingUsers = false
            )}
            //Clear search input and live data when changed
            searchText = ""
            _searchResults.postValue(emptyList())
        }
    }

    //Movie details funcs

    //When Crew button is selected
    fun showMovieCrew(){
        _uiState.update{
                currentState -> currentState.copy(
            viewingMovieCrew = true
        )
        }
    }

    //When Cast button is selected
    fun showMovieCast(){
        _uiState.update{
                currentState -> currentState.copy(
            viewingMovieCrew = false
        )
        }
    }

    //API call fetches  currently viewed movie's genre and user rating
    fun fetchMovieDetails(movieId: Int) {
        viewModelScope.launch {
            try {
                //Get API call response
                val response = RetrofitInstance.api.getMovieDetails(movieId, apiKey)
                //If response is successful
                if (response.isSuccessful) {
                    response.body()?.let { details ->
                        //Update UI state with returned values for displaying
                        _uiState.update { currentState ->
                            currentState.copy(
                                currentMovieGenres = details.genres.map { it.name },
                                currentMovieIMDBRating = String.format("%.1f", details.vote_average).toDouble()
                            )
                        }
                    }
                } else {
                    Log.e("fetchMovieDetails", "Error")
                }
            } catch (e: Exception) {
                Log.e("fetchMovieDetails", "Exception: ${e.message}")
            }
        }
    }


    //Change Password Funcs

    //Updating user inputs on change password page
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

