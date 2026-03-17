package com.phanuwat.movie_booking.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class StaffLoginController {
    private static final String ADMIN_USER = "Admin";
    private static final String ADMIN_PASS = "Admin123";

    @FXML
    private BorderPane rootPane;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField == null ? "" : usernameField.getText().trim();
        String password = passwordField == null ? "" : passwordField.getText().trim();
        if (!ADMIN_USER.equals(username) || !ADMIN_PASS.equals(password)) {
            if (errorLabel != null) {
                errorLabel.setText("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
            if (passwordField != null) {
                passwordField.clear();
            }
            return;
        }
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/phanuwat/movie_booking/view/staff.fxml")
            );
            if (rootPane != null && rootPane.getScene() != null) {
                rootPane.getScene().setRoot(root);
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

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/phanuwat/movie_booking/view/home.fxml")
            );
            if (rootPane != null && rootPane.getScene() != null) {
                rootPane.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
