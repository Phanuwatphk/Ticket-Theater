package com.phanuwat.movie_booking.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.phanuwat.movie_booking.store.BookingStore;
import com.phanuwat.movie_booking.data.MockData;
import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.store.MovieStore;
import com.phanuwat.movie_booking.model.Showtimes;
import com.phanuwat.movie_booking.util.ShowtimeUtils;

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
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

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
        ticketColumn.setResizable(false);
        movieColumn.setResizable(false);
        timeColumn.setResizable(false);
        seatsColumn.setResizable(false);
        nameColumn.setResizable(false);
        phoneColumn.setResizable(false);
        priceColumn.setResizable(false);
        timeStampColumn.setResizable(false);
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
        movieDurationColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDuration() + " นาที"));
        movieDirectorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));
        movieShowtimesColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatShowtimesDisplay(cell.getValue().getShowtimes())));
        moviePosterColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPosterName() == null ? "-" : cell.getValue().getPosterName()));
        movieTitleColumn.setSortable(false);
        movieDurationColumn.setSortable(false);
        movieDirectorColumn.setSortable(false);
        movieShowtimesColumn.setSortable(false);
        moviePosterColumn.setSortable(false);
        movieTitleColumn.setResizable(false);
        movieDurationColumn.setResizable(false);
        movieDirectorColumn.setResizable(false);
        movieShowtimesColumn.setResizable(false);
        moviePosterColumn.setResizable(false);
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
        Dialog<BookingStore.BookingRecord> dialog = new Dialog<>();
        dialog.setTitle("แก้ไขการจอง");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(selectedBooking.getName());
        TextField phoneField = new TextField(selectedBooking.getPhone());
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill:#b10d0d;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("ชื่อคนจอง"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("เบอร์คนจอง"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(errorLabel, 1, 2);
        pane.setContent(grid);

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
                errorLabel.setText("กรุณากรอกชื่อและเบอร์โทร");
                ev.consume();
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("ยืนยันการแก้ไข");
            confirm.setHeaderText("ต้องการบันทึกการแก้ไขหรือไม่?");
            confirm.setContentText(selectedBooking.getTicketId());
            if (confirm.showAndWait().orElse(null) != ButtonType.OK) {
                ev.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            return new BookingStore.BookingRecord(
                    selectedBooking.getTicketId(),
                    selectedBooking.getMovieTitle(),
                    selectedBooking.getShowtime(),
                    selectedBooking.getSeats(),
                    nameField.getText().trim(),
                    phoneField.getText().trim(),
                    parsePrice(selectedBooking.getTotalPrice()),
                    selectedBooking.getTimestamp()
            );
        });

        BookingStore.BookingRecord updated = dialog.showAndWait().orElse(null);
        if (updated == null) {
            return;
        }
        BookingStore.updateBooking(selectedBooking.getTicketId(), updated);
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
            BookingStore.deleteBooking(record.getTicketId());
        }
        statusLabel.setText("ลบการจองแล้ว " + selectedItems.size() + " รายการ");
        selectedBooking = null;
        updateBookingActionVisibility();
        refreshBookings();
    }

    private void refreshBookings() {
        List<BookingStore.BookingRecord> bookings = BookingStore.getBookings();
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
        List<BookingStore.BookingRecord> bookings = BookingStore.getBookings();
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
            row.addRevenue(parsePrice(record.getTotalPrice()));
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
        List<MovieStore.MovieRecord> movies = MovieStore.getMovies();
        ObservableList<MovieStore.MovieRecord> items = FXCollections.observableArrayList(movies);
        movieTable.setItems(items);
    }

    @FXML
    private void handleAddMoviePopup() {
        MovieFormResult result = showMovieFormDialog(null, false);
        if (result == null) {
            return;
        }
        Movie movie = new Movie(
                result.title,
                result.duration,
                result.director,
                result.description,
                result.posterBase64 == null ? MockData.loadPoster(result.posterName) : MovieStore.imageFromBase64(result.posterBase64)
        );
        MockData.addMovie(movie, result.showtimes, result.posterBase64, result.posterName);
        statusLabel.setText("เพิ่มหนังแล้ว");
        refreshMovies();
    }

    @FXML
    private void handleEditMoviePopup() {
        if (selectedMovie == null) {
            statusLabel.setText("เลือกหนัง 1 รายการเพื่อแก้ไข");
            return;
        }
        MovieFormResult result = showMovieFormDialog(selectedMovie, true);
        if (result == null) {
            return;
        }
        Movie movie = new Movie(
                result.title,
                result.duration,
                result.director,
                result.description,
                result.posterBase64 == null ? MockData.loadPoster(result.posterName) : MovieStore.imageFromBase64(result.posterBase64)
        );
        MockData.updateMovie(selectedMovie.getTitle(), movie, result.showtimes, result.posterBase64, result.posterName);
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
            MockData.removeMovie(record.getTitle());
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

    private int parsePrice(String priceText) {
        if (priceText == null) {
            return 0;
        }
        String digits = priceText.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
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
            List<Showtimes> showtimesList = MockData.getShowtimesMap().get(movieTitle);
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

    private MovieFormResult showMovieFormDialog(MovieStore.MovieRecord existing, boolean confirmOnOk) {
        Dialog<MovieFormResult> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "เพิ่มหนัง" : "แก้ไขหนัง");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(existing == null ? "" : existing.getTitle());
        TextField durationField = new TextField(existing == null ? "" : String.valueOf(existing.getDuration()));
        TextField directorField = new TextField(existing == null ? "" : existing.getDirector());
        
        // เพิ่ม TextArea สำหรับกรอกเรื่องย่อ
        TextArea descriptionField = new TextArea(existing == null || existing.getDescription() == null ? "" : existing.getDescription());
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        
        TextField imageField = new TextField(existing == null ? "" : (existing.getPosterName() == null ? "" : existing.getPosterName()));
        String showtimesText = existing == null || existing.getShowtimes() == null ? "" : existing.getShowtimes().stream()
                .map(ShowtimeUtils::normalizeToken)
                .filter(token -> token != null && !token.isBlank())
                .collect(Collectors.joining(", "));
        TextField showtimesField = new TextField(showtimesText);
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill:#b10d0d;");

        Path[] chosenPath = new Path[1];
        Button chooseButton = new Button("เลือกไฟล์");
        chooseButton.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("เลือกไฟล์รูปภาพ");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            java.io.File file = chooser.showOpenDialog(bookingTable.getScene().getWindow());
            if (file != null) {
                chosenPath[0] = file.toPath();
                imageField.setText(chosenPath[0].toString());
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("ชื่อหนัง"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("ความยาว (นาที)"), 0, 1);
        grid.add(durationField, 1, 1);
        grid.add(new Label("ผู้กำกับ"), 0, 2);
        grid.add(directorField, 1, 2);
        
        // เพิ่มเรื่องย่อลงไปในหน้าต่าง Dialog
        grid.add(new Label("เรื่องย่อ"), 0, 3);
        grid.add(descriptionField, 1, 3);
        
        grid.add(new Label("ไฟล์รูป"), 0, 4);
        HBox imageBox = new HBox(8, imageField, chooseButton);
        grid.add(imageBox, 1, 4);
        grid.add(new Label("รอบฉาย (เวลา@โรง)"), 0, 5);
        grid.add(showtimesField, 1, 5);
        Label showtimesHelp = new Label("ตัวอย่าง: 10:00@1, 15:00@2, 18:00@3");
        showtimesHelp.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        grid.add(showtimesHelp, 1, 6);
        grid.add(errorLabel, 1, 7);
        pane.setContent(grid);

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String title = titleField.getText().trim();
            String durationText = durationField.getText().trim();
            String director = directorField.getText().trim();
            String currentShowtimesText = showtimesField.getText().trim();
            if (title.isEmpty() || durationText.isEmpty() || director.isEmpty() || currentShowtimesText.isEmpty()) {
                errorLabel.setText("กรุณากรอกข้อมูลให้ครบ");
                ev.consume();
                return;
            }
            String formatError = validateShowtimeFormat(currentShowtimesText);
            if (formatError != null) {
                errorLabel.setText(formatError);
                ev.consume();
                return;
            }
            List<String> parsedShowtimes = parseShowtimeTokens(currentShowtimesText);
            if (parsedShowtimes.isEmpty()) {
                errorLabel.setText("กรุณากรอกรอบฉายให้ถูกต้อง");
                ev.consume();
                return;
            }
            try {
                Integer.parseInt(durationText);
            } catch (NumberFormatException e) {
                errorLabel.setText("ความยาวต้องเป็นตัวเลข");
                ev.consume();
                return;
            }
            if (confirmOnOk) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("ยืนยันการแก้ไข");
                confirm.setHeaderText("ต้องการบันทึกการแก้ไขหรือไม่?");
                confirm.setContentText(titleField.getText().trim());
                if (confirm.showAndWait().orElse(null) != ButtonType.OK) {
                    ev.consume();
                }
            }
        });

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            String title = titleField.getText().trim();
            int duration = Integer.parseInt(durationField.getText().trim());
            String director = directorField.getText().trim();
            String description = descriptionField.getText().trim(); // ดึงค่าเรื่องย่อ
            List<String> times = parseShowtimeTokens(showtimesField.getText());
            String posterBase64 = existing == null ? null : existing.getPosterBase64();
            String posterName = existing == null ? null : existing.getPosterName();

            Path picked = chosenPath[0];
            if (picked == null && !imageField.getText().trim().isEmpty()) {
                Path maybePath = java.nio.file.Paths.get(imageField.getText().trim());
                if (java.nio.file.Files.exists(maybePath)) {
                    picked = maybePath;
                } else {
                    posterName = imageField.getText().trim();
                }
            }
            if (picked != null) {
                try {
                    posterBase64 = MovieStore.encodeToBase64(picked);
                    posterName = picked.getFileName().toString();
                } catch (IOException e) {
                    return null;
                }
            }
            // คืนค่า MovieFormResult พร้อมกับตัวแปร description
            return new MovieFormResult(title, duration, director, description, times, posterBase64, posterName);
        });

        return dialog.showAndWait().orElse(null);
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
        // Try to find the theater from the showtime map
        String movieTitle = record.getMovieTitle();
        if (movieTitle != null) {
            List<Showtimes> showtimes = MockData.getShowtimesMap().get(movieTitle);
            if (showtimes != null) {
                for (Showtimes st : showtimes) {
                    if (st.getTime().equals(showtime)) {
                        return st.getDisplayTime();
                    }
                }
            }
        }
        // Fallback: assume theater 1
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

    private List<String> parseShowtimeTokens(String showtimesText) {
        if (showtimesText == null) {
            return List.of();
        }
        List<String> tokens = Arrays.stream(showtimesText.split(","))
                .map(String::trim)
                .map(ShowtimeUtils::normalizeToken)
                .filter(token -> token != null && !token.isBlank())
                .collect(Collectors.toList());
        tokens.sort(ShowtimeUtils::compareTokens);
        return tokens;
    }

    private String validateShowtimeFormat(String showtimesText) {
        if (showtimesText == null || showtimesText.trim().isEmpty()) {
            return "กรุณากรอกรอบฉายให้ถูกต้อง (เช่น 10:00@1, 15:00@2)";
        }
        String[] tokens = showtimesText.split(",");
        for (String rawToken : tokens) {
            String token = rawToken == null ? "" : rawToken.trim();
            if (token.isEmpty()) {
                continue;
            }
            String timePart = ShowtimeUtils.extractTime(token);
            String theaterPart = ShowtimeUtils.extractTheater(token);
            if (!ShowtimeUtils.isValidTime(timePart)) {
                return "รูปแบบเวลาไม่ถูกต้อง: " + token;
            }
            if (token.contains("@") || token.contains("|")) {
                if (!ShowtimeUtils.isValidTheater(theaterPart)) {
                    return "รูปแบบโรงไม่ถูกต้อง: " + token;
                }
            }
        }
        return null;
    }

    private static class MovieFormResult {
        private final String title;
        private final int duration;
        private final String director;
        private final String description; // ฟิลด์ใหม่
        private final List<String> showtimes;
        private final String posterBase64;
        private final String posterName;

        private MovieFormResult(
                String title,
                int duration,
                String director,
                String description,
                List<String> showtimes,
                String posterBase64,
                String posterName
        ) {
            this.title = title;
            this.duration = duration;
            this.director = director;
            this.description = description;
            this.showtimes = showtimes;
            this.posterBase64 = posterBase64;
            this.posterName = posterName;
        }
    }

    private static class DashboardRow {
        private final String movieTitle;
        private final String showtime;
        private int seatCount;
        private int revenue;
        private String displayTitle;
        private String displayShowtime;
        private boolean summary;

        private DashboardRow(String movieTitle, String showtime) {
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

        private void addSeats(int seats) {
            seatCount += Math.max(seats, 0);
        }

        private void addRevenue(int amount) {
            revenue += Math.max(amount, 0);
        }

        private void setDisplayTitle(String displayTitle) {
            this.displayTitle = displayTitle;
        }

        private void setDisplayShowtime(String displayShowtime) {
            this.displayShowtime = displayShowtime;
        }

        private void setSummary(boolean summary) {
            this.summary = summary;
        }

        private static DashboardRow createSummary(String movieTitle, int seats, int revenue) {
            DashboardRow row = new DashboardRow(movieTitle, "");
            row.seatCount = Math.max(seats, 0);
            row.revenue = Math.max(revenue, 0);
            row.setSummary(true);
            return row;
        }
    }
}