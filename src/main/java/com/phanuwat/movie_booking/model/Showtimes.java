package com.phanuwat.movie_booking.model;

public class Showtimes {
    private Movie movie;
    private String time;
    private String theater;

    public Showtimes(Movie movie, String time) {
        this.movie = movie;
        if (time != null && time.contains("@")) {
            String[] parts = time.split("@", 2);
            this.time = parts[0].trim();
            this.theater = parts[1].trim();
        } else {
            this.time = time;
            this.theater = "1";
        }
    }

    public Showtimes(Movie movie, String time, String theater) {
        this.movie = movie;
        this.time = time;
        this.theater = theater;
    }
    public Movie getMovie() {
        return movie;
    }
    public String getTime() {
        return time;
    }

    public String getTheater() {
        return theater == null || theater.isBlank() ? "1" : theater.trim();
    }

    public String getDisplayTime() {
        return time + " (โรง " + getTheater() + ")";
    }

    public String getKey() {
        return time + "@" + getTheater();
    }
}
