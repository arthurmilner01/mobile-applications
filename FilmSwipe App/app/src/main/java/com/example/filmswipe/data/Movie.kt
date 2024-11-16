package com.example.filmswipe.data
//Data class for movies returned by API
data class Movie(
    val id: Int, //Movie ID
    val title: String, //Movie title
    val overview: String, //Movie overview
    val poster_path: String?, //Movie poster path
    val vote_average: Double //Movie review score
)
