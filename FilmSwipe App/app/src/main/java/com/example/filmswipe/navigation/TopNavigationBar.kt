package com.example.filmswipe.navigation

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
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
                        text = if(appUiState.viewingHome){ "" } else{ appUiState.navScreenTitle},
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                }
            },
            navigationIcon = {
                // Display back arrow only if viewingMovieDetails is true
                if (appUiState.viewingMovieDetails) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                if (appUiState.viewingHome) {
                    //TODO: On click display filter options
                    IconButton(
                        onClick = { appViewModel.expandFilterMenu() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    DropdownMenu(
                        expanded = appUiState.filterMenuExpanded,
                        onDismissRequest = { appViewModel.dismissFilterMenu() }
                    ) {
                        //TODO: Make these change the appUiState for watchproviderfilter
                        Text(text="Filter by Streaming Service:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier= Modifier
                                .padding(8.dp))
                        DropdownMenuItem(
                            text = {
                                Row{
                                    Text("Disney+")
                                    Switch(checked = appUiState.disneyFilter,
                                        onCheckedChange = null)
                                }},
                            //8 = Netflix, 337 = Disney+, 119 = Prime
                            onClick = {
                                if(appUiState.disneyFilter){
                                    appViewModel.removeStreamingFilter("337")
                                    appViewModel.setDisneyFilter()
                                    appViewModel.fetchPopularMovies()
                                }
                                else{
                                    appViewModel.addStreamingFilter("337")
                                    appViewModel.setDisneyFilter()
                                    appViewModel.fetchPopularMovies()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row{
                                    Text("Amazon Prime")
                                    Switch(checked = appUiState.primeFilter,
                                        onCheckedChange = null)
                            }},
                            onClick = {
                                if(appUiState.primeFilter){
                                    appViewModel.removeStreamingFilter("9")
                                    appViewModel.setPrimeFilter()
                                    appViewModel.fetchPopularMovies()
                                }
                                else{
                                    appViewModel.addStreamingFilter("9")
                                    appViewModel.setPrimeFilter()
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
                                    appViewModel.removeStreamingFilter("8")
                                    appViewModel.setNetflixFilter()
                                    appViewModel.fetchPopularMovies()

                                }
                                else{
                                    appViewModel.addStreamingFilter("8")
                                    appViewModel.setNetflixFilter()
                                    appViewModel.fetchPopularMovies()

                                }
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if(appUiState.viewingHome){ Color.Transparent } else{ MaterialTheme.colorScheme.surface}

            )
        )
    }
}