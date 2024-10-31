package com.example.filmswipe.model

import androidx.annotation.DrawableRes

data class Film(
    val title: String,
    val subtitle: String,
    @DrawableRes val image: Int
)