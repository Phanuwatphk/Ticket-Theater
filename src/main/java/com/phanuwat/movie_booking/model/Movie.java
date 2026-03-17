package com.phanuwat.movie_booking.model;

import javafx.scene.image.Image;

public class Movie {
    private Image poster;
    private String title;
    private int duration; //minutes
    private String director;
    private String description;

    public Movie(String title, int duration, String director, String description, Image poster) {
        this.title = title;
        this.duration = duration;
        this.director = director;
        this.description = description == null ? "-" : description;
        this.poster = poster;
    }
    
    public String getTitle() { return title; }
    public int getDuration() { return duration; }
    public String getDirector() { return director; }
    public String getDescription() { return description; }
    public Image getPoster() { return poster; }
}