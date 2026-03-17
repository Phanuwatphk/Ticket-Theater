package com.phanuwat.movie_booking.controller.staff;

public class DashboardRow {
    private final String movieTitle;
    private final String showtime;
    private int seatCount;
    private int revenue;
    private String displayTitle;
    private String displayShowtime;
    private boolean summary;

    public DashboardRow(String movieTitle, String showtime) {
        this.movieTitle = movieTitle;
        this.showtime = showtime == null ? "" : showtime;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getShowtime() {
        return showtime;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getDisplayShowtime() {
        return displayShowtime;
    }

    public boolean isSummary() {
        return summary;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public int getRevenue() {
        return revenue;
    }

    public void addSeats(int seats) {
        seatCount += Math.max(seats, 0);
    }

    public void addRevenue(int amount) {
        revenue += Math.max(amount, 0);
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public void setDisplayShowtime(String displayShowtime) {
        this.displayShowtime = displayShowtime;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public static DashboardRow createSummary(String movieTitle, int seats, int revenue) {
        DashboardRow row = new DashboardRow(movieTitle, "");
        row.seatCount = Math.max(seats, 0);
        row.revenue = Math.max(revenue, 0);
        row.setSummary(true);
        return row;
    }
}
