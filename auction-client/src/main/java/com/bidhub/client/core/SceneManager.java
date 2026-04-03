package com.bidhub.client.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Quản lý việc chuyển đổi giữa các màn hình trong ứng dụng JavaFX.
 * Sử dụng Singleton Pattern.
 */
public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    // Hàm này được gọi ở ClientApp khi mới bật ứng dụng để truyền cửa sổ chính vào
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Chuyển đổi sang một màn hình khác.
     * @param fxmlFileName Tên file fxml (ví dụ: "LoginView.fxml")
     */
    public void switchTo(String fxmlFileName) {
        try {
            // Đọc file FXML từ thư mục resources/views
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFileName));
            Parent root = loader.load();

            // Tạo Scene mới với kích thước mặc định 800x600
            Scene scene = new Scene(root, 800, 600);

            // Nhúng file CSS (nếu có) để làm đẹp giao diện
            java.net.URL cssUrl = getClass().getResource("/styles/main.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi load màn hình: " + fxmlFileName);
            e.printStackTrace();
        }
    }
}