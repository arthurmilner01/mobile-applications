package com.example.filmswipe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel

@Composable
fun SignUpScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()

    LaunchedEffect(appUiState.isSignedUp) {
        if (appUiState.isSignedUp) {
            navController.navigate("loginscreen")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
    )
    {
        Text("Sign-Up",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = appViewModel.signUpEmailInput,
            onValueChange = { appViewModel.updateSignUpEmailInput(it) },
            isError = appUiState.incorrectSignUp,
            label = { Text("Email",
                color = MaterialTheme.colorScheme.onBackground) },
            modifier = Modifier
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = appViewModel.signUpUsernameInput,
            onValueChange = { appViewModel.updateSignUpUsernameInput(it) },
            isError = appUiState.incorrectSignUp,
            label = { Text("Username",
                color = MaterialTheme.colorScheme.onBackground)
            },
            modifier = Modifier
                .padding(10.dp)
        )

        OutlinedTextField(
            value = appViewModel.signUpPasswordInput,
            onValueChange = { appViewModel.updateSignUpPasswordInput(it) },
            isError = appUiState.incorrectSignUp,
            label = { Text("Password",
                color = MaterialTheme.colorScheme.onBackground)
            },
            modifier = Modifier
                .padding(10.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = { appViewModel.checkSignUpDetails() }, modifier= Modifier
                .padding(10.dp)
                .size(width = 200.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary)
        )
        {
            Text(
                "Sign-Up",
                style= MaterialTheme.typography.bodyMedium
            )
        }
    }
}