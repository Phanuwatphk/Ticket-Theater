package com.phanuwat.movie_booking.model;

public class Seat {
    private String row;
    private int column;
    private boolean booked;

    public Seat(String row, int column) {
        this.row = row;
        this.column = column;
        this.booked = false;
    }

    public String getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}
