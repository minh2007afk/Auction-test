package com.bidhub.client.controller;

import com.bidhub.client.core.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    // Các thẻ @FXML dùng để kết nối với các biến trong file FXML
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isBlank() || password.isBlank()) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        lblError.setText("Đang gửi qua Java Serialization...");
        lblError.setStyle("-fx-text-fill: blue;");

        // Đóng gói mảng dữ liệu
        String[] loginData = {username, password};

        // Tạo Request chuẩn
        com.bidhub.network.Request req = new com.bidhub.network.Request("LOGIN", loginData);

        // Gửi và nhận Response
        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

        // Xử lý giao diện
        if (res != null && res.isSuccess()) {
            System.out.println("✅ " + res.getMessage());
            com.bidhub.client.core.UserSession.setCurrentUser(username);
            com.bidhub.client.core.SceneManager.getInstance().switchTo("AuctionListView.fxml");
        } else {
            lblError.setText(res != null ? res.getMessage() : "Lỗi kết nối");
            lblError.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void switchToRegister() {
        System.out.println("Chuyển sang màn hình Đăng ký...");
        SceneManager.getInstance().switchTo("RegisterView.fxml");
    }
}