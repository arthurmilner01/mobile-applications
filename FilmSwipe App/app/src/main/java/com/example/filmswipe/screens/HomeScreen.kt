package com.example.filmswipe.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmswipe.model.AppViewModel
import kotlin.math.abs

//TODO: Fix the refresh issue, potentially because of how it checks last movie

@Composable
fun HomeScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val appUiState by appViewModel.uiState.collectAsState()
    //Observing live data for movies
    val movies by appViewModel.movies.observeAsState(emptyList())
    //For when API call is loading
    val loading by appViewModel.loading.observeAsState(initial = false)
    //For when API call errors
    val error by appViewModel.error.observeAsState(initial = null)

    BackHandler {  }

    LaunchedEffect(Unit){
        //Get screen title for top nav
        appViewModel.getScreenTitle(navController)
        //If there are no movies to swipe fetch more movies
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
        //Loading icon if API not yet responded
        if(loading){
            CircularProgressIndicator()
        }
        //If error in loading films display this
        else if(error != null){
            Column {
                Text(text = "Error loading movies please try again.")
                //Button to try fetch movies again
                Button(onClick = { appViewModel.fetchPopularMovies() }) {
                    Text(text = "Reload")
                }
            }
        }
        else{
            //If API call is successful
            //For each movie in movies, reversed as makes it easier to work with
            for (index in movies.indices.reversed()) {
                //TODO: LOOK AT THIS TO SEE ABOUT FIXING THE REFRESH ISSUE

                //Checking if last movie in movies
                val isLastMovie = index == movies.size - 1
                //Check if current movie is in users watchlist/watched
                appViewModel.checkMovieInWatchlist(movieID = movies[index].id)
                appViewModel.checkMovieInWatched(movieID = movies[index].id)

                //If movie either watched or in watchlist do not display
                //and remove from list
                if(appUiState.movieInWatched || appUiState.movieInWatchlist){
                    appViewModel.removeMovie(index)
                    //TODO: CHANGE HOW THIS WORKS AS I THINK THIS IS CAUSE OF REFRESH??
                    //If the last movie has already been seen/watched fetch more movies
                    //as wont be able to apply this login to the swipe-able card swipe
                    //functions
                    if(isLastMovie) {
                        appViewModel.fetchPopularMovies()
                    }
                }
                else
                {
                    //Creates swipe-able card from current movie if not already in watched or watchlist
                    SwipableCard(
                        navController = navController,
                        movieId = movies[index].id,
                        title = movies[index].title,
                        subtitle = movies[index].overview,
                        filmImage = movies[index].poster_path,
                        voteAverage =  movies[index].vote_average,
                        onSwipeLeft = {
                            //When movie is swiped left remove it from the movie list
                            appViewModel.removeMovie(index)
                            //If this is the last movie fetch more movies
                            if(isLastMovie) {
                                appViewModel.fetchPopularMovies()
                            } },
                        onSwipeRight = {
                            //When movie is swiped right add movie to watchlist
                            appViewModel.addMovieToWatchlist(movies[index].id,movies[index].title, movies[index].overview, movies[index].poster_path)
                            //Remove movie from movie list
                            appViewModel.removeMovie(index)
                            //If this is the last movie fetch more movies
                            if(isLastMovie) {
                                appViewModel.fetchPopularMovies()
                            } },
                        onSwipeUp = {
                            //When movie is swiped up add movie to watched
                            appViewModel.addMovieToWatched(movies[index].id,movies[index].title, movies[index].overview, movies[index].poster_path)
                            //Remove movie from movie list
                            appViewModel.removeMovie(index)
                            //If this is the last movie fetch more movies
                            if(isLastMovie){
                                appViewModel.fetchPopularMovies()
                            }}
                    )
                    //Update UI state to details of the currently displayed movie
                    appViewModel.getCurrentMovie(movies[index].id,movies[index].title, movies[index].overview, movies[index].poster_path)
                }
            }
        }
    }
}

@Composable
fun SwipableCard(
    navController: NavController,
    movieId: Int,
    title: String,
    subtitle: String,
    filmImage: String?,
    voteAverage: Double,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeUp: () -> Unit = {}
) {
    //States local to swipable card so don't need to be in appUiState
    //Offset to track swipe
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    //State to track whether the card is currently being swiped
    var isSwiping by remember { mutableStateOf(false) }
    //How far user must swipe to trigger either left, right or up swipe functions
    val swipeThreshold = 175f
    //States for handling smooth animation if swipe doesn't go past threshold
    //so card returns to initial position
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
    val animatedOffsetY by animateFloatAsState(targetValue = offsetY)
    //Using offsetX to slightly rotate the card on left/right swipe
    val rotationAngle = offsetX * 0.02f
    //Using offsetX to slowly fade out card as user swipes further from
    //initial position
    val cardAlpha = if(isSwiping) 1f - ((abs(offsetX) / (swipeThreshold*2))) else 1f

    //Sets border colour to indicate type of swipe
    val borderColor = when {
        offsetY < -swipeThreshold / 2 -> Color.Blue.copy(alpha = cardAlpha) //Always blue if card past Y threshold / 2
        offsetX > swipeThreshold / 2 -> Color.Green.copy(alpha = cardAlpha)  //Right
        offsetX < -swipeThreshold / 2 -> Color.Red.copy(alpha = cardAlpha)   //Left
        else -> MaterialTheme.colorScheme.surface                            //Neutral
    }

    //Changes the colour of the rating depending on how high/low
    val ratingBackgroundColor = when {
        voteAverage >= 8.0 -> Color(0xFF4CC452) //Green for high ratings
        voteAverage >= 5.0 -> Color(0xFFDCA60D) //Yellow for medium ratings
        else -> Color(0xFFF44336)              //Red for low ratings
    }

    //Opening movie detail in browser
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, top = 40.dp, bottom = 40.dp)
            .fillMaxSize(0.9f)
            .offset(x = animatedOffsetX.dp, y= animatedOffsetY.dp)
            .rotate(rotationAngle)  //Rotates card based on rotation angle
            .border(BorderStroke(4.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable {
                //On click will navigate to show movies full details
                navController.navigate("moviedetailsscreen")
            }
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
                                //State to track if user is swiping
                                isSwiping = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x //Tracking amount of drag on X

                                //Allows down swipe but not beyond initial offset.y
                                //so card cannot be down swiped
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
                                //Absolute value here as looking at both left/right
                                else if(abs(offsetX) > swipeThreshold) {
                                    //If negative then must be left swipe else right
                                    if (offsetX < 0) onSwipeLeft() else onSwipeRight()
                                }
                                //Reset card position and swiping state
                                offsetX = 0f
                                offsetY = 0f
                                isSwiping = false
                            },
                            onDragCancel = {
                                //Resets position of card
                                offsetX = 0f
                                //Reset swiping state
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

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(13.dp)
                        .clickable{
                            //TODO: Cite source from bibliography about links in browser
                            val tmdbUrl = "https://www.themoviedb.org/movie/${movieId}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tmdbUrl))
                            context.startActivity(intent)
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(ratingBackgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%.1f★", voteAverage),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                            )
                        }
                    }
                }


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
