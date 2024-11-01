package com.example.filmswipe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel

@Composable
fun MovieDetailsScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit){
        appViewModel.getScreenTitle(navController)
    }

    val appUiState by appViewModel.uiState.collectAsState()
    val imageUrl = "https://image.tmdb.org/t/p/w500${appUiState.currentMoviePosterPath ?: ""}"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Column(modifier = Modifier
            .weight(1f)){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .height(200.dp)
                        .width(150.dp)
                        .border(4.dp, MaterialTheme.colorScheme.surface)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(
                        R.string.movie_details_title,
                        appUiState.currentMovieTitle
                    ),
                    style = MaterialTheme.typography.titleLarge
                )

            }
        }
        Column(
            modifier = Modifier
                .weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(
                    R.string.movie_details_overview,
                    appUiState.currentMovieOverview
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}