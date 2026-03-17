package com.phanuwat.movie_booking.controller.staff;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.phanuwat.movie_booking.store.MovieStore;
import com.phanuwat.movie_booking.util.ShowtimeFormUtils;
import com.phanuwat.movie_booking.util.ShowtimeUtils;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public final class MovieFormDialog {
    private MovieFormDialog() {
    }

    public static MovieFormResult show(Window owner, MovieStore.MovieRecord existing, boolean confirmOnOk) {
        Dialog<MovieFormResult> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "เพิ่มหนัง" : "แก้ไขหนัง");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(existing == null ? "" : existing.getTitle());
        TextField durationField = new TextField(existing == null ? "" : String.valueOf(existing.getDuration()));
        TextField genreField = new TextField(existing == null || existing.getGenre() == null ? "" : existing.getGenre());
        TextField directorField = new TextField(existing == null ? "" : existing.getDirector());

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
            java.io.File file = chooser.showOpenDialog(owner);
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
        grid.add(new Label("ประเภท"), 0, 2);
        grid.add(genreField, 1, 2);
        grid.add(new Label("ผู้กำกับ"), 0, 3);
        grid.add(directorField, 1, 3);
        grid.add(new Label("เรื่องย่อ"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(new Label("ไฟล์รูป"), 0, 5);
        HBox imageBox = new HBox(8, imageField, chooseButton);
        grid.add(imageBox, 1, 5);
        grid.add(new Label("รอบฉาย (เวลา@โรง)"), 0, 6);
        grid.add(showtimesField, 1, 6);
        Label showtimesHelp = new Label("ตัวอย่าง: 10:00@1, 15:00@2, 18:00@3");
        showtimesHelp.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        grid.add(showtimesHelp, 1, 7);
        grid.add(errorLabel, 1, 8);
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
            String formatError = ShowtimeFormUtils.validateFormat(currentShowtimesText);
            if (formatError != null) {
                errorLabel.setText(formatError);
                ev.consume();
                return;
            }
            List<String> parsedShowtimes = ShowtimeFormUtils.parseTokens(currentShowtimesText);
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
            String genre = genreField.getText().trim();
            String director = directorField.getText().trim();
            String description = descriptionField.getText().trim();
            List<String> times = ShowtimeFormUtils.parseTokens(showtimesField.getText());
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
            return new MovieFormResult(title, duration, genre, director, description, times, posterBase64, posterName);
        });

        return dialog.showAndWait().orElse(null);
    }
}
