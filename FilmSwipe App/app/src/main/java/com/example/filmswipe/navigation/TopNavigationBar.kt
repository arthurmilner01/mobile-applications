package com.example.filmswipe.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.filmswipe.model.AppViewModel

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