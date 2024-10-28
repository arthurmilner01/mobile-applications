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
    var emailInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")

    fun checkLoginDetails(){
        //TODO: Use database for validation
        if((emailInput.equals("arthur@email.com", ignoreCase=true)) && (passwordInput == "password")){
            userLogsIn(emailInput=emailInput)
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

    fun updateEmailInput(currentEmailInput:String){
        emailInput = currentEmailInput
    }

    fun updatePasswordInput(currentPasswordInput:String){
        passwordInput = currentPasswordInput
    }

    private fun userLogsIn(emailInput:String){
        _uiState.update{
            currentState -> currentState.copy(
                isLoggedIn = true,
                loggedInEmail = emailInput,
                loggedInUsername = "tempusername", //TODO: Use username when implemented
                incorrectLogin = false
        )
        }
    }

    //Top bar funcs

    //Used to map nav host title to the displayed title
    private val screenTitles = mapOf(
        "homescreen" to "Home",
        "profilescreen" to "Profile",
        "settingsscreen" to "Settings",
        "loginscreen" to "Login"
    )

    fun getScreenTitle(navController: NavController){
        val currentScreen = navController.currentBackStackEntry?.destination?.route
        _uiState.update{
            currentState -> currentState.copy(
            navScreenTitle =  screenTitles[currentScreen] ?: " " //Empty title if no title
        )
        }
    }

    fun navGoBack(navController: NavController){
        navController.navigateUp()
        getScreenTitle(navController)
        changeNavSelectedItem(_uiState.value.navSelectedItem)
    }

    //Bottom bar funcs
    fun changeNavSelectedItem(index:Int){
        _uiState.update{
            currentState -> currentState.copy(
                navSelectedItem = index
        )}
    }

}

