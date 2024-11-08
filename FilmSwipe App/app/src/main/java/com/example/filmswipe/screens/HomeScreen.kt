package com.example.filmswipe.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel
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
                //TODO: DB call to check film not already in watchlist/watched for user
                //TODO: Also if isLastMovie must handle this
                //Checking if last film
                val isLastMovie = index == movies.size - 1

                appViewModel.checkMovieInWatchlist(movieID = movies[index].id)
                appViewModel.checkMovieInWatched(movieID = movies[index].id)

                //If movie either watched or in watchlist do not display
                //and remove from list
                if(appUiState.movieInWatched || appUiState.movieInWatchlist){
                    appViewModel.removeMovie(index)
                    if(isLastMovie) {
                        appViewModel.fetchPopularMovies()
                    }
                }
                else
                {
                    SwipableCard(
                        navController = navController,
                        title = movies[index].title,
                        subtitle = movies[index].overview,
                        filmImage = movies[index].poster_path,
                        onSwipeLeft = {
                            appViewModel.removeMovie(index)
                            if(isLastMovie) {
                                appViewModel.fetchPopularMovies()
                            } },
                        onSwipeRight = { appViewModel.removeLikedMovie(index)
                            if(isLastMovie) {
                                appViewModel.fetchPopularMovies()
                            } },
                        onSwipeUp = {
                            appViewModel.addMovieToWatchedList(movies[index])
                            appViewModel.removeWatchedMovie(index)
                            if(isLastMovie){
                                appViewModel.fetchPopularMovies()
                            }}
                    )
                    appViewModel.getCurrentMovie(movies[index].id,movies[index].title, movies[index].overview, movies[index].poster_path)
                }
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
    onSwipeRight: () -> Unit = {},
    onSwipeUp: () -> Unit = {}
) {
    //States local to swipeable card so don't need to be in appUiState
    //Offset to track swipe
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    //State to track whether the card is currently being swiped
    var isSwiping by remember { mutableStateOf(false) }
    //How far user must swipe to register either left, right, up or down
    val swipeThreshold = 175f
    //States for handling smooth animation if swipe doesn't go past threshold
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
    val animatedOffsetY by animateFloatAsState(targetValue = offsetY)
    //Using offsetX to slightly rotate the card on left/right swipe
    val rotationAngle = offsetX * 0.02f
    //Using offsetX to slowly fade out card as user swipes
    val cardAlpha = if(isSwiping) 1f - ((abs(offsetX) / (swipeThreshold*2))) else 1f

    //Sets border colour depending on swipe type
    val borderColor = when {
        offsetY < -swipeThreshold / 2 -> Color.Blue.copy(alpha = cardAlpha) //Always blue if card past Y threshold / 2
        offsetX > swipeThreshold / 2 -> Color.Green.copy(alpha = cardAlpha)  //Right
        offsetX < -swipeThreshold / 2 -> Color.Red.copy(alpha = cardAlpha)   //Left
        else -> MaterialTheme.colorScheme.surface                            //Neutral
    }

    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, top = 40.dp, bottom = 40.dp)
            .fillMaxSize(0.9f)
            .offset(x = animatedOffsetX.dp, y= animatedOffsetY.dp)
            .rotate(rotationAngle)  //Rotates card based on rotation angle
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
                    .pointerInput(Unit) { //Handling swiping
                        detectDragGestures(
                            onDragStart = {
                                isSwiping = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x //Tracking amount of drag on X

                                //Allows down swipe but not beyond initial offset.y
                                val minOffsetY = offsetY + dragAmount.y
                                if (minOffsetY <= 0) {
                                    offsetY = minOffsetY
                                }
                            },
                            onDragEnd = {
                                //If card above certain Y always count as swipe up
                                //Else check offset X for left or right swipe
                                if(abs(offsetY) > swipeThreshold){
                                    onSwipeUp()
                                }
                                else if(abs(offsetX) > swipeThreshold) {
                                    //If negative then must be left swipe else right
                                    if (offsetX < 0) onSwipeLeft() else onSwipeRight()
                                }
                                // Reset position and swiping state
                                offsetX = 0f
                                offsetY = 0f
                                isSwiping = false
                            },
                            onDragCancel = {
                                //Resets position
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
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start=12.dp, end = 12.dp, top= 4.dp, bottom = 4.dp)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 12.dp, end=12.dp, bottom =8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
