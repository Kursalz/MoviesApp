package com.d100.moviesappprova.api;

import com.d100.moviesappprova.model.Movie;
import com.d100.moviesappprova.model.MoviesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Service {
    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("search/movie")
    Call<MoviesResponse> getMoviesByTitle(@Query("api_key") String apiKey, @Query("query") String title, @Query("page") int page);

    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetail(@Path("movie_id") int id, @Query("api_key") String apiKey);
}
