package com.phanuwat.movie_booking.service;

import java.util.List;
import java.util.Map;

import com.phanuwat.movie_booking.data.MockData;
import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Showtimes;
import com.phanuwat.movie_booking.store.MovieStore;

import javafx.scene.image.Image;

public final class MovieService {
    private MovieService() {
    }

    public static Map<String, List<Showtimes>> getShowtimesMap() {
        return MockData.getShowtimesMap();
    }

    public static Image loadPoster(String fileName) {
        return MockData.loadPoster(fileName);
    }

    public static List<MovieStore.MovieRecord> getMovies() {
        return MovieStore.getMovies();
    }

    public static void addMovie(
            Movie movie,
            List<String> showtimes,
            String posterBase64,
            String posterName
    ) {
        MockData.addMovie(movie, showtimes, posterBase64, posterName);
    }

    public static void updateMovie(
            String oldTitle,
            Movie movie,
            List<String> showtimes,
            String posterBase64,
            String posterName
    ) {
        MockData.updateMovie(oldTitle, movie, showtimes, posterBase64, posterName);
    }

    public static void removeMovie(String title) {
        MockData.removeMovie(title);
    }
}
