package com.example.filmswipe.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.filmswipe.model.AppViewModel

@Composable
fun BottomNavigationBar(navController: NavController, appViewModel: AppViewModel){
    val appUiState by appViewModel.uiState.collectAsState()
    val items = listOf("Home", "Search", "Profile","Settings")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Search, Icons.Filled.Person, Icons.Filled.Settings)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Search, Icons.Outlined.Person, Icons.Outlined.Settings)

    if(appUiState.isLoggedIn && !appUiState.viewingChangePassword){
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
                    label = {
                        Text(
                        item,
                        style= MaterialTheme.typography.labelSmall
                    )
                    },
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