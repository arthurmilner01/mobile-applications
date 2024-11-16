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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmswipe.R
import com.example.filmswipe.data.CastMember
import com.example.filmswipe.data.CrewMember
import com.example.filmswipe.model.AppViewModel

@Composable
fun MovieDetailsScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()

    val cast by appViewModel.cast.observeAsState(emptyList())
    val crew by appViewModel.crew.observeAsState(emptyList())

    val castLoading by appViewModel.creditsLoading.observeAsState(false)
    val castError by appViewModel.creditsError.observeAsState()

    LaunchedEffect(Unit){
        appViewModel.getScreenTitle(navController)
        appViewModel.fetchMovieCredits(appUiState.currentMovieID)
        appViewModel.fetchMovieDetails(appUiState.currentMovieID)
        appViewModel.checkMovieInWatchlist(movieID = appUiState.currentMovieID)
        appViewModel.checkMovieInWatched(movieID = appUiState.currentMovieID)
    }

    val imageUrl = "https://image.tmdb.org/t/p/w500${appUiState.currentMoviePosterPath ?: ""}"
    val director = crew.find{ it.job == "Director" } //Finds crew member who is the director


    val ratingBackgroundColor = when {
        appUiState.currentMovieIMDBRating >= 8.0 -> Color(0xFF4CC452) //Green for high ratings
        appUiState.currentMovieIMDBRating >= 5.0 -> Color(0xFFDCA60D) //Yellow for medium ratings
        else -> Color(0xFFF44336)              //Red for low ratings
    }

    //Opening movie detail in browser
    val context = LocalContext.current

    Column(modifier=Modifier.fillMaxSize()) {

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
                                val tmdbUrl = "https://www.themoviedb.org/movie/${appUiState.currentMovieID}"
                                //https://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application
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
                        Text(
                            text = appUiState.currentMovieGenres.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge
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

                        Crossfade(targetState = appUiState.movieInWatched,
                            animationSpec = tween(durationMillis = 1000)) { movieInWatched ->
                            if (movieInWatched) {
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            appViewModel.removeMovieFromWatched(appUiState.currentMovieID)
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
                                            appViewModel.addMovieToWatched(
                                                appUiState.currentMovieID,
                                                appUiState.currentMovieTitle,
                                                appUiState.currentMovieOverview,
                                                appUiState.currentMoviePosterPath
                                            )
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

                        Crossfade(targetState = appUiState.movieInWatchlist,
                            animationSpec = tween(durationMillis = 1000)) { movieInWatchlist ->
                            if(appUiState.movieInWatchlist){
                            Column(modifier = Modifier
                                .clickable {
                                    appViewModel.removeMovieFromWatchlist(appUiState.currentMovieID)
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
                                        appViewModel.addMovieToWatchlist(appUiState.currentMovieID, appUiState.currentMovieTitle, appUiState.currentMovieOverview, appUiState.currentMoviePosterPath)
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
                                .clickable { appViewModel.showMovieCast() }
                                .padding(12.dp)
                                .background(
                                    color = if (!appUiState.viewingMovieCrew) MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.5f
                                    ) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ){
                            Text(
                                text = "Cast",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (!appUiState.viewingMovieCrew) FontWeight.Bold else FontWeight.Normal
                                )

                            )
                        }
                        Box(
                            modifier = Modifier
                                .clickable { appViewModel.showMovieCrew() }
                                .padding(12.dp)
                                .background(
                                    color = if (appUiState.viewingMovieCrew) MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.5f
                                    ) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ){
                            Text(
                                text = "Crew",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (appUiState.viewingMovieCrew) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            when {
                castLoading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                castError != null -> {
                    item {
                        Text(text = "Error loading cast: $castError")
                    }
                }

                else -> {
                    if(appUiState.viewingMovieCrew){
                        items(crew) { crewMember ->
                            CrewMemberListItem(crewMember)
                        }
                    }
                    else{
                        items(cast) { castMember ->
                            CastMemberListItem(castMember)
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