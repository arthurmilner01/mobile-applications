package com.example.filmswipe.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.filmswipe.model.AppViewModel

@Composable
fun SettingsScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier){
    val appUiState by appViewModel.uiState.collectAsState()

    LaunchedEffect(!appUiState.isLoggedIn) {
        if (!appUiState.isLoggedIn) {
            appViewModel.changeNavSelectedItem(0)
            navController.navigate("loginscreen")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(modifier= Modifier
            .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Enable Notifications:",
                modifier = modifier,
                style= MaterialTheme.typography.labelLarge)
            Spacer(modifier= Modifier
                .weight(1f))
            Switch(
                checked = appUiState.enableNotifs,
                onCheckedChange = { appViewModel.updateNotifSetting(it) }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)

        Row(modifier= Modifier
            .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Enable Dark Mode:",
                modifier = modifier,
                style= MaterialTheme.typography.labelLarge)
            Spacer(modifier= Modifier
                .weight(1f))
            Switch(
                checked = appUiState.darkMode,
                onCheckedChange = { appViewModel.updateDarkModeSetting(it) }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)

        Row(modifier= Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clickable { appViewModel.userLogsOut() },
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Log-Out",
                modifier = modifier,
                style= MaterialTheme.typography.labelLarge)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground, thickness = 1.dp)

        Row(modifier= Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clickable {  }, //TODO: Make functional (passing activity context by view model??)
            verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Quit Application",
                modifier = modifier,
                style= MaterialTheme.typography.labelLarge)
        }

    }
}