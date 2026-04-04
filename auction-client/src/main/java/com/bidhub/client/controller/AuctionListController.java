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
    @FXML private javafx.scene.control.Label lblBalance;

    /**
     * Hàm này tự động chạy khi giao diện được load.
     * Chúng ta sẽ dùng nó để setup các cột và nạp dữ liệu giả.
     */
    @FXML
    public void initialize() {
        // 1. Cài đặt cho các cột biết lấy dữ liệu từ thuộc tính nào
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Gọi điện lên Server xin danh sách Đấu giá
        System.out.println("Đang tải dữ liệu từ Server...");
        com.bidhub.network.Request req = new com.bidhub.network.Request("GET_AUCTIONS", null);
        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

        // 3. Xử lý dữ liệu Server trả về
        if (res != null && res.isSuccess()) {
            // Ép kiểu dữ liệu nhận được thành List các mảng String
            java.util.List<String[]> dataList = (java.util.List<String[]>) res.getData();

            // Chuyển đổi thành định dạng ObservableList mà TableView yêu cầu
            ObservableList<AuctionItem> tableData = FXCollections.observableArrayList();
            for (String[] row : dataList) {
                // row[0] là tên, row[1] là giá, row[2] là trạng thái
                tableData.add(new AuctionItem(row[0], row[1], row[2]));
            }

            // Đổ dữ liệu thật vào bảng!
            tableAuctions.setItems(tableData);
            System.out.println("✅ Tải danh sách đấu giá thành công!");
        } else {
            System.err.println("❌ Lỗi tải dữ liệu: " + (res != null ? res.getMessage() : "Mất kết nối"));
        }
        refreshBalance();

        // DẠY BẢNG LẮNG NGHE SỰ KIỆN NHẤP ĐÚP CHUỘT
        tableAuctions.setRowFactory(tv -> {
            javafx.scene.control.TableRow<AuctionItem> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                // Nếu click 2 lần và dòng đó có dữ liệu (không click vào chỗ trống)
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    AuctionItem selectedItem = row.getItem();
                    handleBidding(selectedItem); // Gọi hàm xử lý đặt giá
                }
            });
            return row;
        });
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

    @FXML
    private void handleTopUp() {
        // 1. Hỏi số tiền muốn nạp
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Nạp tiền vào ví");
        dialog.setHeaderText("Cổng thanh toán BidHub");
        dialog.setContentText("Nhập số tiền muốn nạp (VNĐ):");

        java.util.Optional<String> result = dialog.showAndWait();

        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) { showError("Số tiền nạp phải lớn hơn 0!"); return; }

                String username = "minhminh"; // Tạm thời hardcode tài khoản đang đăng nhập

                // ==========================================
                // 2. TẠO MÃ VIETQR (Bạn hãy thay thông tin của bạn vào đây)
                // ==========================================
                String bankId = "MB";                 // Mã ngân hàng (VD: MB, VCB, TCB, BIDV, VPB...)
                String accountNo = "0355438207";      // Số tài khoản của bạn
                String accountName = "NGUYEN CONG MINH"; // Tên chủ tài khoản (Viết không dấu)
                String memo = "BidHub nap " + username;  // Nội dung chuyển khoản

                // Tạo link ảnh QR thông minh của VietQR
                String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%.0f&addInfo=%s&accountName=%s",
                        bankId, accountNo, amount, memo.replace(" ", "%20"), accountName.replace(" ", "%20"));

                // 3. Hiển thị Popup chứa mã QR
                javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(qrUrl));
                qrView.setFitWidth(350);
                qrView.setPreserveRatio(true);

                javafx.scene.control.Alert qrAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                qrAlert.setTitle("Quét mã thanh toán");
                qrAlert.setHeaderText("Mở App Ngân hàng quét mã để nạp " + String.format("%,.0f VNĐ", amount));
                qrAlert.setGraphic(qrView);
                qrAlert.setContentText("Vui lòng quét mã trên. Bấm OK sau khi bạn đã chuyển khoản thành công!");

                // 4. Chờ người dùng quét xong và bấm OK
                qrAlert.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        // 5. Gửi request báo Server cộng tiền (Giả lập việc Admin đã xác nhận)
                        Object[] payload = {username, amount};
                        com.bidhub.network.Request req = new com.bidhub.network.Request("TOPUP", payload);
                        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

                        if (res != null && res.isSuccess()) {
                            refreshBalance();

                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Thành công");
                            alert.setHeaderText(null);
                            alert.setContentText(res.getMessage());
                            alert.showAndWait();

                        } else {
                            showError(res != null ? res.getMessage() : "Mất kết nối với máy chủ!");
                        }
                    }
                });

            } catch (NumberFormatException e) {
                showError("Vui lòng nhập số hợp lệ (VD: 500000)!");
            }
        });
    }

    // Hàm phụ trợ để hiện Pop-up báo lỗi cho đẹp
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Lỗi giao dịch");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Hàm gọi Server để lấy số dư mới nhất
    private void refreshBalance() {
        String username = "minhminh"; // Tài khoản đang test

        com.bidhub.network.Request req = new com.bidhub.network.Request("GET_BALANCE", username);
        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

        if (res != null && res.isSuccess()) {
            // Ép kiểu an toàn
            double balance = Double.parseDouble(res.getData().toString());

            // Ép giao diện JavaFX phải cập nhật NGAY LẬP TỨC
            javafx.application.Platform.runLater(() -> {
                lblBalance.setText(String.format("Số dư: %,.0f VNĐ", balance));
            });
        }
    }

    // Hàm xử lý khi người dùng nhấp đúp vào sản phẩm để đặt giá
    private void handleBidding(AuctionItem item) {
        // 1. Kiểm tra trạng thái (Chỉ cho phép đấu giá khi đang diễn ra)
        if (!"Đang diễn ra".equals(item.getStatus())) {
            showError("Sản phẩm này hiện không trong thời gian đấu giá!");
            return;
        }

        // 2. Hiện Pop-up nhập giá tiền
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Tham gia đấu giá");
        dialog.setHeaderText("Đang đấu giá: " + item.getName() + "\nGiá cao nhất hiện tại: " + item.getPrice());
        dialog.setContentText("Nhập mức giá bạn muốn trả (VNĐ):");

        dialog.showAndWait().ifPresent(bidAmountStr -> {
            try {
                double bidAmount = Double.parseDouble(bidAmountStr);
                if (bidAmount <= 0) {
                    showError("Mức giá phải lớn hơn 0!");
                    return;
                }

                String username = "minhminh"; // Tạm thời hardcode tài khoản

                // 3. Đóng gói gửi lên Server [Tên tài khoản, Tên món hàng, Số tiền cược]
                Object[] payload = {username, item.getName(), bidAmount};
                com.bidhub.network.Request req = new com.bidhub.network.Request("PLACE_BID", payload);
                com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

                // 4. Báo kết quả
                if (res != null && res.isSuccess()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Đặt giá thành công");
                    alert.setHeaderText(null);
                    alert.setContentText(res.getMessage());
                    alert.showAndWait();

                    // Trừ tiền xong thì cập nhật lại số dư trên góc
                    refreshBalance();

                    // TODO: Tạm thời bạn phải Đăng xuất ra vào lại để bảng cập nhật giá mới.
                    // Bước sau chúng ta sẽ viết hàm tự động cập nhật bảng!
                } else {
                    showError(res != null ? res.getMessage() : "Mất kết nối với máy chủ!");
                }
            } catch (NumberFormatException e) {
                showError("Vui lòng nhập số tiền hợp lệ!");
            }
        });
    }
}