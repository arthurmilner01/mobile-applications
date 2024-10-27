package com.example.filmswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.filmswipe.model.AppViewModel
import com.example.filmswipe.ui.theme.FilmSwipeTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.wallet.button.ButtonConstants

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FilmSwipeTheme {
                val navController = rememberNavController()
                val appViewModel: AppViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {BottomNavigationBar(navController, appViewModel)}) { innerPadding ->
                    AppNavigator(modifier = Modifier.padding(innerPadding),
                        navController= navController,
                        appViewModel=appViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigator(modifier:Modifier = Modifier, navController: NavController, appViewModel: AppViewModel){
    NavHost(
        navController = navController as NavHostController,
        startDestination = "loginscreen",
        modifier=modifier)
    {
        composable("loginscreen") { LoginScreen(navController, appViewModel, modifier) }
        composable("homescreen") { HomeScreen(navController, appViewModel, modifier) }
        composable("profilescreen") { ProfileScreen(navController, appViewModel, modifier) }
        composable("settingsscreen") { SettingsScreen(navController, appViewModel, modifier) }
    }
}

@Composable
fun LoginScreen(navController:NavController, appViewModel:AppViewModel, modifier:Modifier=Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()
    val loginImage = painterResource(R.drawable.filmswipelogo)

    LaunchedEffect(appUiState.isLoggedIn) {
        if (appUiState.isLoggedIn) {
            navController.navigate("homescreen")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
            .background(colorResource(id=R.color.Background))
    )
    {
        Image(painter=loginImage,
            contentDescription = "App Logo",
            modifier=Modifier
                .padding(top=10.dp,bottom=10.dp, start=25.dp, end=25.dp)
                .size(200.dp))

        OutlinedTextField(
            value = appViewModel.emailInput,
            onValueChange = { appViewModel.updateEmailInput(it) },
            isError = appUiState.incorrectLogin,
            label = { Text("Email", color = Color.White) },
            modifier = Modifier
                .padding(10.dp)
        )
        OutlinedTextField(
            value = appViewModel.passwordInput,
            onValueChange = { appViewModel.updatePasswordInput(it) },
            isError = appUiState.incorrectLogin,
            label = { Text("Password", color = Color.White) },
            modifier = Modifier
                .padding(10.dp)
        )
        Button(
            onClick = { appViewModel.checkLoginDetails() }, modifier=Modifier
                .padding(10.dp)
                .size(width = 200.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id=R.color.PrimaryButton))
        )
        {
            Text("Login")
        }
        Button(
            onClick = {},
            modifier=Modifier
                .padding(10.dp)
                .size(width = 200.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id=R.color.SecondaryButton))
        )
        {
            Text("Sign-Up")
        }
    }
}

@Composable
fun HomeScreen(navController:NavController, appViewModel:AppViewModel, modifier: Modifier=Modifier){
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
fun ProfileScreen(navController:NavController,appViewModel:AppViewModel, modifier: Modifier=Modifier){
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

@Composable
fun SettingsScreen(navController:NavController,appViewModel:AppViewModel, modifier: Modifier=Modifier){
    val appUiState by appViewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Settings!",
            modifier = modifier
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, appViewModel: AppViewModel){
    val appUiState by appViewModel.uiState.collectAsState()
    val items = listOf("Home", "Profile","Settings")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Person, Icons.Filled.Settings)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Person, Icons.Outlined.Settings)

    if(appUiState.isLoggedIn){
        NavigationBar {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (appUiState.navSelectedItem == index) selectedIcons[index] else unselectedIcons[index],
                            contentDescription = item
                        )
                    },
                    label = { Text(item) },
                    selected = index == appUiState.navSelectedItem,
                    onClick = {
                        appViewModel.changeNavSelectedItem(index)
                        navController.navigate(route = item.plus("screen"))
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FilmSwipeTheme {
        val navController = rememberNavController()
        val appViewModel: AppViewModel = viewModel()

        Scaffold(modifier = Modifier.fillMaxSize(),
            bottomBar = {BottomNavigationBar(navController, appViewModel)}) { innerPadding ->
            AppNavigator(modifier = Modifier.padding(innerPadding), navController, appViewModel)
        }
    }
}