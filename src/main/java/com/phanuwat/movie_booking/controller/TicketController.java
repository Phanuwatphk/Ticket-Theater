package com.phanuwat.movie_booking.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.phanuwat.movie_booking.model.Movie;
import com.phanuwat.movie_booking.model.Seat;
import com.phanuwat.movie_booking.model.Showtimes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;

public class TicketController {
    @FXML
    private StackPane ticketCard;

    @FXML
    private ImageView posterImage;

    @FXML
    private Label ticketIdLabel;

    @FXML
    private Label movieLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label seatBigLabel;

    @FXML
    private Label seatsLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label seatIndexLabel;

    @FXML
    private Button prevSeatButton;

    @FXML
    private Button nextSeatButton;

    private List<String> seatCodes = new ArrayList<>();
    private int seatIndex = 0;
    private Integer ticketStart = null;
    private Integer ticketEnd = null;
    private String ticketRange = "";

    public void setTicketData(
            Movie movie,
            Showtimes showtime,
            List<Seat> seats,
            String name,
            String phone,
            String ticketId
    ) {
        posterImage.setImage(movie.getPoster());
        ticketRange = ticketId == null ? "" : ticketId.trim();
        parseTicketRange(ticketRange);
        movieLabel.setText(movie.getTitle());
        timeLabel.setText("รอบฉาย: " + showtime.getDisplayTime());
        seatCodes = seats.stream()
                .map(seat -> seat.getRow() + seat.getColumn())
                .collect(Collectors.toList());
        seatIndex = 0;
        nameLabel.setText(safeValue(name));
        phoneLabel.setText(safeValue(phone));
        updateTicketView();
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/phanuwat/movie_booking/view/home.fxml")
            );
            posterImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFinish() {
        handleBack();
    }

    @FXML
    private void handleDownload() {
        if (ticketCard == null || ticketCard.getScene() == null) {
            return;
        }
        if (seatCodes.size() <= 1) {
            saveSingleTicket();
        } else {
            saveAllTickets();
        }
    }

    @FXML
    private void handlePrevSeat() {
        if (seatIndex > 0) {
            seatIndex -= 1;
            updateTicketView();
        }
    }

    @FXML
    private void handleNextSeat() {
        if (seatIndex < seatCodes.size() - 1) {
            seatIndex += 1;
            updateTicketView();
        }
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim();
    }

    private void updateTicketView() {
        String seatCode = seatCodes.isEmpty() ? "-" : seatCodes.get(seatIndex);
        seatBigLabel.setText(seatCode);
        seatsLabel.setText("ที่นั่ง: " + seatCode);
        ticketIdLabel.setText("Ticket ID: " + getCurrentTicketId());
        if (seatIndexLabel != null) {
            int total = Math.max(seatCodes.size(), 1);
            seatIndexLabel.setText((seatIndex + 1) + "/" + total);
        }
        if (prevSeatButton != null) {
            prevSeatButton.setDisable(seatIndex <= 0);
        }
        if (nextSeatButton != null) {
            nextSeatButton.setDisable(seatIndex >= seatCodes.size() - 1);
        }
        if (seatCodes.size() <= 1) {
            if (prevSeatButton != null) {
                prevSeatButton.setDisable(true);
            }
            if (nextSeatButton != null) {
                nextSeatButton.setDisable(true);
            }
        }
    }

    private void saveSingleTicket() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("บันทึกตั๋วเป็นรูปภาพ");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );
        chooser.setInitialFileName(buildDefaultFileName(getCurrentTicketIdSafe(), getCurrentSeatSafe()));
        File file = chooser.showSaveDialog(ticketCard.getScene().getWindow());
        if (file == null) {
            return;
        }
        writeTicketImage(file);
    }

    private void saveAllTickets() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("เลือกโฟลเดอร์สำหรับบันทึกรูปตั๋ว");
        File dir = chooser.showDialog(ticketCard.getScene().getWindow());
        if (dir == null) {
            return;
        }
        int originalIndex = seatIndex;
        for (int i = 0; i < seatCodes.size(); i++) {
            seatIndex = i;
            updateTicketView();
            String seatSafe = safeFilePart(seatCodes.get(i));
            String ticketSafe = safeFilePart(getCurrentTicketId());
            File file = new File(dir, buildDefaultFileName(ticketSafe, seatSafe));
            writeTicketImage(file);
        }
        seatIndex = originalIndex;
        updateTicketView();
    }

    private void writeTicketImage(File file) {
        try {
            WritableImage image = ticketCard.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentSeatSafe() {
        if (seatCodes.isEmpty()) {
            return "seat";
        }
        return safeFilePart(seatCodes.get(seatIndex));
    }

    private String getCurrentTicketId() {
        if (ticketStart != null) {
            int id = ticketStart + seatIndex;
            if (ticketEnd != null && id > ticketEnd) {
                id = ticketEnd;
            }
            return formatTicketId(id);
        }
        if (ticketRange != null && !ticketRange.isBlank()) {
            return ticketRange;
        }
        return "ticket";
    }

    private String getCurrentTicketIdSafe() {
        return safeFilePart(getCurrentTicketId());
    }

    private String safeFilePart(String value) {
        if (value == null || value.isBlank()) {
            return "seat";
        }
        return value.replaceAll("[^a-zA-Z0-9\\-_.]", "_");
    }

    private String buildDefaultFileName(String ticketSuffix, String seatSuffix) {
        String safeTicket = ticketSuffix == null || ticketSuffix.isBlank()
                ? "ticket"
                : safeFilePart(ticketSuffix);
        String safeSeat = seatSuffix == null ? "" : safeFilePart(seatSuffix);
        if (safeSeat.isBlank()) {
            return "ticket-" + safeTicket + ".png";
        }
        return "ticket-" + safeTicket + "-" + safeSeat + ".png";
    }

    private void parseTicketRange(String ticketId) {
        ticketStart = null;
        ticketEnd = null;
        if (ticketId == null || ticketId.isBlank()) {
            return;
        }
        String trimmed = ticketId.trim();
        if (trimmed.contains("-")) {
            String[] parts = trimmed.split("-", 2);
            Integer start = extractTicketNumber(parts[0]);
            Integer end = extractTicketNumber(parts[1]);
            if (start != null) {
                ticketStart = start;
                ticketEnd = end == null ? start : end;
            }
            return;
        }
        Integer single = extractTicketNumber(trimmed);
        if (single != null) {
            ticketStart = single;
            ticketEnd = single;
        }
    }

    private Integer extractTicketNumber(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatTicketId(int number) {
        return String.format("T%04d", number);
    }
}
