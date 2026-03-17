package com.phanuwat.movie_booking.model;

import javafx.scene.image.Image;

public class Movie {
    private Image poster;
    private String title;
    private int duration; // minutes
    private String genre; // ประเภทหนัง
    private String director;
    private String description; // เรื่องย่อ

    public Movie(String title, int duration, String genre, String director, String description, Image poster) {
        this.title = title;
        this.duration = duration;
        this.genre = genre == null || genre.isEmpty() ? "-" : genre;
        this.director = director;
        this.description = description == null ? "-" : description;
        this.poster = poster;
    }
    
    public String getTitle() { return title; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public String getDirector() { return director; }
    public String getDescription() { return description; }
    public Image getPoster() { return poster; }
}