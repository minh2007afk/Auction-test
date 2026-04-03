package com.bidhub.client;

import com.bidhub.client.core.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("BidHub - Đấu Giá Trực Tuyến");

        // 1. Kết nối thẳng tới Server đang chạy trên chính máy này (localhost - 127.0.0.1)
        com.bidhub.client.network.NetworkManager.getInstance().connect("127.0.0.1", 8888);

        // 2. Giao cửa sổ chính cho SceneManager quản lý
        SceneManager.getInstance().setPrimaryStage(primaryStage);

        // 3. Yêu cầu load màn hình Login đầu tiên
        SceneManager.getInstance().switchTo("LoginView.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}