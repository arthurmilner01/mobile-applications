package com.example.filmswipe.data
//TODO: Comment
data class MovieDetailsResponse(
    val genres: List<Genre>,
    val imdb_id: String?,
    val vote_average: Double
)

data class Genre(
    val id: Int,
    val name: String
)
