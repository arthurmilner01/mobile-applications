package com.example.filmswipe.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
        "searchscreen" to "Search"
    )

    fun getScreenTitle(navController: NavController){
        val currentScreen = navController.currentBackStackEntry?.destination?.route
        _uiState.update{
            currentState -> currentState.copy(
            navScreenTitle =  screenTitles[currentScreen] ?: " " //Empty title if no title
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
    }


    fun performSearch() {
        val currentQuery = searchText
        //TODO: Get Film Data from api using query
    }

}

