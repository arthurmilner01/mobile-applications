package com.example.filmswipe.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.filmswipe.R
import com.example.filmswipe.data.FilmswipeUser
import com.example.filmswipe.data.Movie
import com.example.filmswipe.data.ProfileMovie
import com.example.filmswipe.model.AppViewModel
import java.io.ByteArrayOutputStream
import kotlin.math.abs


@Composable
fun ProfileScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier, email: String? = null){

    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }


    val userEmail = email ?: appViewModel.uiState.collectAsState().value.loggedInEmail
    var userProfile by remember { mutableStateOf<FilmswipeUser?>(null) }

    val watchlistedMovies by appViewModel.watchlistMovies.observeAsState(emptyList())
    val watchedMovies by appViewModel.watchedMovies.observeAsState(emptyList())
    val appUiState by appViewModel.uiState.collectAsState()
    val defaultProfilePic = painterResource(R.drawable.defaultprofilepic)



    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
        } else {
            val exception = result.error
        }
    }



    if (imageUri != null) {
        if (Build.VERSION.SDK_INT < 28) {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri!!)
            bitmap = ImageDecoder.decodeBitmap(source)
        }

        //Convert bitmap to Base64 string
        bitmap?.let {
            val base64String = appViewModel.convertBitmapToBase64(it)
            userProfile?.let { profile ->
                appViewModel.updateUserProfilePicture(userEmail, base64String)
                profile.profile_picture = base64String
            }
        }
    }



    LaunchedEffect(userEmail) {
        if(userProfile == null){
            appViewModel.getScreenTitle(navController)
            appViewModel.fetchUserProfileByEmail(userEmail) { user ->
                userProfile = user
                if (user?.profile_picture != null) {
                    bitmap = appViewModel.convertBase64ToBitmap(user.profile_picture!!)
                }
            }
            //Functions which update watchlistedMovies and watchedMovies
            appViewModel.usersWatchlistedMovies(userEmail)
            appViewModel.usersWatchedMovies(userEmail)
        }

    }



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(12.dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(4.dp, MaterialTheme.colorScheme.onBackground),
                        CircleShape
                    )
                    .clickable {
                        if (userEmail == appUiState.loggedInEmail) {
                            imageUri = null
                            imageCropLauncher.launch(
                                CropImageContractOptions(
                                    null,
                                    CropImageOptions()
                                )
                            )
                        }
                    }
            )
        } else {
            Image(
                painter = if (userProfile?.profile_picture != null) {
                    rememberAsyncImagePainter(userProfile!!.profile_picture)
                } else {
                    painterResource(id = R.drawable.defaultprofilepic)
                },
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .padding(12.dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(4.dp, MaterialTheme.colorScheme.onBackground),
                        CircleShape
                    )
                    .clickable {
                        if (userEmail == appUiState.loggedInEmail) {
                            imageUri = null
                            imageCropLauncher.launch(
                                CropImageContractOptions(
                                    null,
                                    CropImageOptions()
                                )
                            )
                        }
                    }
            )
        }


        Text(
            text = userProfile?.username ?: "Loading...",
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize()
        ) {
            if(appUiState.viewingWatchedMovies){
                //If user clicks liked movies label
                items(watchedMovies){ watchedMovie ->
                    //For each movie place profile movie composable
                    ProfileMovieCard(watchedMovie, appViewModel, navController)

                }
            }
            else
            {
                //If user has watchlist label selected
                //For each watchlisted movie
                items(watchlistedMovies){ watchlistedMovie ->
                    //For each movie place profile movie composable
                    ProfileMovieCard(watchlistedMovie, appViewModel, navController)

                }

            }
        }


    }
}

@Composable
fun ProfileMovieCard(profileMovie: ProfileMovie, appViewModel: AppViewModel, navController: NavController){
    val ifNotPoster = painterResource(R.drawable.defaultprofilepic)
    val imageUrl = "https://image.tmdb.org/t/p/w500${profileMovie.poster_path ?: ""}"
    Box(
        modifier = Modifier
            .padding(12.dp)
    ){
        if (!profileMovie.poster_path.isNullOrEmpty()){
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .width(100.dp)
                    .clickable {
                        appViewModel.getCurrentMovie(profileMovie.id.toInt(), profileMovie.title, profileMovie.overview, profileMovie.poster_path)
                        navController.navigate("moviedetailsscreen")
                    },
                contentScale = ContentScale.Crop
            )
        }
        else
        {
            Image(
                painter = ifNotPoster,
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .width(100.dp)
                    .clickable {
                        appViewModel.getCurrentMovie(profileMovie.id.toInt(), profileMovie.title, profileMovie.overview, profileMovie.poster_path)
                        navController.navigate("moviedetailsscreen")
                    },
                contentScale = ContentScale.Crop
            )
        }

    }
}