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
import androidx.compose.material.icons.filled.Lock
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
fun ChangePasswordScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()
    val loginImage = painterResource(R.drawable.filmswipelogo)

    LaunchedEffect(Unit){
        appViewModel.getScreenTitle(navController)
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
            text = "Change Password",
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp)
        )

        AnimatedVisibility(visible = appUiState.isUpdatedPasswordSuccess) {
            Text(
                text = "Password updated successfully!",
                color = Color.Green,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }


        OutlinedTextField(
            value = appViewModel.changePasswordCurrentPasswordInput,
            onValueChange = { appViewModel.updateChangePasswordCurrentPasswordInput(it) },
            isError = appUiState.changePasswordCurrentPasswordError != "",
            label = { Text("Current Password",
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
            visible = appUiState.changePasswordCurrentPasswordError != ""
        ) {
            Text(
                text = appUiState.changePasswordCurrentPasswordError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }

        OutlinedTextField(
            value = appViewModel.changePasswordPasswordInput,
            onValueChange = { appViewModel.updateChangePasswordPasswordInput(it) },
            isError = appUiState.changePasswordPasswordError != "",
            label = { Text("New Password",
                color = MaterialTheme.colorScheme.onBackground)
            },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        AnimatedVisibility(
            visible = appUiState.changePasswordPasswordError != ""
        ) {
            Text(
                text = appUiState.changePasswordPasswordError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }


        OutlinedTextField(
            value = appViewModel.changePasswordConfirmPasswordInput,
            onValueChange = { appViewModel.updateChangePasswordConfirmPasswordInput(it) },
            isError = appUiState.changePasswordConfirmPasswordError != "",
            label = { Text("Confirm New Password",
                color = MaterialTheme.colorScheme.onBackground)
            },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        AnimatedVisibility(
            visible = appUiState.changePasswordConfirmPasswordError != ""
        ) {
            Text(
                text = appUiState.changePasswordConfirmPasswordError ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }



        Button(
            onClick = { appViewModel.checkChangePasswordDetails() }, modifier= Modifier
                .padding(10.dp)
                .size(width = 200.dp, height = 50.dp),
            enabled = appViewModel.changePasswordCurrentPasswordInput.isNotEmpty() &&
                    appViewModel.changePasswordPasswordInput.isNotEmpty() &&
                    appViewModel.changePasswordConfirmPasswordInput.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary)
        )
        {
            Text(
                "Confirm Changes",
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
                text = "Don't want to edit your password?",
                color = Color.White,
            )
            TextButton(onClick = { navController.navigate("settingsscreen") }) {
                Text(
                    text = "Return to Settings",
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}