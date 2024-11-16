package com.example.filmswipe.network
import com.example.filmswipe.data.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBAPIService {
    @GET("discover/movie")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") pageNumber: Int,
        @Query("with_watch_providers") watchProviders: String,
        @Query("watch_region") watchRegion: String = "GB",
        @Query("language") language: String = "en-US"
    ): Response<MovieResponse>

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<CastResponse>

    @GET("search/movie")
    suspend fun searchMoviesByTitle(
        @Query("api_key") apiKey: String,
        @Query("query") title: String,
        @Query("page") pageNumber: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<MovieResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<MovieDetailsResponse>

}
