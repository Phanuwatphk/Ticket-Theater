package com.phanuwat.movie_booking.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.phanuwat.movie_booking.service.BookingService;
import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Seat;
import com.phanuwat.movie_booking.model.Showtimes;
import com.phanuwat.movie_booking.util.SeatPricing;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class SeatSelectionController {
    private static final int ROWS = 10;
    private static final int COLUMNS = 20;
    private static final int SEAT_SIZE = 26;
    private static final String SEAT_AVAILABLE_STYLE =
            "-fx-background-color:#e0e0e0; -fx-text-fill:#222; -fx-background-radius:6; -fx-font-size:9px; -fx-font-weight:bold; -fx-padding:0;";
    private static final String SEAT_SELECTED_STYLE =
            "-fx-background-color:#2e7d32; -fx-text-fill:white; -fx-background-radius:6; -fx-font-size:9px; -fx-font-weight:bold; -fx-padding:0;";
    private static final String SEAT_BOOKED_STYLE =
            "-fx-background-color:#9e9e9e; -fx-text-fill:white; -fx-background-radius:6; -fx-font-size:9px; -fx-font-weight:bold; -fx-padding:0;";

    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label selectedSeatsLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label formHintLabel;

    @FXML
    private Label theaterLabel;

    @FXML
    private ImageView posterImage;

    @FXML
    private GridPane seatGrid;

    private Movie movie;
    private Showtimes showtime;
    private List<Showtimes> showtimes;
    private List<Seat> seats = new ArrayList<>();
    private Set<String> preselectedSeatCodes = new HashSet<>();
    private Set<String> bookedSeatCodes = new HashSet<>();

    public void setSelectionData(Movie movie, Showtimes showtime, List<Showtimes> showtimes) {
        setSelectionData(movie, showtime, showtimes, new ArrayList<>());
    }

    public void setSelectionData(
            Movie movie,
            Showtimes showtime,
            List<Showtimes> showtimes,
            List<Seat> preselectedSeats
    ) {
        this.movie = movie;
        this.showtime = showtime;
        this.showtimes = showtimes;
        this.preselectedSeatCodes = preselectedSeats.stream()
                .map(seat -> seat.getRow() + seat.getColumn())
                .collect(Collectors.toSet());
        this.bookedSeatCodes = BookingService.getBookedSeats(movie.getTitle(), showtime.getTime());
        movieTitleLabel.setText(movie.getTitle());
        timeLabel.setText("เวลา: " + showtime.getTime());
        if (theaterLabel != null) {
            theaterLabel.setText("โรง: " + showtime.getTheater());
        }
        posterImage.setImage(movie.getPoster());
        formHintLabel.setText("");
        buildSeatGrid();
        updateSelectedSeatsLabel();
    }

    private void buildSeatGrid() {
        seats.clear();
        seatGrid.getChildren().clear();

        for (int col = 1; col <= COLUMNS; col++) {
            Label colLabel = new Label(String.valueOf(col));
            colLabel.setMinWidth(SEAT_SIZE);
            colLabel.setPrefWidth(SEAT_SIZE);
            colLabel.setStyle("-fx-font-size:9px; -fx-text-fill:#333;");
            colLabel.setAlignment(javafx.geometry.Pos.CENTER);
            seatGrid.add(colLabel, col, 0);
        }

        int gridRow = 1;
        for (int row = 0; row < ROWS; row++) {
            if (row == 3 || row == 7) {
                addAisleRow(gridRow);
                gridRow++;
            }
            char rowChar = (char) ('A' + row);
            Label rowLabel = new Label(String.valueOf(rowChar));
            rowLabel.setMinWidth(SEAT_SIZE);
            rowLabel.setPrefWidth(SEAT_SIZE);
            rowLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#333;");
            rowLabel.setAlignment(javafx.geometry.Pos.CENTER);
            seatGrid.add(rowLabel, 0, gridRow);

            for (int col = 1; col <= COLUMNS; col++) {
                Seat seat = new Seat(String.valueOf(rowChar), col);
                String seatCode = rowChar + String.valueOf(col);
                boolean isBooked = bookedSeatCodes.contains(seatCode);
                if (preselectedSeatCodes.contains(seatCode)) {
                    seat.setBooked(true);
                }
                seats.add(seat);

                Button seatButton = new Button(seatCode);
                seatButton.setMinSize(SEAT_SIZE, SEAT_SIZE);
                seatButton.setPrefSize(SEAT_SIZE, SEAT_SIZE);
                seatButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                seatButton.setTextOverrun(OverrunStyle.CLIP);
                if (isBooked) {
                    seatButton.setStyle(SEAT_BOOKED_STYLE);
                    seatButton.setDisable(true);
                } else {
                    seatButton.setStyle(seat.isBooked() ? SEAT_SELECTED_STYLE : SEAT_AVAILABLE_STYLE);
                    seatButton.setOnAction(event -> toggleSeat(seat, seatButton));
                }
                seatGrid.add(seatButton, col, gridRow);
            }
            gridRow++;
        }
    }

    private void addAisleRow(int gridRow) {
        Region spacer = new Region();
        spacer.setMinHeight(14);
        spacer.setPrefHeight(14);
        seatGrid.add(spacer, 0, gridRow, COLUMNS + 1, 1);
    }

    private void toggleSeat(Seat seat, Button seatButton) {
        seat.setBooked(!seat.isBooked());
        seatButton.setStyle(seat.isBooked() ? SEAT_SELECTED_STYLE : SEAT_AVAILABLE_STYLE);
        updateSelectedSeatsLabel();
    }

    private void updateSelectedSeatsLabel() {
        List<Seat> selected = seats.stream()
                .filter(Seat::isBooked)
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            selectedSeatsLabel.setText("ยังไม่ได้เลือกที่นั่ง");
            totalPriceLabel.setText("ราคารวม: 0 บาท");
            return;
        }
        String seatsText = formatSeatList(selected);
        int totalPrice = selected.stream()
                .mapToInt(SeatPricing::getSeatPrice)
                .sum();
        selectedSeatsLabel.setText("ที่นั่งที่เลือก: " + seatsText + " | " + selected.size() + " ที่นั่ง");
        totalPriceLabel.setText("ราคารวม: " + totalPrice + " บาท");
    }

    private String formatSeatList(List<Seat> selected) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (Seat seat : selected) {
            if (count > 0) {
                if (count % 6 == 0) {
                    builder.append("\n");
                } else {
                    builder.append(", ");
                }
            }
            builder.append(seat.getRow()).append(seat.getColumn());
            count++;
        }
        return builder.toString();
    }

    

    private List<Seat> getSelectedSeats() {
        return seats.stream()
                .filter(Seat::isBooked)
                .map(seat -> {
                    Seat copy = new Seat(seat.getRow(), seat.getColumn());
                    copy.setBooked(true);
                    return copy;
                })
                .collect(Collectors.toList());
    }

    @FXML
    private void handleConfirm() {
        List<Seat> selectedSeats = getSelectedSeats();
        if (selectedSeats.isEmpty()) {
            formHintLabel.setText("โปรดเลือกที่นั่งอย่างน้อย 1 ที่นั่ง");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/customer-info.fxml")
            );
            Parent root = loader.load();
            CustomerInfoController controller = loader.getController();
            controller.setCustomerData(movie, showtime, showtimes, selectedSeats);
            seatGrid.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/movie-detail.fxml")
            );
            Parent root = loader.load();
            MovieDetailController controller = loader.getController();
            controller.setMovieData(movie, showtimes);
            seatGrid.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
