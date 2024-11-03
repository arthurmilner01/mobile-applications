package com.example.filmswipe.screens

import android.graphics.Paint.Align
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
    }

    val imageUrl = "https://image.tmdb.org/t/p/w500${appUiState.currentMoviePosterPath ?: ""}"
    val director = crew.find{ it.job == "Director" } //Finds crew member who is the director

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
                                .width(100.dp)
                                .border(2.dp, MaterialTheme.colorScheme.onBackground)
                                .clip(RoundedCornerShape(8.dp)),
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
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )

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
                    //TODO: ADD STATE THAT DISPLAYS EITHER CAST OR CREW

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
            .background(MaterialTheme.colorScheme.surface)
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
            Text(
                text = "as ${castMember.character}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
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
            .background(MaterialTheme.colorScheme.surface)
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
            Text(
                text = "as ${crewMember.job}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
    HorizontalDivider(
        modifier=Modifier
            .padding(start = 20.dp, end=20.dp),
        thickness = 1.dp,
        color = Color.Transparent)
}