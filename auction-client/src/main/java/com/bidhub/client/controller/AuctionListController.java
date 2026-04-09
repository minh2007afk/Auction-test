package com.bidhub.client.controller;

import com.bidhub.client.core.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class AuctionListController {

    @FXML private TableView<AuctionItem> tableAuctions;
    @FXML private TableColumn<AuctionItem, String> colName;
    @FXML private TableColumn<AuctionItem, String> colPrice;
    @FXML private TableColumn<AuctionItem, String> colStatus;
    @FXML private TableColumn<AuctionItem, String> colTimeLeft; // Cột đếm ngược
    @FXML private javafx.scene.control.Label lblBalance;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        colTimeLeft.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timeLeft"));

        refreshBalance();
        refreshTable();

        // Lắng nghe nhấp đúp
        tableAuctions.setRowFactory(tv -> {
            javafx.scene.control.TableRow<AuctionItem> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleBidding(row.getItem());
                }
            });
            return row;
        });

        // =========================================================
        // ĐỒNG HỒ ĐẾM NGƯỢC CHẠY MỖI 1 GIÂY & TỰ ĐỘNG LÀM MỚI BẢNG
        // =========================================================
        javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    // Cập nhật lại chuỗi hiển thị thời gian cho từng dòng trong bảng
                    for (AuctionItem item : tableAuctions.getItems()) {
                        item.updateTimeLeft();
                    }

                    // Gọi luôn refreshTable() mỗi 1 giây để ép bảng cập nhật giá siêu tốc
                    refreshTable();
                })
        );
        clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clock.play();
    }

    // Class AuctionItem được nâng cấp để tự tính toán thời gian
    public static class AuctionItem {
        private final String name;
        private final String price;
        private final String status;
        private final String endTimeStr;
        private String timeLeft;

        public AuctionItem(String name, String price, String status, String endTimeStr) {
            this.name = name;
            this.price = price;
            this.status = status;
            this.endTimeStr = endTimeStr;
            updateTimeLeft(); // Tính toán lần đầu
        }

        public void updateTimeLeft() {
            try {
                if (endTimeStr == null || endTimeStr.isEmpty()) {
                    this.timeLeft = "--:--:--";
                    return;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime end = java.time.LocalDate.parse(endTimeStr, formatter).atStartOfDay();
                LocalDateTime now = LocalDateTime.now();

                Duration duration = Duration.between(now, end);
                if (duration.isNegative()) {
                    this.timeLeft = "Đã kết thúc";
                } else {
                    long days = duration.toDays();
                    long hours = duration.toHours() % 24;
                    long minutes = duration.toMinutes() % 60;
                    long seconds = duration.getSeconds() % 60;
                    this.timeLeft = String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
                }
            } catch (Exception e) {
                this.timeLeft = "Vô thời hạn";
            }
        }

        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getStatus() { return status; }
        public String getTimeLeft() { return timeLeft; }
    }

    private void refreshTable() {
        com.bidhub.network.Request req = new com.bidhub.network.Request("GET_AUCTIONS", null);
        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

        if (res != null && res.isSuccess()) {
            java.util.List<String[]> dataList = (java.util.List<String[]>) res.getData();
            ObservableList<AuctionItem> tableData = FXCollections.observableArrayList();

            for (String[] row : dataList) {
                tableData.add(new AuctionItem(row[0], row[1], row[2], row[3]));
            }

            javafx.application.Platform.runLater(() -> {
                // Lưu lại vị trí cuộn để không bị giật mình khi refresh 1s/lần
                int selectedIndex = tableAuctions.getSelectionModel().getSelectedIndex();
                tableAuctions.setItems(tableData);
                tableAuctions.getSelectionModel().select(selectedIndex);
            });
        }
    }

    @FXML
    private void handleLogout() {
        System.out.println("Đang đăng xuất...");
        com.bidhub.client.core.UserSession.clear();
        SceneManager.getInstance().switchTo("LoginView.fxml");
    }

    @FXML
    private void handleTopUp() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Nạp tiền vào ví");
        dialog.setHeaderText("Cổng thanh toán BidHub");
        dialog.setContentText("Nhập số tiền muốn nạp (VNĐ):");

        java.util.Optional<String> result = dialog.showAndWait();

        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) { showError("Số tiền nạp phải lớn hơn 0!"); return; }

                String username = com.bidhub.client.core.UserSession.getCurrentUser();

                String bankId = "MB";
                String accountNo = "0355438207";
                String accountName = "NGUYEN CONG MINH";
                String memo = "BidHub nap " + username;

                String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%.0f&addInfo=%s&accountName=%s",
                        bankId, accountNo, amount, memo.replace(" ", "%20"), accountName.replace(" ", "%20"));

                javafx.scene.image.ImageView qrView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(qrUrl));
                qrView.setFitWidth(350);
                qrView.setPreserveRatio(true);

                javafx.scene.control.Alert qrAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                qrAlert.setTitle("Quét mã thanh toán");
                qrAlert.setHeaderText("Mở App Ngân hàng quét mã để nạp " + String.format("%,.0f VNĐ", amount));
                qrAlert.setGraphic(qrView);
                qrAlert.setContentText("Vui lòng quét mã trên. Bấm OK sau khi bạn đã chuyển khoản thành công!");

                qrAlert.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
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

    private void refreshBalance() {
        String username = com.bidhub.client.core.UserSession.getCurrentUser();
        com.bidhub.network.Request req = new com.bidhub.network.Request("GET_BALANCE", username);
        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

        if (res != null && res.isSuccess()) {
            double balance = Double.parseDouble(res.getData().toString());
            javafx.application.Platform.runLater(() -> {
                lblBalance.setText(String.format("Số dư: %,.0f VNĐ", balance));
            });
        }
    }

    private void handleBidding(AuctionItem item) {
        if (!"Đang diễn ra".equals(item.getStatus())) {
            showError("Sản phẩm này hiện không trong thời gian đấu giá!");
            return;
        }

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

                String username = com.bidhub.client.core.UserSession.getCurrentUser();

                Object[] payload = {username, item.getName(), bidAmount};
                com.bidhub.network.Request req = new com.bidhub.network.Request("PLACE_BID", payload);
                com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

                if (res != null && res.isSuccess()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Đặt giá thành công");
                    alert.setHeaderText(null);
                    alert.setContentText(res.getMessage());
                    alert.showAndWait();

                    refreshBalance();
                    refreshTable();
                } else {
                    showError(res != null ? res.getMessage() : "Mất kết nối với máy chủ!");
                }
            } catch (NumberFormatException e) {
                showError("Vui lòng nhập số tiền hợp lệ!");
            }
        });
    }

    @FXML
    private void handleAddProduct() {
        // NÂNG CẤP: Dùng mảng Object[] để chứa được Tên, Giá và Lịch Ngày kết thúc
        javafx.scene.control.Dialog<Object[]> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Đăng Bán Sản Phẩm");
        dialog.setHeaderText("Nhập thông tin món hàng bạn muốn bán");

        javafx.scene.control.ButtonType btnOk = new javafx.scene.control.ButtonType("Đăng Bán", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);

        javafx.scene.control.TextField txtName = new javafx.scene.control.TextField();
        txtName.setPromptText("Tên món hàng (VD: Macbook Pro)");

        javafx.scene.control.TextField txtPrice = new javafx.scene.control.TextField();
        txtPrice.setPromptText("Giá khởi điểm (VNĐ)");

        // THÊM LỊCH CHỌN NGÀY KẾT THÚC
        javafx.scene.control.DatePicker dpEndDate = new javafx.scene.control.DatePicker();
        dpEndDate.setPromptText("Chọn ngày kết thúc");
        dpEndDate.setValue(java.time.LocalDate.now().plusDays(1)); // Mặc định là ngày mai

        grid.add(new javafx.scene.control.Label("Tên sản phẩm:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new javafx.scene.control.Label("Giá khởi điểm:"), 0, 1);
        grid.add(txtPrice, 1, 1);
        grid.add(new javafx.scene.control.Label("Ngày kết thúc:"), 0, 2);
        grid.add(dpEndDate, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnOk) {
                return new Object[]{txtName.getText(), txtPrice.getText(), dpEndDate.getValue()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String name = ((String) result[0]).trim();
            String priceStr = ((String) result[1]).trim();
            java.time.LocalDate endDate = (java.time.LocalDate) result[2];

            if (name.isEmpty() || priceStr.isEmpty() || endDate == null) {
                showError("Vui lòng điền đầy đủ thông tin!");
                return;
            }

            if (endDate.isBefore(java.time.LocalDate.now())) {
                showError("Ngày kết thúc không được nằm trong quá khứ!");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                String sellerName = com.bidhub.client.core.UserSession.getCurrentUser();
                String endTimeStr = endDate.toString(); // Chuyển ngày thành chuỗi YYYY-MM-DD để gửi lên Server

                // Đóng gói đủ 4 tham số gửi lên
                Object[] payload = {sellerName, name, price, endTimeStr};
                com.bidhub.network.Request req = new com.bidhub.network.Request("ADD_ITEM", payload);
                com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

                if (res != null && res.isSuccess()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText(res.getMessage());
                    alert.showAndWait();

                    refreshTable();
                } else {
                    showError(res != null ? res.getMessage() : "Lỗi mạng!");
                }
            } catch (NumberFormatException e) {
                showError("Giá tiền phải là một số hợp lệ!");
            }
        });
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Lỗi giao dịch");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}