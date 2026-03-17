package com.phanuwat.movie_booking.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Seat;
import com.phanuwat.movie_booking.model.Showtimes;
import com.phanuwat.movie_booking.util.SeatPricing;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class CustomerInfoController {
    @FXML
    private ImageView posterImage;

    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label seatsLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label formHintLabel;

    private Movie movie;
    private Showtimes showtime;
    private List<Showtimes> showtimes;
    private List<Seat> selectedSeats;
    private String customerName = "";
    private String customerPhone = "";

    public void setCustomerData(Movie movie, Showtimes showtime, List<Showtimes> showtimes, List<Seat> seats) {
        setCustomerData(movie, showtime, showtimes, seats, "", "");
    }

    public void setCustomerData(
            Movie movie,
            Showtimes showtime,
            List<Showtimes> showtimes,
            List<Seat> seats,
            String name,
            String phone
    ) {
        this.movie = movie;
        this.showtime = showtime;
        this.showtimes = showtimes;
        this.selectedSeats = seats;
        this.customerName = name == null ? "" : name;
        this.customerPhone = phone == null ? "" : phone;

        posterImage.setImage(movie.getPoster());
        movieTitleLabel.setText(movie.getTitle());
        timeLabel.setText("รอบฉาย: " + showtime.getDisplayTime());
        seatsLabel.setText("ที่นั่ง: " + seats.stream()
                .map(seat -> seat.getRow() + seat.getColumn())
                .collect(Collectors.joining(", ")));
        int totalPrice = seats.stream()
                .mapToInt(SeatPricing::getSeatPrice)
                .sum();
        priceLabel.setText("ราคา: " + totalPrice + " บาท");
        formHintLabel.setText("");
        nameField.setText(customerName);
        phoneField.setText(customerPhone);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/seat-selection.fxml")
            );
            Parent root = loader.load();
            SeatSelectionController controller = loader.getController();
            controller.setSelectionData(movie, showtime, showtimes, selectedSeats);
            posterImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNext() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            formHintLabel.setText("โปรดกรอกชื่อและเบอร์โทรก่อนดำเนินการต่อ");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/confirmation.fxml")
            );
            Parent root = loader.load();
            ConfirmationController controller = loader.getController();
            controller.setConfirmationData(movie, showtime, showtimes, selectedSeats, name, phone);
            posterImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
