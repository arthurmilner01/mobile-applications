package com.example.filmswipe.network
import com.example.filmswipe.data.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBAPIService {
    @GET("discover/movie")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") pageNumber: Int,
        @Query("language") language: String = "en-US"
    ): Response<MovieResponse>
}
