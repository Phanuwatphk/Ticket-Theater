package com.phanuwat.movie_booking.controller.staff;

import com.phanuwat.movie_booking.store.BookingStore;
import com.phanuwat.movie_booking.util.TextUtils;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public final class BookingEditDialog {
    private BookingEditDialog() {
    }

    public static BookingStore.BookingRecord show(BookingStore.BookingRecord selectedBooking) {
        if (selectedBooking == null) {
            return null;
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
                    TextUtils.parsePrice(selectedBooking.getTotalPrice()),
                    selectedBooking.getTimestamp()
            );
        });

        return dialog.showAndWait().orElse(null);
    }
}
