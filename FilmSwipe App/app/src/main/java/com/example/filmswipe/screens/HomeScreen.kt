package com.example.filmswipe.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel

@Composable
fun HomeScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier){
    val appUiState by appViewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.login_email, appUiState.loggedInEmail),
            modifier = modifier,
            style= MaterialTheme.typography.headlineMedium
        )
    }
}
