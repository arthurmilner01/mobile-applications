package com.example.filmswipe.data
//Data class for movies grabbed from firebase
//to display on profile
data class ProfileMovie(
    val id: String = "", //Movie ID
    val title: String = "", //Movie title
    val overview: String = "", //Movie overview
    val poster_path: String? = null //Movie poster path
)
