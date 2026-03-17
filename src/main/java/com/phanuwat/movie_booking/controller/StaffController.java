package com.phanuwat.movie_booking.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.phanuwat.movie_booking.store.BookingStore;
import com.phanuwat.movie_booking.service.BookingService;
import com.phanuwat.movie_booking.service.MovieService;
import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.store.MovieStore;
import com.phanuwat.movie_booking.model.Showtimes;
import com.phanuwat.movie_booking.controller.staff.BookingEditDialog;
import com.phanuwat.movie_booking.controller.staff.DashboardRow;
import com.phanuwat.movie_booking.controller.staff.MovieFormDialog;
import com.phanuwat.movie_booking.controller.staff.MovieFormResult;
import com.phanuwat.movie_booking.util.ShowtimeUtils;
import com.phanuwat.movie_booking.util.TextUtils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class StaffController {
    @FXML
    private TableView<BookingStore.BookingRecord> bookingTable;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> ticketColumn;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> movieColumn;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> timeColumn;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> seatsColumn;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> nameColumn;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> phoneColumn;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> priceColumn;

    @FXML
    private TableColumn<BookingStore.BookingRecord, String> timeStampColumn;

    @FXML
    private HBox bookingActionBox;

    @FXML
    private TableView<DashboardRow> dashboardTable;

    @FXML
    private TableColumn<DashboardRow, String> dashboardMovieColumn;

    @FXML
    private TableColumn<DashboardRow, String> dashboardShowtimeColumn;

    @FXML
    private TableColumn<DashboardRow, Integer> dashboardSeatColumn;

    @FXML
    private TableColumn<DashboardRow, Integer> dashboardRevenueColumn;

    @FXML
    private Button bookingEditButton;

    @FXML
    private Button bookingDeleteButton;

    @FXML
    private TableView<MovieStore.MovieRecord> movieTable;

    @FXML
    private TableColumn<MovieStore.MovieRecord, String> movieTitleColumn;
    
    @FXML
    private TableColumn<MovieStore.MovieRecord, String> movieGenreColumn;

    @FXML
    private TableColumn<MovieStore.MovieRecord, String> movieDurationColumn;

    @FXML
    private TableColumn<MovieStore.MovieRecord, String> movieDirectorColumn;

    @FXML
    private TableColumn<MovieStore.MovieRecord, String> movieShowtimesColumn;

    @FXML
    private TableColumn<MovieStore.MovieRecord, String> moviePosterColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private HBox movieActionBox;

    @FXML
    private Label statusLabel;

    private MovieStore.MovieRecord selectedMovie;
    private BookingStore.BookingRecord selectedBooking;

    @FXML
    public void initialize() {
        if (dashboardTable != null) {
            dashboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            dashboardTable.setRowFactory(table -> new javafx.scene.control.TableRow<DashboardRow>() {
                @Override
                protected void updateItem(DashboardRow row, boolean empty) {
                    super.updateItem(row, empty);
                    if (empty || row == null) {
                        setStyle("");
                        return;
                    }
                    if (row.isSummary()) {
                        setStyle("-fx-font-weight:bold; -fx-background-color:#eaf0f9; -fx-border-color:#c8d8f0; -fx-border-width:0 0 1 0;");
                    } else {
                        setStyle("");
                    }
                }
            });
        }
        if (dashboardMovieColumn != null) {
            dashboardMovieColumn.setCellValueFactory(cell ->
                    new SimpleStringProperty(safeTitle(cell.getValue().getDisplayTitle())));
        }
        if (dashboardShowtimeColumn != null) {
            dashboardShowtimeColumn.setCellValueFactory(cell ->
                    new SimpleStringProperty(safeText(resolveDashboardShowtime(cell.getValue()))));
        }
        if (dashboardSeatColumn != null) {
            dashboardSeatColumn.setCellValueFactory(cell ->
                    new SimpleIntegerProperty(cell.getValue().getSeatCount()).asObject());
        }
        if (dashboardRevenueColumn != null) {
            dashboardRevenueColumn.setCellValueFactory(cell ->
                    new SimpleIntegerProperty(cell.getValue().getRevenue()).asObject());
        }
        refreshDashboard();

        bookingTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        ticketColumn.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        movieColumn.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        timeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatBookingShowtime(cell.getValue())));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        timeStampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        bookingTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        bookingTable.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<BookingStore.BookingRecord>) change -> {
                    updateSelectedBookingState();
                }
        );
        updateSelectedBookingState();
        updateBookingActionVisibility();
        refreshBookings();

        movieTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        movieTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        if (movieGenreColumn != null) {
            movieGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        }
        
        movieDurationColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDuration() + " นาที"));
        movieDirectorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));
        movieShowtimesColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatShowtimesDisplay(cell.getValue().getShowtimes())));
        moviePosterColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPosterName() == null ? "-" : cell.getValue().getPosterName()));
                
        movieTitleColumn.setSortable(false);
        if (movieGenreColumn != null) movieGenreColumn.setSortable(false);
        movieDurationColumn.setSortable(false);
        movieDirectorColumn.setSortable(false);
        movieShowtimesColumn.setSortable(false);
        moviePosterColumn.setSortable(false);
        
        movieTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        movieTable.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<MovieStore.MovieRecord>) change -> {
                    updateSelectedMovieState();
                }
        );
        updateSelectedMovieState();
        refreshMovies();
    }

    @FXML
    private void handleRefresh() {
        refreshBookings();
    }

    @FXML
    private void handleDashboardRefresh() {
        refreshDashboard();
    }

    @FXML
    private void handleMovieRefresh() {
        refreshMovies();
    }

    @FXML
    private void handleEditBookingPopup() {
        if (selectedBooking == null) {
            statusLabel.setText("เลือก 1 รายการเพื่อแก้ไข");
            return;
        }
        BookingStore.BookingRecord updated = BookingEditDialog.show(selectedBooking);
        if (updated == null) {
            return;
        }
        BookingService.updateBooking(selectedBooking.getTicketId(), updated);
        statusLabel.setText("บันทึกการแก้ไขแล้ว");
        refreshBookings();
    }

    @FXML
    private void handleDeleteBooking() {
        List<BookingStore.BookingRecord> selectedItems = bookingTable.getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            statusLabel.setText("เลือกการจองก่อนลบ");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("ยืนยันการลบ");
        confirm.setHeaderText("ต้องการลบการจองที่เลือกหรือไม่?");
        confirm.setContentText("จำนวน " + selectedItems.size() + " รายการ");
        if (confirm.showAndWait().orElse(null) != ButtonType.OK) {
            return;
        }
        for (BookingStore.BookingRecord record : selectedItems) {
            BookingService.deleteBooking(record.getTicketId());
        }
        statusLabel.setText("ลบการจองแล้ว " + selectedItems.size() + " รายการ");
        selectedBooking = null;
        updateBookingActionVisibility();
        refreshBookings();
    }

    private void refreshBookings() {
        List<BookingStore.BookingRecord> bookings = BookingService.getBookings();
        ObservableList<BookingStore.BookingRecord> items = FXCollections.observableArrayList(bookings);
        bookingTable.setItems(items);
        bookingTable.getSelectionModel().clearSelection();
        updateSelectedBookingState();
        refreshDashboard();
    }

    private void refreshDashboard() {
        if (dashboardTable == null) {
            return;
        }
        List<BookingStore.BookingRecord> bookings = BookingService.getBookings();
        java.util.Map<String, DashboardRow> summary = new java.util.LinkedHashMap<>();
        for (BookingStore.BookingRecord record : bookings) {
            if (record == null) {
                continue;
            }
            String title = record.getMovieTitle();
            String time = record.getShowtime();
            if (title == null || title.isBlank()) {
                continue;
            }
            String key = buildDashboardKey(title, time);
            DashboardRow row = summary.get(key);
            if (row == null) {
                row = new DashboardRow(title, time == null ? "" : time.trim());
                summary.put(key, row);
            }
            row.addSeats(countSeats(record.getSeats()));
            row.addRevenue(TextUtils.parsePrice(record.getTotalPrice()));
        }
        List<DashboardRow> rows = summary.values().stream()
                .sorted((a, b) -> {
                    int byMovie = a.getMovieTitle().compareToIgnoreCase(b.getMovieTitle());
                    if (byMovie != 0) {
                        return byMovie;
                    }
                    return ShowtimeUtils.compareTokens(
                            ShowtimeUtils.normalizeToken(a.getShowtime()),
                            ShowtimeUtils.normalizeToken(b.getShowtime())
                    );
                })
                .collect(Collectors.toList());
        List<DashboardRow> output = new java.util.ArrayList<>();
        String currentMovie = null;
        int sumSeats = 0;
        int sumRevenue = 0;
        for (DashboardRow row : rows) {
            String rowTitle = safeText(row.getMovieTitle());
            if (currentMovie == null) {
                currentMovie = rowTitle;
            }
            if (!rowTitle.equalsIgnoreCase(currentMovie)) {
                output.add(DashboardRow.createSummary(currentMovie, sumSeats, sumRevenue));
                currentMovie = rowTitle;
                sumSeats = 0;
                sumRevenue = 0;
            }
            output.add(row);
            sumSeats += row.getSeatCount();
            sumRevenue += row.getRevenue();
        }
        if (currentMovie != null && !currentMovie.equals("-")) {
            output.add(DashboardRow.createSummary(currentMovie, sumSeats, sumRevenue));
        }
        String lastTitle = null;
        for (DashboardRow row : output) {
            if (row.isSummary()) {
                row.setDisplayTitle("");
                row.setDisplayShowtime("รวม");
                continue;
            }
            String currentTitle = safeText(row.getMovieTitle());
            if (lastTitle != null && !currentTitle.equals("-")
                    && currentTitle.equalsIgnoreCase(lastTitle)) {
                row.setDisplayTitle("");
            } else {
                row.setDisplayTitle(currentTitle.equals("-") ? "-" : row.getMovieTitle());
                lastTitle = currentTitle.equals("-") ? lastTitle : currentTitle;
            }
        }
        dashboardTable.setItems(FXCollections.observableArrayList(output));
    }

    private void refreshMovies() {
        List<MovieStore.MovieRecord> movies = MovieService.getMovies();
        ObservableList<MovieStore.MovieRecord> items = FXCollections.observableArrayList(movies);
        movieTable.setItems(items);
    }

    @FXML
    private void handleAddMoviePopup() {
        MovieFormResult result = MovieFormDialog.show(
                bookingTable == null ? null : bookingTable.getScene().getWindow(),
                null,
                false
        );
        if (result == null) {
            return;
        }
        Movie movie = new Movie(
                result.getTitle(),
                result.getDuration(),
                result.getGenre(),
                result.getDirector(),
                result.getDescription(),
                result.getPosterBase64() == null
                        ? MovieService.loadPoster(result.getPosterName())
                        : MovieStore.imageFromBase64(result.getPosterBase64())
        );
        MovieService.addMovie(movie, result.getShowtimes(), result.getPosterBase64(), result.getPosterName());
        statusLabel.setText("เพิ่มหนังแล้ว");
        refreshMovies();
    }

    @FXML
    private void handleEditMoviePopup() {
        if (selectedMovie == null) {
            statusLabel.setText("เลือกหนัง 1 รายการเพื่อแก้ไข");
            return;
        }
        MovieFormResult result = MovieFormDialog.show(
                bookingTable == null ? null : bookingTable.getScene().getWindow(),
                selectedMovie,
                true
        );
        if (result == null) {
            return;
        }
        Movie movie = new Movie(
                result.getTitle(),
                result.getDuration(),
                result.getGenre(),
                result.getDirector(),
                result.getDescription(),
                result.getPosterBase64() == null
                        ? MovieService.loadPoster(result.getPosterName())
                        : MovieStore.imageFromBase64(result.getPosterBase64())
        );
        MovieService.updateMovie(selectedMovie.getTitle(), movie, result.getShowtimes(), result.getPosterBase64(), result.getPosterName());
        statusLabel.setText("บันทึกการแก้ไขแล้ว");
        refreshMovies();
    }

    @FXML
    private void handleDeleteMovie() {
        List<MovieStore.MovieRecord> selectedItems = movieTable.getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            statusLabel.setText("เลือกหนังจากตารางก่อนลบ");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("ยืนยันการลบ");
        confirm.setHeaderText("ต้องการลบหนังที่เลือกหรือไม่?");
        confirm.setContentText("จำนวน " + selectedItems.size() + " รายการ");
        if (confirm.showAndWait().orElse(null) != ButtonType.OK) {
            return;
        }
        for (MovieStore.MovieRecord record : selectedItems) {
            MovieService.removeMovie(record.getTitle());
        }
        statusLabel.setText("ลบหนังแล้ว " + selectedItems.size() + " รายการ");
        selectedMovie = null;
        updateMovieActionVisibility();
        refreshMovies();
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/phanuwat/movie_booking/view/home.fxml")
            );
            bookingTable.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMovieActionVisibility() {
        int selectedCount = movieTable == null
                ? 0
                : movieTable.getSelectionModel().getSelectedItems().size();
        boolean hasSelection = selectedCount > 0;
        boolean canEdit = selectedCount == 1;
        if (updateButton != null) {
            updateButton.setDisable(!canEdit);
            updateButton.setVisible(canEdit);
            updateButton.setManaged(canEdit);
        }
        if (deleteButton != null) {
            deleteButton.setDisable(!hasSelection);
            deleteButton.setVisible(hasSelection);
            deleteButton.setManaged(hasSelection);
        }
        if (movieActionBox != null) {
            movieActionBox.setVisible(true);
            movieActionBox.setManaged(true);
        }
    }

    private void updateSelectedMovieState() {
        if (movieTable == null) {
            selectedMovie = null;
            return;
        }
        List<MovieStore.MovieRecord> selectedItems = movieTable.getSelectionModel().getSelectedItems();
        int selectedCount = selectedItems == null ? 0 : selectedItems.size();
        if (selectedCount == 1) {
            selectedMovie = selectedItems.get(0);
            statusLabel.setText("เลือกหนัง: " + selectedMovie.getTitle());
        } else if (selectedCount > 1) {
            selectedMovie = null;
            statusLabel.setText("เลือกหนัง " + selectedCount + " รายการ");
        } else {
            selectedMovie = null;
        }
        updateMovieActionVisibility();
    }

    private void updateBookingActionVisibility() {
        int selectedCount = bookingTable == null
                ? 0
                : bookingTable.getSelectionModel().getSelectedItems().size();
        boolean hasSelection = selectedCount > 0;
        boolean canEdit = selectedCount == 1;
        if (bookingEditButton != null) {
            bookingEditButton.setDisable(!canEdit);
            bookingEditButton.setVisible(canEdit);
            bookingEditButton.setManaged(canEdit);
        }
        if (bookingDeleteButton != null) {
            bookingDeleteButton.setDisable(!hasSelection);
            bookingDeleteButton.setVisible(hasSelection);
            bookingDeleteButton.setManaged(hasSelection);
        }
        if (bookingActionBox != null) {
            bookingActionBox.setVisible(true);
            bookingActionBox.setManaged(true);
        }
    }

    private void updateSelectedBookingState() {
        if (bookingTable == null) {
            selectedBooking = null;
            return;
        }
        List<BookingStore.BookingRecord> selectedItems = bookingTable.getSelectionModel().getSelectedItems();
        int selectedCount = selectedItems == null ? 0 : selectedItems.size();
        if (selectedCount == 1) {
            selectedBooking = selectedItems.get(0);
            statusLabel.setText("เลือกการจอง: " + selectedBooking.getTicketId());
        } else if (selectedCount > 1) {
            selectedBooking = null;
            statusLabel.setText("เลือกการจอง " + selectedCount + " รายการ");
        } else {
            selectedBooking = null;
        }
        updateBookingActionVisibility();
    }

    private String safeText(String value) {
        if (value == null) {
            return "-";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "-";
        }
        return trimmed;
    }

    private String safeTitle(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private String resolveDashboardShowtime(DashboardRow row) {
        if (row == null) {
            return "-";
        }
        String display = row.getDisplayShowtime();
        if (display != null && !display.isBlank()) {
            return display;
        }
        return formatDashboardShowtime(row.getMovieTitle(), row.getShowtime());
    }

    private String formatDashboardShowtime(String movieTitle, String showtime) {
        if (showtime == null || showtime.isBlank()) {
            return "-";
        }
        String normalized = ShowtimeUtils.normalizeToken(showtime);
        String timeOnly = showtime.trim();
        if (timeOnly.contains("@") || timeOnly.contains("|")) {
            return normalized == null ? "-" : normalized;
        }
        if (movieTitle != null) {
            List<Showtimes> showtimesList = MovieService.getShowtimesMap().get(movieTitle);
            if (showtimesList != null) {
                for (Showtimes st : showtimesList) {
                    if (st.getTime().equals(timeOnly)) {
                        return st.getTime() + "@" + st.getTheater();
                    }
                }
            }
        }
        return normalized == null ? "-" : normalized;
    }

    private int countSeats(String seatsText) {
        if (seatsText == null || seatsText.isBlank()) {
            return 0;
        }
        return (int) Arrays.stream(seatsText.split(","))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .count();
    }

    private String buildDashboardKey(String title, String showtime) {
        String safeTitle = title == null ? "" : title.trim();
        String safeTime = showtime == null ? "" : showtime.trim();
        return safeTitle + "@" + safeTime;
    }

    private String formatBookingShowtime(BookingStore.BookingRecord record) {
        String showtime = record.getShowtime();
        if (showtime == null || showtime.isEmpty()) {
            return "-";
        }
        if (showtime.contains("@") || showtime.contains("|")) {
            String normalized = ShowtimeUtils.normalizeToken(showtime);
            if (normalized != null && normalized.contains("@")) {
                String[] parts = normalized.split("@", 2);
                return parts[0] + " (โรง " + parts[1] + ")";
            }
        }
        String movieTitle = record.getMovieTitle();
        if (movieTitle != null) {
            List<Showtimes> showtimes = MovieService.getShowtimesMap().get(movieTitle);
            if (showtimes != null) {
                for (Showtimes st : showtimes) {
                    if (st.getTime().equals(showtime)) {
                        return st.getDisplayTime();
                    }
                }
            }
        }
        return showtime + " (โรง 1)";
    }

    private String formatShowtimesDisplay(List<String> showtimes) {
        if (showtimes == null || showtimes.isEmpty()) {
            return "-";
        }
        List<String> displayTimes = showtimes.stream()
                .map(ShowtimeUtils::normalizeToken)
                .filter(token -> token != null && !token.isBlank())
                .map(token -> {
                    String[] parts = token.split("@", 2);
                    return parts[0] + " (โรง " + parts[1] + ")";
                })
                .collect(Collectors.toList());
        if (displayTimes.isEmpty()) {
            return "-";
        }
        return String.join(", ", displayTimes);
    }

}
