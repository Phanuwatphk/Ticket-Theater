package com.phanuwat.movie_booking.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.phanuwat.movie_booking.data.MockData;
import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Showtimes;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class HomeController {

    @FXML
    private FlowPane movieContainer;

    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() {

        Map<String, List<Showtimes>> showtimes = MockData.getShowtimesMap();
        List<String> movies = new ArrayList<>(showtimes.keySet());


        for (String movieTitle : movies) {

            VBox movieCard = new VBox();
            movieCard.setSpacing(10);
            movieCard.setAlignment(Pos.CENTER);
            movieCard.getStyleClass().add("movie-card");

            List<Showtimes> movieShowtimes = showtimes.get(movieTitle);
            Movie movie = movieShowtimes.get(0).getMovie();

            Label title = new Label(movieTitle);
            title.getStyleClass().add("card-title");
            Label director = new Label("Director: " + movie.getDirector());
            director.getStyleClass().add("card-meta");
            Label duration = new Label("Duration: " + movie.getDuration() + " min");
            duration.getStyleClass().add("card-meta");

            ImageView poster = new ImageView(movie.getPoster());
            if (poster != null) {
                poster.setFitWidth(200);
                poster.setFitHeight(280);
                poster.setPreserveRatio(true);
                poster.setSmooth(true);
                movieCard.getChildren().add(poster);
            }

            movieCard.getChildren().addAll(title, director, duration);
            movieCard.setOnMouseClicked(event -> openMovieDetail(movie, movieShowtimes));

            movieContainer.getChildren().add(movieCard);
        }
    }

    private void openMovieDetail(Movie movie, List<Showtimes> showtimes) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/movie-detail.fxml")
            );
            Parent root = loader.load();
            MovieDetailController controller = loader.getController();
            controller.setMovieData(movie, showtimes);
            movieContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/phanuwat/movie_booking/view/staff-login.fxml")
            );
            Parent root = loader.load();
            if (rootPane != null && rootPane.getScene() != null) {
                rootPane.getScene().setRoot(root);
                return;
            }
            if (movieContainer != null && movieContainer.getScene() != null) {
                movieContainer.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("เปิดหน้าไม่สำเร็จ");
            alert.setHeaderText("ไปหน้าพนักงานไม่สำเร็จ");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
