package com.phanuwat.movie_booking.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.phanuwat.movie_booking.store.BookingStore;
import com.phanuwat.movie_booking.service.BookingService;
import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Seat;
import com.phanuwat.movie_booking.model.Showtimes;
import com.phanuwat.movie_booking.util.SeatPricing;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ConfirmationController {
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
    private Label durationLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Movie movie;
    private Showtimes showtime;
    private List<Showtimes> showtimes;
    private List<Seat> selectedSeats;
    private String customerName;
    private String customerPhone;

    public void setConfirmationData(
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
        this.customerName = name;
        this.customerPhone = phone;

        posterImage.setImage(movie.getPoster());
        movieTitleLabel.setText(movie.getTitle());
        timeLabel.setText("รอบฉาย: " + showtime.getDisplayTime());
        durationLabel.setText("ความยาว: " + movie.getDuration() + " นาที");

        String seatsText = seats.stream()
                .map(seat -> seat.getRow() + seat.getColumn())
                .collect(Collectors.joining(", "));
        seatsLabel.setText("ที่นั่ง: " + seatsText);

        int totalPrice = seats.stream()
                .mapToInt(SeatPricing::getSeatPrice)
                .sum();
        priceLabel.setText("ราคา: " + totalPrice + " บาท");
        nameLabel.setText("ชื่อคนจอง: " + safeValue(customerName));
        phoneLabel.setText("เบอร์คนจอง: " + safeValue(customerPhone));
    }

    

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/customer-info.fxml")
            );
            Parent root = loader.load();
            CustomerInfoController controller = loader.getController();
            controller.setCustomerData(movie, showtime, showtimes, selectedSeats, customerName, customerPhone);
            posterImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfirm() {
        String ticketId = BookingService.allocateTicketRange(selectedSeats.size());
        String seatsText = selectedSeats.stream()
                .map(seat -> seat.getRow() + seat.getColumn())
                .collect(Collectors.joining(", "));
        int totalPrice = selectedSeats.stream()
                .mapToInt(SeatPricing::getSeatPrice)
                .sum();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        BookingStore.BookingRecord record = new BookingStore.BookingRecord(
                ticketId,
                movie.getTitle(),
                showtime.getTime(),
                seatsText,
                customerName,
                customerPhone,
                totalPrice,
                timestamp
        );
        BookingService.addBookingWithRecord(movie.getTitle(), showtime.getTime(), selectedSeats, record);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/ticket.fxml")
            );
            Parent root = loader.load();
            TicketController controller = loader.getController();
            controller.setTicketData(movie, showtime, selectedSeats, customerName, customerPhone, ticketId);
            posterImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim();
    }
}
