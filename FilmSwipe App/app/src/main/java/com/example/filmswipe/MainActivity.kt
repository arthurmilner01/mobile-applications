package com.example.filmswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.filmswipe.model.AppViewModel
import com.example.filmswipe.ui.theme.FilmSwipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FilmSwipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigator(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigator(modifier:Modifier = Modifier){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "loginscreen"
    ){
        composable("loginscreen") { LoginScreen(navController) }
        composable("homescreen") { HomeScreen(navController) }
        composable("profilescreen") { ProfileScreen(navController) }
    }
}

@Composable
fun LoginScreen(navController:NavController, appViewModel:AppViewModel=viewModel(), modifier:Modifier=Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    )
    {
        Text(
            text = "Login !",
            modifier = modifier
        )
        OutlinedTextField(
            value = appViewModel.emailInput,
            onValueChange = { appViewModel.updateEmailInput(it) },
            isError=appUiState.incorrectLogin,
            label = { Text("Email") }
        )
        OutlinedTextField(
            value = appViewModel.passwordInput,
            onValueChange = { appViewModel.updatePasswordInput(it) },
            isError=appUiState.incorrectLogin,
            label = { Text("Password") }
        )
        Button(onClick = {
            appViewModel.checkLoginDetails()
            if(appUiState.isLoggedIn){
            navController.navigate("homescreen")
        }
        }) {
            Text("Login")
        }
    }
}

@Composable
fun HomeScreen(navController:NavController, appViewModel:AppViewModel=viewModel(), modifier: Modifier=Modifier){
    val appUiState by appViewModel.uiState.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.login_email, appUiState.loggedInEmail),
            modifier = modifier
        )
    }
}

@Composable
fun ProfileScreen(navController:NavController,appViewModel:AppViewModel=viewModel(), modifier: Modifier=Modifier){
    val appUiState by appViewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Profile!",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FilmSwipeTheme {
        AppNavigator()
    }
}