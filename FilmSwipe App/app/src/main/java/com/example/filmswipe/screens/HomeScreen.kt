package com.example.filmswipe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.filmswipe.R
import com.example.filmswipe.model.AppViewModel
import com.example.filmswipe.model.Film
import kotlin.math.abs

@Composable
fun HomeScreen(navController: NavController, appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val films = remember {
        mutableStateListOf(
            Film("Inception", "A mind-bending thriller", R.drawable.defaultprofilepic),
            Film("The Matrix", "A sci-fi classic", R.drawable.defaultprofilepic),
            Film("Interstellar", "A journey through space", R.drawable.defaultprofilepic)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        for (index in films.indices.reversed()) {
            SwipableCard(
                title = films[index].title,
                subtitle = films[index].subtitle,
                filmImage = films[index].image,
                onSwipeLeft = { films.removeAt(index) },
                onSwipeRight = { films.removeAt(index) },
            )
        }
    }
}

@Composable
fun SwipableCard(
    title: String,
    subtitle: String,
    filmImage: Int,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    val swipeThreshold = 150f

    val rotationAngle = offsetX * 0.02f
    val cardAlpha = if(isSwiping) 1f - ((abs(offsetX) / (swipeThreshold*2))) else 1f
    val borderColor = if (isSwiping) MaterialTheme.colorScheme.tertiary.copy(alpha=cardAlpha) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .padding(24.dp)
            .rotate(rotationAngle)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .offset(x = offsetX.dp)
            .border(BorderStroke(4.dp, borderColor), RoundedCornerShape(8.dp))
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
            Image(
                painter = painterResource(filmImage),
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
