package com.example.filmswipe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmswipe.R
import com.example.filmswipe.data.FilmswipeUser
import com.example.filmswipe.data.Movie
import com.example.filmswipe.data.ProfileMovie
import com.example.filmswipe.model.AppViewModel
import kotlin.math.abs

@Composable
fun ProfileScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier, email: String? = null){
    val userEmail = email ?: appViewModel.uiState.collectAsState().value.loggedInEmail
    var userProfile by remember { mutableStateOf<FilmswipeUser?>(null) }

    val watchlistedMovies by appViewModel.watchlistMovies.observeAsState(emptyList())
    val watchedMovies by appViewModel.watchedMovies.observeAsState(emptyList())
    val appUiState by appViewModel.uiState.collectAsState()
    val defaultProfilePic = painterResource(R.drawable.defaultprofilepic)

    LaunchedEffect(userEmail) {
        if(userProfile == null){
            appViewModel.getScreenTitle(navController)
            appViewModel.fetchUserProfileByEmail(userEmail) { user ->
                userProfile = user
            }
            //Functions which update watchlistedMovies and watchedMovies
            appViewModel.usersWatchlistedMovies(userEmail)
            appViewModel.usersWatchedMovies(userEmail)
        }

    }



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(painter=defaultProfilePic, //TODO: IF Statement checking if logged user has pfp
            contentDescription = "App Logo",
            contentScale = ContentScale.Crop,
            modifier= Modifier
                .padding(12.dp)
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(4.dp, MaterialTheme.colorScheme.onBackground),
                    CircleShape
                )

        )
        Text(
            text = userProfile?.username ?: "Loading...",
            modifier = modifier
                .padding(bottom = 12.dp),
            style= MaterialTheme.typography.titleLarge
        )
        Row {
            Text(
                text = stringResource(R.string.profile_watchlist),
                modifier = modifier
                    .padding(8.dp)
                    .clickable { appViewModel.showProfilesWatchlist() },
                style= MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if(!appUiState.viewingWatchedMovies) FontWeight.Bold else FontWeight.Normal
            ))
            Text(
                text = stringResource(R.string.profile_liked),
                modifier = modifier
                    .padding(8.dp)
                    .clickable { appViewModel.showProfilesLikedMovies() },
                style= MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if(appUiState.viewingWatchedMovies) FontWeight.Bold else FontWeight.Normal
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground,
            thickness = 1.dp,
            modifier = Modifier.padding(start=20.dp,end = 20.dp, bottom = 8.dp, top = 8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize()
        ) {
            if(appUiState.viewingWatchedMovies){
                //If user clicks liked movies label
                items(watchedMovies){ watchedMovie ->
                    //For each movie place profile movie composable
                    ProfileMovieCard(watchedMovie, appViewModel, navController)

                }
            }
            else
            {
                //If user has watchlist label selected
                //For each watchlisted movie
                items(watchlistedMovies){ watchlistedMovie ->
                    //For each movie place profile movie composable
                    ProfileMovieCard(watchlistedMovie, appViewModel, navController)

                }

            }
        }


    }
}

@Composable
fun ProfileMovieCard(profileMovie: ProfileMovie, appViewModel: AppViewModel, navController: NavController){
    val ifNotPoster = painterResource(R.drawable.defaultprofilepic)
    val imageUrl = "https://image.tmdb.org/t/p/w500${profileMovie.poster_path ?: ""}"
    Box(
        modifier = Modifier
            .padding(12.dp)
    ){
        if (!profileMovie.poster_path.isNullOrEmpty()){
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .width(100.dp)
                    .clickable {
                        appViewModel.getCurrentMovie(profileMovie.id.toInt(), profileMovie.title, profileMovie.overview, profileMovie.poster_path)
                        navController.navigate("moviedetailsscreen")
                    },
                contentScale = ContentScale.Crop
            )
        }
        else
        {
            Image(
                painter = ifNotPoster,
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .width(100.dp)
                    .clickable {
                        appViewModel.getCurrentMovie(profileMovie.id.toInt(), profileMovie.title, profileMovie.overview, profileMovie.poster_path)
                        navController.navigate("moviedetailsscreen")
                    },
                contentScale = ContentScale.Crop
            )
        }

    }
}