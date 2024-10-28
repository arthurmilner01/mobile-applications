package com.example.filmswipe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel

@Composable
fun ProfileScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier){
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

        Text(
            text = stringResource(R.string.profile_watchlist),
            modifier = modifier
                .padding(8.dp),
            style= MaterialTheme.typography.titleSmall
        )

        //TODO: Add list of watch listed films here

    }
}