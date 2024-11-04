package com.example.filmswipe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmswipe.data.Movie
import com.example.filmswipe.model.AppViewModel

@Composable
fun SearchScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {

    val appUiState by appViewModel.uiState.collectAsState()
    val searchResults by appViewModel.searchResults.observeAsState(emptyList())

    LaunchedEffect(Unit){
        appViewModel.getScreenTitle(navController)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = appViewModel.searchText,
                onValueChange = { appViewModel.updateSearchQuery(it) },
                label = { Text("Search...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Button(
                onClick = {
                    if(appUiState.searchingUsers){
                        appViewModel.performUserSearch()
                    }
                    else{
                        appViewModel.searchMoviesByTitle()
                    }
                          },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(height = 56.dp, width = 100.dp)
            ) {
                Text("Search")
            }
        }
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = appUiState.searchingUsers,
                onCheckedChange = { appViewModel.changeSearchType(it) }
            )
            Text(text="Search for users?", style = MaterialTheme.typography.labelMedium)
        }

        LazyColumn {
            if(appUiState.searchingUsers){
                if(appViewModel.searchText == ""){
                    item{
                        Text(
                            text = "User search results will appear here.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
            else
            {
                if(appViewModel.searchText == ""){
                    item{
                        Text(
                            text = "Search results will appear here.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                else{
                    if (searchResults.isEmpty()) {
                        item {
                            Text(
                                text = "Hit search to confirm your input. If this message remains please try a different search term.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        items(searchResults) { movie ->
                            MovieItem(movie, navController, appViewModel)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun MovieItem(movie: Movie, navController: NavController, appViewModel: AppViewModel){
    val searchPoster = "https://image.tmdb.org/t/p/w200${movie.poster_path}"

    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(75.dp)
            .clickable {
                //TODO: Navigate to movie details screen pass movie.id
                appViewModel.getCurrentMovie(movie.id, movie.title, movie.overview, movie.poster_path)
                navController.navigate("moviedetailsscreen")
            }
            .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(searchPoster),
            contentDescription = null,
            modifier = Modifier
                .height(75.dp)
                .width(66.dp)
                .padding(8.dp)
        )
        Text(text = movie.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start=20.dp)
            )
    }
}

