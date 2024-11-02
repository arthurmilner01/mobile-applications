package com.example.filmswipe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel
import com.example.filmswipe.model.Film
import kotlin.math.abs

@Composable
fun HomeScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()
    val movies by appViewModel.movies.observeAsState(emptyList())
    val loading by appViewModel.loading.observeAsState(initial = false)
    val error by appViewModel.error.observeAsState(initial = null)

    LaunchedEffect(Unit){
        appViewModel.getScreenTitle(navController)
        if (movies.isEmpty()) {
            appViewModel.fetchPopularMovies()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if(loading){
            CircularProgressIndicator() //Loading icon if API not yet responded
        }
        else if(error != null){ //If error in loading films display this
            Column {
                Text(text = "Error loading movies please try again.")
                Button(onClick = { appViewModel.fetchPopularMovies() }) {
                    Text(text = "Reload")
                }
            }
        }
        else{ //If API call is successful
            for (index in movies.indices.reversed()) {
                SwipableCard(
                    navController = navController,
                    title = movies[index].title,
                    subtitle = movies[index].overview,
                    filmImage = movies[index].poster_path,
                    onSwipeLeft = { appViewModel.removeMovie(index) },
                    onSwipeRight = { appViewModel.removeLikedMovie(index) },
                )
                appViewModel.getCurrentMovie(movies[index].title, movies[index].overview, movies[index].poster_path)
            }
        }
    }
}

@Composable
fun SwipableCard(
    navController: NavController,
    title: String,
    subtitle: String,
    filmImage: String?,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    val swipeThreshold = 150f

    val rotationAngle = offsetX * 0.02f
    val cardAlpha = if(isSwiping) 1f - ((abs(offsetX) / (swipeThreshold*2))) else 1f

    //Change border color based on swipe direction and intensity
    //OLD: val borderColor = if (isSwiping) MaterialTheme.colorScheme.tertiary.copy(alpha=cardAlpha) else MaterialTheme.colorScheme.surface
    val borderColor = when {
        offsetX > swipeThreshold / 2 -> Color.Green.copy(alpha = cardAlpha)  //Right
        offsetX < -swipeThreshold / 2 -> Color.Red.copy(alpha = cardAlpha)   //Left
        else -> MaterialTheme.colorScheme.surface                            //Neutral
    }

    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, top = 40.dp, bottom = 40.dp)
            .fillMaxSize()
            .offset(x = offsetX.dp)
            .rotate(rotationAngle)  // Rotate the entire Box, including the border
            .border(BorderStroke(4.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable { navController.navigate("moviedetailsscreen") }
    ){
        Card(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .alpha(cardAlpha),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isSwiping = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                            },
                            onDragEnd = {
                                // Check if the swipe exceeds the threshold
                                if (abs(offsetX) > swipeThreshold) {
                                    if (offsetX < 0) onSwipeLeft() else onSwipeRight()
                                }
                                // Reset position and swiping state
                                offsetX = 0f
                                isSwiping = false
                            },
                            onDragCancel = {
                                // Reset position and swiping state on cancel
                                offsetX = 0f
                                isSwiping = false
                            }
                        )
                    }
            ) {
                val imageUrl = "https://image.tmdb.org/t/p/w500${filmImage ?: ""}"
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(12.dp)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
