package com.phanuwat.movie_booking.data;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Showtimes;
import com.phanuwat.movie_booking.store.MovieStore;
import com.phanuwat.movie_booking.util.ShowtimeUtils;

import javafx.scene.image.Image;

public class MockData {
    private static final String IMAGE_PATH = "/com/phanuwat/movie_booking/images/";
    private static final Map<String, List<Showtimes>> SHOWTIMES_MAP = new LinkedHashMap<>();
    private static boolean initialized = false;
    private static final List<MovieStore.MovieRecord> DEFAULT_MOVIES = Arrays.asList(
            new MovieStore.MovieRecord(
                    "Interstellar",
                    169,
                    "Christopher Nolan","-",
                    Arrays.asList("10:00@1", "15:00@2", "18:00@1", "20:00@3"),
                    null,
                    "Interstellar.jpg"
            ),
            new MovieStore.MovieRecord(
                    "Inception",
                    148,
                    "Christopher Nolan","-",
                    Arrays.asList("11:00@2", "14:00@1", "17:00@3", "19:00@2"),
                    null,
                    "Inception.jpg"
            ),
            new MovieStore.MovieRecord(
                    "Avatar 1",
                    162,
                    "James Cameron","-",
                    Arrays.asList("11:00@3", "15:00@1", "19:00@2", "21:00@3"),
                    null,
                    "Avatar.jpg"
            ),
            new MovieStore.MovieRecord(
                    "The Matrix",
                    136,
                    "The Wachowskis","-",
                    Arrays.asList("09:30@1", "14:00@2", "18:00@1", "21:00@3"),
                    null,
                    "The_Matrix.png"
            ),
            new MovieStore.MovieRecord(
                    "Terminator",
                    107,
                    "James Cameron","-",
                    Arrays.asList("09:30@2", "14:00@3", "18:00@2", "21:00@1"),
                    null,
                    "Terminator.jpg"
            )
    );

    public static Map<String, List<Showtimes>> getShowtimesMap() {
        initIfNeeded();
        return SHOWTIMES_MAP;
    }

    public static synchronized void addMovie(Movie movie, List<String> showtimes) {
        initIfNeeded();
        List<Showtimes> list = showtimes.stream()
                .map(String::trim)
                .filter(time -> !time.isEmpty())
                .map(time -> new Showtimes(movie, time))
                .collect(Collectors.toList());
        SHOWTIMES_MAP.put(movie.getTitle(), list);
    }

    public static synchronized void addMovie(
            Movie movie,
            List<String> showtimes,
            String posterBase64,
            String posterName
    ) {
        addMovie(movie, showtimes);
        MovieStore.MovieRecord record = new MovieStore.MovieRecord(
                movie.getTitle(),
                movie.getDuration(),
                movie.getDirector(),
                movie.getDescription(),
                showtimes,
                posterBase64,
                posterName
        );
        MovieStore.addMovie(record);
    }

    public static synchronized void updateMovie(
            String oldTitle,
            Movie movie,
            List<String> showtimes,
            String posterBase64,
            String posterName
    ) {
        initIfNeeded();
        List<Showtimes> list = showtimes.stream()
                .map(String::trim)
                .filter(time -> !time.isEmpty())
                .map(time -> new Showtimes(movie, time))
                .collect(Collectors.toList());
        if (oldTitle != null && !oldTitle.equals(movie.getTitle())) {
            SHOWTIMES_MAP.remove(oldTitle);
        }
        SHOWTIMES_MAP.put(movie.getTitle(), list);
        MovieStore.MovieRecord record = new MovieStore.MovieRecord(
                movie.getTitle(),
                movie.getDuration(),
                movie.getDirector(),
                movie.getDescription(),
                showtimes,
                posterBase64,
                posterName
        );
        MovieStore.updateMovie(oldTitle, record);
    }

    public static synchronized void removeMovie(String title) {
        initIfNeeded();
        if (title == null) {
            return;
        }
        SHOWTIMES_MAP.remove(title);
        MovieStore.deleteMovie(title);
    }

    public static Image loadPoster(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return getDefaultPoster();
        }
        InputStream stream = MockData.class.getResourceAsStream(IMAGE_PATH + fileName.trim());
        if (stream == null) {
            return getDefaultPoster();
        }
        return new Image(stream);
    }

    private static void initIfNeeded() {
        if (initialized) {
            return;
        }
        initialized = true;
        ensureDefaultsPresent();
        loadFromStore();
    }

    private static Image getDefaultPoster() {
        InputStream stream = MockData.class.getResourceAsStream(IMAGE_PATH + "Interstellar.jpg");
        return new Image(stream);
    }

    private static void ensureDefaultsPresent() {
        List<MovieStore.MovieRecord> storedMovies = MovieStore.getMovies();
        java.util.Set<String> existingTitles = storedMovies.stream()
                .map(MovieStore.MovieRecord::getTitle)
                .filter(title -> title != null && !title.isBlank())
                .collect(Collectors.toSet());
        for (MovieStore.MovieRecord record : DEFAULT_MOVIES) {
            if (!existingTitles.contains(record.getTitle())) {
                MovieStore.addMovie(record);
            }
        }
    }

    private static void loadFromStore() {
        SHOWTIMES_MAP.clear();
        List<MovieStore.MovieRecord> storedMovies = MovieStore.getMovies();
        for (MovieStore.MovieRecord record : storedMovies) {
            Image poster = MovieStore.imageFromBase64(record.getPosterBase64());
            if (poster == null) {
                String posterName = record.getPosterName();
                if (posterName != null && !posterName.isBlank()) {
                    poster = loadPoster(posterName);
                } else {
                    poster = getDefaultPoster();
                }
            }
            Movie movie = new Movie(
                    record.getTitle(),
                    record.getDuration(),
                    record.getDirector(),
                    record.getDescription(),
                    poster
            );
            List<String> times = record.getShowtimes();
            if (times == null) {
                continue;
            }
            List<Showtimes> list = times.stream()
                    .map(ShowtimeUtils::normalizeToken)
                    .filter(token -> token != null && !token.isBlank())
                    .map(token -> {
                        String[] parts = token.split("@", 2);
                        String time = parts[0].trim();
                        String theater = parts.length > 1 ? parts[1].trim() : "1";
                        return new Showtimes(movie, time, theater);
                    })
                    .collect(Collectors.toList());
            SHOWTIMES_MAP.put(movie.getTitle(), list);
        }
    }
}
