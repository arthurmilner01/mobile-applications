package com.example.filmswipe.data
//Data class for cast member details returned by api
data class CastMember(
    val cast_id:Int, //Cast member ID
    val name:String, //Cast member name
    val character:String, //Cast member character in movie
    val order:Int, //Order to display
    val profile_path: String? //Profile picture path
)
