package com.phanuwat.movie_booking.store;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.scene.image.Image;

public class MovieStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path STORE_PATH = Paths.get("data", "movies.json");
    private static final Path LEGACY_STORE_PATH = Paths.get(
            System.getProperty("user.home"),
            ".movie-booking",
            "movies.json"
    );

    private static final List<MovieRecord> MOVIES = new ArrayList<>();
    private static boolean loaded = false;

    private MovieStore() {
    }

    public static synchronized List<MovieRecord> getMovies() {
        ensureLoaded();
        return new ArrayList<>(MOVIES);
    }

    public static synchronized void addMovie(MovieRecord record) {
        ensureLoaded();
        MOVIES.add(record);
        save();
    }

    public static synchronized void updateMovie(String oldTitle, MovieRecord updated) {
        ensureLoaded();
        if (oldTitle == null || updated == null) {
            return;
        }
        MOVIES.removeIf(record -> oldTitle.equals(record.getTitle()));
        MOVIES.add(updated);
        save();
    }

    public static synchronized void deleteMovie(String title) {
        ensureLoaded();
        if (title == null) {
            return;
        }
        MOVIES.removeIf(record -> title.equals(record.getTitle()));
        save();
    }

    public static Image imageFromBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(data));
    }

    public static String encodeToBase64(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(data);
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        migrateLegacyStoreIfNeeded();
        if (!Files.exists(STORE_PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(STORE_PATH)) {
            StoreData data = GSON.fromJson(reader, StoreData.class);
            if (data != null && data.movies != null) {
                MOVIES.addAll(data.movies);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            StoreData data = new StoreData();
            data.movies = MOVIES;
            try (Writer writer = Files.newBufferedWriter(STORE_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateLegacyStoreIfNeeded() {
        try {
            if (Files.exists(STORE_PATH)) {
                return;
            }
            if (!Files.exists(LEGACY_STORE_PATH)) {
                return;
            }
            Files.createDirectories(STORE_PATH.getParent());
            Files.copy(LEGACY_STORE_PATH, STORE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class MovieRecord {
        private String title;
        private int duration;
        private String genre;
        private String director;
        private String description;
        private List<String> showtimes = new ArrayList<>();
        private String posterBase64;
        private String posterName;

        public MovieRecord(
                String title,
                int duration,
                String genre,
                String director,
                String description,
                List<String> showtimes,
                String posterBase64,
                String posterName
        ) {
            this.title = title;
            this.duration = duration;
            this.genre = genre;
            this.director = director;
            this.description = description;
            if (showtimes != null) {
                this.showtimes.addAll(showtimes);
            }
            this.posterBase64 = posterBase64;
            this.posterName = posterName;
        }

        public String getTitle() { return title; }
        public int getDuration() { return duration; }
        public String getGenre() { return genre; }
        public String getDirector() { return director; }
        public String getDescription() { return description; }
        public List<String> getShowtimes() { return showtimes; }
        public String getPosterBase64() { return posterBase64; }
        public String getPosterName() { return posterName; }
    }

    private static class StoreData {
        List<MovieRecord> movies = new ArrayList<>();
    }
}