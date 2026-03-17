package com.phanuwat.movie_booking.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.phanuwat.movie_booking.data.MockData;
import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Showtimes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML
    private FlowPane movieContainer;

    @FXML
    private BorderPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    public void initialize() {
        Map<String, List<Showtimes>> showtimes = MockData.getShowtimesMap();
        Set<String> genres = new HashSet<>();
        genres.add("ทั้งหมด"); 
        
        for (List<Showtimes> list : showtimes.values()) {
            if (list != null && !list.isEmpty()) {
                String genre = list.get(0).getMovie().getGenre();
                if (genre != null && !genre.isBlank()) {
                    genres.add(genre);
                }
            }
        }
    
        if (genreComboBox != null) {
            genreComboBox.getItems().addAll(genres);
            genreComboBox.getSelectionModel().select("ทั้งหมด");
            
            genreComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                executeFilter();
            });
        }

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                executeFilter();
            });
        }

        executeFilter();
    }

    private void executeFilter() {
        String keyword = searchField != null && searchField.getText() != null 
                ? searchField.getText().trim().toLowerCase() : "";
        String selectedGenre = genreComboBox != null && genreComboBox.getValue() != null 
                ? genreComboBox.getValue() : "ทั้งหมด";
        
        loadMovies(keyword, selectedGenre);
    }

    private void loadMovies(String keyword, String selectedGenre) {
        movieContainer.getChildren().clear();

        Map<String, List<Showtimes>> showtimes = MockData.getShowtimesMap();
        List<String> movies = new ArrayList<>(showtimes.keySet());

        for (String movieTitle : movies) {
            List<Showtimes> movieShowtimes = showtimes.get(movieTitle);
            if (movieShowtimes == null || movieShowtimes.isEmpty()) continue;
            
            Movie movie = movieShowtimes.get(0).getMovie();

            if (!keyword.isEmpty() && !movie.getTitle().toLowerCase().contains(keyword)) {
                continue;

            if (!selectedGenre.equals("ทั้งหมด") && !movie.getGenre().equals(selectedGenre)) {
                continue; 
            }

            VBox movieCard = new VBox();
            movieCard.setSpacing(10);
            movieCard.setAlignment(Pos.CENTER);
            movieCard.getStyleClass().add("movie-card");

            Label title = new Label(movie.getTitle());
            title.getStyleClass().add("card-title");
            
            Label metaLabel = new Label(movie.getGenre() + " | " + movie.getDuration() + " min");
            metaLabel.getStyleClass().add("card-meta");

            ImageView poster = new ImageView(movie.getPoster());
            if (poster != null) {
                poster.setFitWidth(200);
                poster.setFitHeight(280);
                poster.setPreserveRatio(true);
                poster.setSmooth(true);
                movieCard.getChildren().add(poster);
            }

            movieCard.getChildren().addAll(title, metaLabel);
            
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