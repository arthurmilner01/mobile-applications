package com.example.filmswipe.network
import com.example.filmswipe.data.CastResponse
import com.example.filmswipe.data.MovieDetailsResponse
import com.example.filmswipe.data.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBAPIService {
    //Used for fetching random movies
    @GET("discover/movie")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String, //API key for making calls
        @Query("page") pageNumber: Int, //Page number of results, is randomly decided before call is made
        @Query("with_watch_providers") watchProviders: String, //Filter for watch providers
        @Query("watch_region") watchRegion: String = "GB", //UK region
        @Query("language") language: String = "en-US" //Return english
    ): Response<MovieResponse>

    //Used for getting cast/crew
    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int, //Movie ID you want the cast for
        @Query("api_key") apiKey: String, //API key for making calls
        @Query("language") language: String = "en-US" //Return english
    ): Response<CastResponse>

    //Used for the movie search
    @GET("search/movie")
    suspend fun searchMoviesByTitle(
        @Query("api_key") apiKey: String, //API key for making calls
        @Query("query") title: String, //Movie title provided from user search query
        @Query("page") pageNumber: Int = 1, //Only return first page of results
        @Query("language") language: String = "en-US"
    ): Response<MovieResponse>

    //TODO: Comment
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<MovieDetailsResponse>

}
