package com.bidhub.client.controller;

import com.bidhub.client.core.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AuctionListController {

    @FXML private TableView<AuctionItem> tableAuctions;
    @FXML private TableColumn<AuctionItem, String> colName;
    @FXML private TableColumn<AuctionItem, String> colPrice;
    @FXML private TableColumn<AuctionItem, String> colStatus;

    /**
     * Hàm này tự động chạy khi giao diện được load.
     * Chúng ta sẽ dùng nó để setup các cột và nạp dữ liệu giả.
     */
    @FXML
    public void initialize() {
        // 1. Cài đặt cho các cột biết lấy dữ liệu từ thuộc tính nào của đối tượng
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Tạo một danh sách dữ liệu giả (Dummy data)
        ObservableList<AuctionItem> data = FXCollections.observableArrayList(
                new AuctionItem("iPhone 15 Pro Max 256GB", "25,000,000 VNĐ", "Đang diễn ra"),
                new AuctionItem("Laptop ThinkPad X1 Carbon", "18,500,000 VNĐ", "Sắp bắt đầu"),
                new AuctionItem("Đồng hồ Rolex Submariner", "150,000,000 VNĐ", "Đang diễn ra"),
                new AuctionItem("Màn hình Dell UltraSharp 27", "8,200,000 VNĐ", "Đã kết thúc")
        );

        // 3. Đưa dữ liệu vào bảng
        tableAuctions.setItems(data);
    }

    @FXML
    private void handleLogout() {
        System.out.println("Đang đăng xuất...");
        // Nhờ Người điều phối chuyển về màn hình Đăng nhập
        SceneManager.getInstance().switchTo("LoginView.fxml");
    }

    // =========================================================================
    // Class tĩnh nội bộ dùng làm dữ liệu tạm (Tuần sau sẽ dùng model thật từ Server)
    // =========================================================================
    public static class AuctionItem {
        private final String name;
        private final String price;
        private final String status;

        public AuctionItem(String name, String price, String status) {
            this.name = name;
            this.price = price;
            this.status = status;
        }

        // Bắt buộc phải có Getter thì TableView mới lấy dữ liệu ra được
        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getStatus() { return status; }
    }
}