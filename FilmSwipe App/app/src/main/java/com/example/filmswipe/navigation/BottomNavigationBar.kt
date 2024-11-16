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
    //Labels for bottom nav icons
    val items = listOf("Home", "Search", "Profile","Settings")
    //Icons to display when user is on the specific page
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Search, Icons.Filled.Person, Icons.Filled.Settings)
    //Icons to display when user is not on the specific page
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.Search, Icons.Outlined.Person, Icons.Outlined.Settings)

    //If user is logged in and not changing their password display top nav
    if(appUiState.isLoggedIn && !appUiState.viewingChangePassword){
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ){
            //For each icon
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            //If navigated to specific page show icon as selected
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
                    //Further styling when selected
                    selected = index == appUiState.navSelectedItem,
                    onClick = {
                        //When bottom nav is used change currently selected icon
                        appViewModel.changeNavSelectedItem(index)
                        //Navigate to the screen of the icon selected
                        navController.navigate(route = item.plus("screen"))
                    }
                )
            }
        }
    }
}