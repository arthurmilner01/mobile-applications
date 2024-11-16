package com.example.filmswipe.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel

@Composable
fun SignUpScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()
    val loginImage = painterResource(R.drawable.filmswipelogo)


    LaunchedEffect(appUiState.isSignedUp) {
        if (appUiState.isSignedUp) {
            navController.navigate("loginscreen")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {


        Image(painter=loginImage,
            contentDescription = "App Logo",
            modifier= Modifier
                .padding(
                    top = 10.dp,
                    bottom = 10.dp,
                )
                .size(185.dp)
        )

        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )



        OutlinedTextField(
            value = appViewModel.signUpEmailInput,
            onValueChange = { appViewModel.updateSignUpEmailInput(it) },
            isError = appUiState.emailError != "",
            label = { Text("Email",
                color = MaterialTheme.colorScheme.onBackground) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        AnimatedVisibility(
            visible = appUiState.emailError != ""
        )
        {
            Text(
                text = appUiState.emailError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }


        OutlinedTextField(
            value = appViewModel.signUpUsernameInput,
            onValueChange = { appViewModel.updateSignUpUsernameInput(it) },
            isError = appUiState.usernameError != "",
            label = { Text("Username",
                color = MaterialTheme.colorScheme.onBackground)
            },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
        )
        AnimatedVisibility(
            visible = appUiState.usernameError != ""
        )
        {
            Text(
                text = appUiState.usernameError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }

        OutlinedTextField(
            value = appViewModel.signUpPasswordInput,
            onValueChange = { appViewModel.updateSignUpPasswordInput(it) },
            isError = appUiState.passwordError != "",
            label = { Text("Password",
                color = MaterialTheme.colorScheme.onBackground)
            },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        AnimatedVisibility(
            visible = appUiState.passwordError != ""
        ) {
            Text(
                text = appUiState.passwordError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }




        Button(
            onClick = { appViewModel.checkSignUpDetails() }, modifier= Modifier
                .padding(10.dp)
                .size(width = 200.dp, height = 50.dp),
            enabled = appViewModel.signUpEmailInput.isNotEmpty() &&
                    appViewModel.signUpUsernameInput.isNotEmpty() &&
                    appViewModel.signUpPasswordInput.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary)
        )
        {
            Text(
                "Sign-Up",
                style= MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }


        Spacer(Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account?",
                color = Color.White,
            )
            TextButton(onClick = { navController.navigate("loginScreen") }) {
                Text(
                    text = "Login",
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}