package com.example.filmswipe.data

data class CastResponse(
    val id: Int,  // The ID of the movie
    val cast: List<CastMember>,
    val crew: List<CrewMember>
)
