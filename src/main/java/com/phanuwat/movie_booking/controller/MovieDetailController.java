package com.phanuwat.movie_booking.controller;

import java.io.IOException;
import java.util.List;

import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Showtimes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

public class MovieDetailController {
    private static final String TIME_BUTTON_STYLE =
            "-fx-background-color:#1e5aa8; -fx-text-fill:white; -fx-background-radius:16; -fx-font-size:14px; -fx-padding:6 14;";

    @FXML
    private Label titleLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private Label directorLabel;

    @FXML
    private ImageView posterImage;

    @FXML
    private FlowPane showtimeContainer;

    @FXML
    private Label descriptionLabel;

    private Movie movie;
    private List<Showtimes> showtimes;

    public void setMovieData(Movie movie, List<Showtimes> showtimes) {
        this.movie = movie;
        this.showtimes = showtimes;
        titleLabel.setText(movie.getTitle());
        durationLabel.setText("Duration: " + movie.getDuration() + " min");
        directorLabel.setText("Director: " + movie.getDirector());
        descriptionLabel.setText(movie.getDescription());
        posterImage.setImage(movie.getPoster());

        showtimeContainer.getChildren().clear();
        for (Showtimes showtime : showtimes) {
            Button timeButton = new Button(showtime.getTime());
            timeButton.setStyle(TIME_BUTTON_STYLE);
            timeButton.setOnAction(event -> openSeatSelection(showtime));
            showtimeContainer.getChildren().add(timeButton);
        }
    }

    private void openSeatSelection(Showtimes showtime) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/seat-selection.fxml")
            );
            Parent root = loader.load();
            SeatSelectionController controller = loader.getController();
            controller.setSelectionData(movie, showtime, showtimes);
            showtimeContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/phanuwat/movie_booking/view/home.fxml")
            );
            titleLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
