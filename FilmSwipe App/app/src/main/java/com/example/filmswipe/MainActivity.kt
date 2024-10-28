package com.example.filmswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.wallet.button.ButtonConstants

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val appViewModel: AppViewModel = viewModel()
            val appUiState by appViewModel.uiState.collectAsState()

            FilmSwipeTheme(appUiState.darkMode, navController,appViewModel){
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = { TopNavigationBar(navController, appViewModel)},
                    bottomBar = {BottomNavigationBar(navController, appViewModel)}) { innerPadding ->
                    Box(modifier=Modifier.padding(innerPadding)) {
                        AppNavigator(
                            modifier = Modifier.padding(8.dp),
                            navController = navController,
                            appViewModel = appViewModel
                        )
                    }
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
        modifier= modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize())
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
        modifier = Modifier
    )
    {
        Image(painter=loginImage,
            contentDescription = "App Logo",
            modifier= Modifier
                .padding(
                    top = 10.dp,
                    bottom = 10.dp,
                    start = 25.dp,
                    end = 25.dp
                )
                .size(200.dp)
        )

        OutlinedTextField(
            value = appViewModel.emailInput,
            onValueChange = { appViewModel.updateEmailInput(it) },
            isError = appUiState.incorrectLogin,
            label = { Text("Email",
                color = MaterialTheme.colorScheme.onBackground) },
            modifier = Modifier
                .padding(10.dp)
        )
        OutlinedTextField(
            value = appViewModel.passwordInput,
            onValueChange = { appViewModel.updatePasswordInput(it) },
            isError = appUiState.incorrectLogin,
            label = { Text("Password",
                color = MaterialTheme.colorScheme.onBackground)},
            modifier = Modifier
                .padding(10.dp)
        )
        Button(
            onClick = { appViewModel.checkLoginDetails() }, modifier= Modifier
                .padding(10.dp)
                .size(width = 200.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary)
        )
        {
            Text(
                "Login",
                style=MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = {},
            modifier= Modifier
                .padding(10.dp)
                .size(width = 200.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary)
        )
        {
            Text(
                "Sign-Up",
                style=MaterialTheme.typography.bodyMedium)
            //TODO: Add sign-up functionality
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
            modifier = modifier,
            style=MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ProfileScreen(navController:NavController,appViewModel:AppViewModel, modifier: Modifier=Modifier){
    val appUiState by appViewModel.uiState.collectAsState()
    val defaultProfilePic = painterResource(R.drawable.defaultprofilepic)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(painter=defaultProfilePic, //TODO: IF Statement checking if logged user has pfp
            contentDescription = "App Logo",
            contentScale = ContentScale.Crop,
            modifier= Modifier
                .padding(4.dp)
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(4.dp, MaterialTheme.colorScheme.onBackground),
                    CircleShape
                )

        )
        Text(
            text = stringResource(R.string.profile_name, appUiState.loggedInUsername),
            modifier = modifier
                .padding(bottom = 12.dp),
            style= MaterialTheme.typography.titleLarge
        )

        Text(
            text = stringResource(R.string.profile_watchlist),
            modifier = modifier
                .padding(8.dp),
            style= MaterialTheme.typography.titleSmall
        )

        //TODO: Add list of watch listed films here

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
        Row(modifier=Modifier
            .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Enable Notifications:",
                modifier = modifier,
                style=MaterialTheme.typography.labelLarge)
            Spacer(modifier=Modifier
                .weight(1f))
            Switch(
                checked = appUiState.enableNotifs,
                onCheckedChange = { appViewModel.updateNotifSetting(it) }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)

        Row(modifier=Modifier
            .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Enable Dark Mode:",
                modifier = modifier,
                style=MaterialTheme.typography.labelLarge)
            Spacer(modifier=Modifier
                .weight(1f))
            Switch(
                checked = appUiState.darkMode,
                onCheckedChange = { appViewModel.updateDarkModeSetting(it) }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(navController: NavController, appViewModel: AppViewModel){
    val appUiState by appViewModel.uiState.collectAsState()



    if(appUiState.isLoggedIn){
        appViewModel.getScreenTitle(navController)
        TopAppBar(
            title = { Text(appUiState.navScreenTitle) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
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
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ){
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (appUiState.navSelectedItem == index) selectedIcons[index] else unselectedIcons[index],
                            contentDescription = item
                        )
                    },
                    label = {Text(
                        item,
                        style=MaterialTheme.typography.labelSmall
                    )},
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
    val navController = rememberNavController()
    val appViewModel: AppViewModel = viewModel()
    val appUiState by appViewModel.uiState.collectAsState()

    FilmSwipeTheme(appUiState.darkMode, navController,appViewModel){
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = { TopNavigationBar(navController, appViewModel)},
            bottomBar = {BottomNavigationBar(navController, appViewModel)}) { innerPadding ->
            Box(modifier=Modifier.padding(innerPadding)) {
                AppNavigator(
                    modifier = Modifier.padding(8.dp),
                    navController = navController,
                    appViewModel = appViewModel
                )
            }
        }
    }
}