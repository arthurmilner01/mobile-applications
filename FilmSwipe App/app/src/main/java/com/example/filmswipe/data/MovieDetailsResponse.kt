package com.example.filmswipe.data

data class MovieDetailsResponse(
    val genres: List<Genre>, //List of genres for the given movie
    val imdb_id: String?, //Movie ID
    val vote_average: Double //Average user rating for the given movie
)

data class Genre(
    val id: Int, //Genre ID
    val name: String //Genre name
)
