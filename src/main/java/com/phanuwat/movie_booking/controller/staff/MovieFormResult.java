package com.phanuwat.movie_booking.controller.staff;

import java.util.List;

public class MovieFormResult {
    private final String title;
    private final int duration;
    private final String genre;
    private final String director;
    private final String description;
    private final List<String> showtimes;
    private final String posterBase64;
    private final String posterName;

    public MovieFormResult(
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
        this.showtimes = showtimes;
        this.posterBase64 = posterBase64;
        this.posterName = posterName;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public String getGenre() {
        return genre;
    }

    public String getDirector() {
        return director;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getShowtimes() {
        return showtimes;
    }

    public String getPosterBase64() {
        return posterBase64;
    }

    public String getPosterName() {
        return posterName;
    }
}
