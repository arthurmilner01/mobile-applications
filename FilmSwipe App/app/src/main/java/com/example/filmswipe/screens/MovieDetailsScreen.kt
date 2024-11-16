package com.example.filmswipe.screens

import android.content.Intent
import android.graphics.Paint.Align
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmswipe.R
import com.example.filmswipe.data.CastMember
import com.example.filmswipe.data.CrewMember
import com.example.filmswipe.model.AppViewModel

@Composable
fun MovieDetailsScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    //App ui state instance
    val appUiState by appViewModel.uiState.collectAsState()
    //Reference for live data store of cast and crew details
    val cast by appViewModel.cast.observeAsState(emptyList())
    val crew by appViewModel.crew.observeAsState(emptyList())
    //If api call fails or is loading
    val castLoading by appViewModel.creditsLoading.observeAsState(false)
    val castError by appViewModel.creditsError.observeAsState()

    LaunchedEffect(Unit){
        //Get screen title to update top nav
        appViewModel.getScreenTitle(navController)
        //Api call to fetch movie cast/crew
        appViewModel.fetchMovieCredits(appUiState.currentMovieID)
        //Api call to fetch movie details
        appViewModel.fetchMovieDetails(appUiState.currentMovieID)
        //Checks movie is/isn't in watchlist to update add/remove icon accordingly
        appViewModel.checkMovieInWatchlist(movieID = appUiState.currentMovieID)
        //Checks movie is/isn't in watched to update add/remove icon accordingly
        appViewModel.checkMovieInWatched(movieID = appUiState.currentMovieID)
    }

    //URL for the movie poster
    val imageUrl = "https://image.tmdb.org/t/p/w500${appUiState.currentMoviePosterPath ?: ""}"
    //Gets the director from the crew data
    val director = crew.find{ it.job == "Director" }

    //Sets background colour of film rating
    val ratingBackgroundColor = when {
        appUiState.currentMovieIMDBRating >= 8.0 -> Color(0xFF4CC452) //Green for high ratings
        appUiState.currentMovieIMDBRating >= 5.0 -> Color(0xFFDCA60D) //Yellow for medium ratings
        else -> Color(0xFFF44336)              //Red for low ratings
    }

    //Opening movie detail in browser
    val context = LocalContext.current

    Column(modifier=Modifier.fillMaxSize()) {
        //Lazy column for displaying cast/crew
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {

                    //Background/header image
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    //Box to dim the background image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f))
                    )

                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        //Movie poster
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .height(150.dp)
                                .width(100.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp)) //Space between title and poster
                        Column {
                            Text(
                                text = stringResource(
                                    R.string.movie_details_title,
                                    appUiState.currentMovieTitle
                                ),
                                style = MaterialTheme.typography.titleLarge
                            )
                            //Checks if director was found in crew, if not doesn't display
                            if (director != null) {
                                Text(
                                    text=stringResource(R.string.movie_director,director.name),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(13.dp)
                            .clickable{
                                //TODO: Cite web browser source here from bibliography
                                val tmdbUrl = "https://www.themoviedb.org/movie/${appUiState.currentMovieID}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tmdbUrl))
                                context.startActivity(intent)
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(ratingBackgroundColor),
                                contentAlignment = Alignment.Center
                            ) {
                                //Display movie rating
                                Text(
                                    text = "${appUiState.currentMovieIMDBRating}★",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                            }
                        }
                    }
                }
            }


            item {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Genres:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        //Displays movie genres, if none found display no genres available
                        Text(
                            text = if (appUiState.currentMovieGenres.isNotEmpty()) {
                                appUiState.currentMovieGenres.joinToString(", ")
                            } else {
                                "No genres available"
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                //Error styling if none found
                                color = if (appUiState.currentMovieGenres.isNotEmpty()) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        )

                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            R.string.movie_details_overview,
                            appUiState.currentMovieOverview
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        //Crossfade animation to smoothly switch icons between add/remove
                        //Crossfade based on if movie is in watched
                        Crossfade(targetState = appUiState.movieInWatched,
                            //Animation speed
                            animationSpec = tween(durationMillis = 1000)) { movieInWatched ->
                            if (movieInWatched) {
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            //Removes the movie from the users watched in db
                                            appViewModel.removeMovieFromWatched(appUiState.currentMovieID)
                                            //Checks the movie is in watched to update UI state
                                            appViewModel.checkMovieInWatched(appUiState.currentMovieID)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Remove Watched Icon",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp)
                                    )
                                    Text("Remove", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            //Adds movie to watched in db
                                            appViewModel.addMovieToWatched(
                                                appUiState.currentMovieID,
                                                appUiState.currentMovieTitle,
                                                appUiState.currentMovieOverview,
                                                appUiState.currentMoviePosterPath
                                            )
                                            //Checks the movie is in watched to update UI state
                                            appViewModel.checkMovieInWatched(appUiState.currentMovieID)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Mark as Watched Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp)
                                    )
                                    Text("Add", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        //Crossfade based on if movie is in watchlist
                        Crossfade(targetState = appUiState.movieInWatchlist,
                            animationSpec = tween(durationMillis = 1000)) { movieInWatchlist ->
                            if(appUiState.movieInWatchlist){
                            Column(modifier = Modifier
                                .clickable {
                                    //Removes movie from watchlist in db
                                    appViewModel.removeMovieFromWatchlist(appUiState.currentMovieID)
                                    //Checks movie in watchlist to update UI state
                                    appViewModel.checkMovieInWatchlist(appUiState.currentMovieID)
                                },
                                horizontalAlignment = Alignment.CenterHorizontally){
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Watchlist Icon Remove",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                )
                                Text("Remove", style = MaterialTheme.typography.labelSmall)
                            }
                            }
                            else
                            {
                                Column(modifier = Modifier
                                    .clickable {
                                        //Adds movie to watchlist
                                        appViewModel.addMovieToWatchlist(appUiState.currentMovieID,
                                            appUiState.currentMovieTitle,
                                            appUiState.currentMovieOverview,
                                            appUiState.currentMoviePosterPath)
                                        //Checks movie in watchlist to update UI state
                                        appViewModel.checkMovieInWatchlist(appUiState.currentMovieID)
                                    },
                                    horizontalAlignment = Alignment.CenterHorizontally){
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Watchlist Icon Add",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp)
                                    )
                                    Text("Add", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    //Updates UI state so movie cast is shown below
                                    appViewModel.showMovieCast()
                                }
                                .padding(12.dp)
                                .background(
                                    //Styling if currently selected
                                    color = if (!appUiState.viewingMovieCrew) MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.5f
                                    ) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ){
                            Text(
                                text = "Cast",
                                //Styling if currently selected
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (!appUiState.viewingMovieCrew) FontWeight.Bold else FontWeight.Normal
                                )

                            )
                        }
                        Box(
                            modifier = Modifier
                                .clickable {
                                    //Updates UI state so movie crew is shown below
                                    appViewModel.showMovieCrew()
                                }
                                .padding(12.dp)
                                .background(
                                    //Styling if currently selected
                                    color = if (appUiState.viewingMovieCrew) MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.5f
                                    ) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ){
                            Text(
                                text = "Crew",
                                //Styling if currently selected
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (appUiState.viewingMovieCrew) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            when {
                //If API call is still loading
                castLoading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }
                //If API call encounters error
                castError != null -> {
                    item {
                        Text(text = "Error loading cast: $castError")
                    }
                }

                else -> {
                    //If viewing movie crew
                    if(appUiState.viewingMovieCrew){
                        //If no movie crew found display this
                        if (crew.isEmpty()) {
                            item {
                                Text(
                                    text = "No crew available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            //For each crew member create crew member list item
                            items(crew) { crewMember ->
                                CrewMemberListItem(crewMember)
                            }
                        }
                    }
                    //If viewing movie cast
                    else {
                        //If no cast found display this
                        if (cast.isEmpty()) {
                            item {
                                Text(
                                    text = "No cast available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            //For each cast member create cast member list item
                            items(cast) { castMember ->
                                CastMemberListItem(castMember)
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun CastMemberListItem(castMember: CastMember){
    //Prefix for url of cast pic
    val profileImageUrl = "https://image.tmdb.org/t/p/w200${castMember.profile_path}"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(start = 12.dp, end = 12.dp)
    ){
        Image(
            //If no image found display placeholder
            painter = if(castMember.profile_path != null)
            {rememberAsyncImagePainter(profileImageUrl)}
            else { painterResource(R.drawable.defaultprofilepic) },
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(4.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = castMember.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            //Displays the character of the cast member if one is returned
            if(castMember.character.isNotEmpty()){
                Text(
                    text = "as ${castMember.character}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            else{
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

        }
    }
    HorizontalDivider(
        modifier=Modifier
            .padding(start = 20.dp, end=20.dp),
        thickness = 1.dp,
        color = Color.Transparent)
}

@Composable
fun CrewMemberListItem(crewMember: CrewMember){
    //Prefix for url of cast pic
    val profileImageUrl = "https://image.tmdb.org/t/p/w200${crewMember.profile_path}"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(start = 12.dp, end = 12.dp)
    ){
        Image(
            //If no image found use placeholder
            painter = if(crewMember.profile_path != null)
            {rememberAsyncImagePainter(profileImageUrl)}
            else { painterResource(R.drawable.defaultprofilepic) },
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(4.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = crewMember.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            //If crew member job is returned display here
            if(crewMember.job.isNotEmpty()){
                Text(
                    text = "as ${crewMember.job}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            else{
                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
    HorizontalDivider(
        modifier=Modifier
            .padding(start = 20.dp, end=20.dp),
        thickness = 1.dp,
        color = Color.Transparent)
}