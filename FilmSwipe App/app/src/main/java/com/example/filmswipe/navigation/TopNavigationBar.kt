package com.example.filmswipe.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.filmswipe.model.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(navController: NavController, appViewModel: AppViewModel){
    val appUiState by appViewModel.uiState.collectAsState()


    //If user is logged in display top nav
    if(appUiState.isLoggedIn){
        appViewModel.getScreenTitle(navController)
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        //If on home or changing password do not display a title
                        text = if(appUiState.viewingHome || appUiState.viewingChangePassword){ "" } else{ appUiState.navScreenTitle},
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                }
            },
            navigationIcon = {
                //Display back arrow only if viewing movie details or changing password
                if (appUiState.viewingMovieDetails || appUiState.viewingChangePassword) {
                    IconButton(onClick = {
                        //Goes to previous screen
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                //If viewing home
                if (appUiState.viewingHome) {
                    IconButton(
                        //Update UI state to display filter menu
                        onClick = { appViewModel.expandFilterMenu() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    //Streaming service ID's 8 = Netflix, 337 = Disney+, 9 = Prime
                    DropdownMenu(
                        //Expanded when filterMenuExpanded is true
                        expanded = appUiState.filterMenuExpanded,
                        //Minimises streaming filter menu/sets filterMenuExpanded to false
                        onDismissRequest = { appViewModel.dismissFilterMenu() }
                    ) {
                        Text(text="Filter by Streaming Service:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier= Modifier
                                .padding(8.dp))
                        DropdownMenuItem(
                            text = {
                                Row{
                                    Text("Disney+")
                                    //Updates switch appearance
                                    Switch(checked = appUiState.disneyFilter,
                                        onCheckedChange = null)
                                }},
                            onClick = {
                                if(appUiState.disneyFilter){
                                    //Removes disney filter
                                    appViewModel.removeStreamingFilter("337")
                                    //Set disney filter to false
                                    appViewModel.setDisneyFilter()
                                    //Fetches new filtered movies to display on home
                                    appViewModel.fetchPopularMovies()
                                }
                                else{
                                    //Add disney filter
                                    appViewModel.addStreamingFilter("337")
                                    //Set disney filter to true
                                    appViewModel.setDisneyFilter()
                                    //Fetches new filtered movies to display on home
                                    appViewModel.fetchPopularMovies()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row{
                                    Text("Amazon Prime")
                                    //Updates switch appearance
                                    Switch(checked = appUiState.primeFilter,
                                        onCheckedChange = null)
                            }},
                            onClick = {
                                if(appUiState.primeFilter){
                                    //Removes prime filter
                                    appViewModel.removeStreamingFilter("9")
                                    //Set prime filter to false
                                    appViewModel.setPrimeFilter()
                                    //Fetches new filtered movies to display on home
                                    appViewModel.fetchPopularMovies()
                                }
                                else{
                                    //Add prime filter
                                    appViewModel.addStreamingFilter("9")
                                    //Set prime filter to true
                                    appViewModel.setPrimeFilter()
                                    //Fetches new filtered movies to display on home
                                    appViewModel.fetchPopularMovies()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row{
                                    Text("Netflix")
                                    Switch(checked = appUiState.netflixFilter,
                                        onCheckedChange = null)
                                }
                            },
                            onClick = {
                                if(appUiState.netflixFilter){
                                    //Remove netflix filter
                                    appViewModel.removeStreamingFilter("8")
                                    //Set netflix filter to false
                                    appViewModel.setNetflixFilter()
                                    //Fetches new filtered movies to display on home
                                    appViewModel.fetchPopularMovies()

                                }
                                else{
                                    //Add netflix filter
                                    appViewModel.addStreamingFilter("8")
                                    //Set netflix filter to true
                                    appViewModel.setNetflixFilter()
                                    //Fetches new filtered movies to display on home
                                    appViewModel.fetchPopularMovies()

                                }
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                //If viewing home make top bar transparent
                containerColor = if(appUiState.viewingHome){ Color.Transparent } else{ MaterialTheme.colorScheme.surface}

            )
        )
    }
}