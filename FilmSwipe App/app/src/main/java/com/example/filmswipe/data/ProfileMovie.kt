package com.example.filmswipe.data
//Data class for movies grabbed from firebase
data class ProfileMovie(
    val id: String = "",
    val title: String = "",
    val overview: String = "",
    val poster_path: String? = null
)
