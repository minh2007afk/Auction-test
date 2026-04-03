package com.bidhub.client.controller;

import com.bidhub.client.core.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    // Kết nối với các thành phần trên giao diện FXML
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblError;

    /**
     * Hàm này tự động chạy khi màn hình được load lên.
     * Dùng để setup các giá trị mặc định.
     */
    @FXML
    public void initialize() {
        // Nạp dữ liệu vào ô chọn Vai trò
        cbRole.setItems(FXCollections.observableArrayList("Người đấu giá (Bidder)", "Người bán (Seller)"));
        cbRole.getSelectionModel().selectFirst(); // Chọn mặc định là Bidder
    }

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String roleStr = cbRole.getValue();

        // 1. Kiểm tra bỏ trống
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Kiểm tra mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!");
            return;
        }

        // 3. Quy đổi vai trò ra mã chuẩn (BIDDER hoặc SELLER)
        String roleCode = roleStr.contains("Bidder") ? "BIDDER" : "SELLER";

        lblError.setText("Đang gửi yêu cầu đăng ký...");
        lblError.setStyle("-fx-text-fill: blue;");

        // GÓI HÀNG ĐỂ GỬI ĐI: Mảng chứa [username, password, email, role]
        String[] registerData = {username, password, email, roleCode};

        // Tạo Request và Gửi
        com.bidhub.network.Request req = new com.bidhub.network.Request("REGISTER", registerData);
        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

        // Xử lý kết quả trả về
        if (res != null && res.isSuccess()) {
            System.out.println("✅ " + res.getMessage());
            // Đăng ký xong thì chuyển về màn hình Login để họ tự đăng nhập lại
            SceneManager.getInstance().switchTo("LoginView.fxml");
        } else {
            showError(res != null ? res.getMessage() : "Lỗi kết nối tới máy chủ!");
        }
    }

    @FXML
    private void switchToLogin() {
        // Nhờ Người điều phối chuyển về màn hình Login
        SceneManager.getInstance().switchTo("LoginView.fxml");
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: red;");
    }
}