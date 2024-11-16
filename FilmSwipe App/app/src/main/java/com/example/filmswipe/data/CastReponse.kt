package com.example.filmswipe.data
//Data class that packages the API response for cast and crew
data class CastResponse(
    val id: Int,  //Movie ID
    val cast: List<CastMember>, //List of cast
    val crew: List<CrewMember> //List of crew
)
