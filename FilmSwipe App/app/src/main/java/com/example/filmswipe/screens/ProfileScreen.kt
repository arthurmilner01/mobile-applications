package com.example.filmswipe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel
import kotlin.math.abs

@Composable
fun ProfileScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier){
    LaunchedEffect(Unit){
        appViewModel.getScreenTitle(navController)
    }

    val appUiState by appViewModel.uiState.collectAsState()
    val defaultProfilePic = painterResource(R.drawable.defaultprofilepic)


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(painter=defaultProfilePic, //TODO: IF Statement checking if logged user has pfp
            contentDescription = "App Logo",
            contentScale = ContentScale.Crop,
            modifier= Modifier
                .padding(4.dp)
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(4.dp, MaterialTheme.colorScheme.onBackground),
                    CircleShape
                )

        )
        Text(
            text = stringResource(R.string.profile_name, appUiState.loggedInUsername),
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

        if(appUiState.viewingWatchedMovies){
            //If user clicks liked movies label
            Text("Viewing Watched Movies")
        }
        else
        {
            //If user has watchlist label selected
            Text("Viewing Watchlist")
        }


        //TODO: Add list of watch listed films here

    }
}