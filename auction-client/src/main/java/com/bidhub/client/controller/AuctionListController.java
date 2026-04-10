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
    @FXML private TableColumn<AuctionItem, String> colTimeLeft;
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
                    handleBidding(row.getItem()); // Mở form đấu giá xịn
                }
            });
            return row;
        });

        javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), event -> {
                    for (AuctionItem item : tableAuctions.getItems()) {
                        item.updateTimeLeft();
                    }
                    refreshTable();
                })
        );
        clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clock.play();
    }

    // NÂNG CẤP: Lưu thêm Mô tả và Ảnh vào Model, xử lý Giờ Phút Giây
    public static class AuctionItem {
        private final String name;
        private final String price;
        private final String status;
        private final String endTimeStr;
        private final String description;
        private final String imagePath;
        private String timeLeft;

        public AuctionItem(String name, String price, String status, String endTimeStr, String description, String imagePath) {
            this.name = name;
            this.price = price;
            this.status = status;
            this.endTimeStr = endTimeStr;
            this.description = description;
            this.imagePath = imagePath;
            updateTimeLeft();
        }

        public void updateTimeLeft() {
            try {
                if (endTimeStr == null || endTimeStr.isEmpty() || endTimeStr.equals("null")) {
                    this.timeLeft = "--:--:--";
                    return;
                }

                LocalDateTime end;
                // Thuật toán thông minh: Nhận diện cả 2 loại chuỗi thời gian (cũ và mới)
                if (endTimeStr.length() > 10) {
                    // Dữ liệu mới có đầy đủ Giờ Phút Giây (VD: 2026-04-30 20:15:00)
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    end = LocalDateTime.parse(endTimeStr, formatter);
                } else {
                    // Dữ liệu cũ chỉ có Ngày (VD: 2026-04-30) - Lùi về 00:00:00 đầu ngày
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    end = java.time.LocalDate.parse(endTimeStr, formatter).atStartOfDay();
                }

                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(now, end);

                if (duration.isNegative() || duration.isZero()) {
                    this.timeLeft = "Đã kết thúc";
                } else {
                    long days = duration.toDays();
                    long hours = duration.toHours() % 24;
                    long minutes = duration.toMinutes() % 60;
                    long seconds = duration.getSeconds() % 60;

                    if (days > 0) {
                        this.timeLeft = String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
                    } else {
                        // Ẩn số ngày nếu chỉ còn dưới 24h
                        this.timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    }
                }
            } catch (Exception e) {
                this.timeLeft = "Lỗi hiển thị";
            }
        }

        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getStatus() { return status; }
        public String getTimeLeft() { return timeLeft; }
        public String getDescription() { return description; }
        public String getImagePath() { return imagePath; }
    }

    private void refreshTable() {
        com.bidhub.network.Request req = new com.bidhub.network.Request("GET_AUCTIONS", null);
        com.bidhub.network.Response res = com.bidhub.client.network.NetworkManager.getInstance().sendRequest(req);

        if (res != null && res.isSuccess()) {
            java.util.List<String[]> dataList = (java.util.List<String[]>) res.getData();
            ObservableList<AuctionItem> tableData = FXCollections.observableArrayList();

            for (String[] row : dataList) {
                String desc = (row.length > 4 && row[4] != null) ? row[4] : "Chưa có mô tả";
                String img = (row.length > 5 && row[5] != null) ? row[5] : "no_image.png";
                tableData.add(new AuctionItem(row[0], row[1], row[2], row[3], desc, img));
            }

            javafx.application.Platform.runLater(() -> {
                int selectedIndex = tableAuctions.getSelectionModel().getSelectedIndex();
                tableAuctions.setItems(tableData);
                tableAuctions.getSelectionModel().select(selectedIndex);
            });
        }
    }

    @FXML
    private void handleLogout() {
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
                qrView.setFitWidth(350); qrView.setPreserveRatio(true);
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
                            alert.setTitle("Thành công"); alert.setHeaderText(null); alert.setContentText(res.getMessage()); alert.showAndWait();
                        } else {
                            showError(res != null ? res.getMessage() : "Mất kết nối với máy chủ!");
                        }
                    }
                });
            } catch (NumberFormatException e) { showError("Vui lòng nhập số hợp lệ (VD: 500000)!"); }
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

        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Chi tiết sản phẩm & Đặt giá");
        dialog.setHeaderText("Đang đấu giá: " + item.getName());

        javafx.scene.control.ButtonType btnBid = new javafx.scene.control.ButtonType("Tham gia Trả giá", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnBid, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        imgView.setFitWidth(300);
        imgView.setFitHeight(200);
        imgView.setPreserveRatio(true);
        String imgPath = item.getImagePath();

        if (imgPath != null && !imgPath.equals("no_image.png") && !imgPath.isEmpty()) {
            try {
                imgView.setImage(new javafx.scene.image.Image("file:" + imgPath));
            } catch (Exception e) {
                System.out.println("Không load được ảnh: " + imgPath);
            }
        }

        javafx.scene.control.Label lblDesc = new javafx.scene.control.Label("Mô tả: " + item.getDescription());
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(300);

        javafx.scene.control.Label lblPrice = new javafx.scene.control.Label("Giá cao nhất hiện tại: " + item.getPrice());
        lblPrice.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red;");

        javafx.scene.control.TextField txtBid = new javafx.scene.control.TextField();
        txtBid.setPromptText("Nhập mức giá bạn muốn trả (VNĐ)");

        vbox.getChildren().addAll(imgView, lblDesc, lblPrice, txtBid);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnBid) {
                return txtBid.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(bidAmountStr -> {
            try {
                double bidAmount = Double.parseDouble(bidAmountStr);
                if (bidAmount <= 0) { showError("Mức giá phải lớn hơn 0!"); return; }

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

    // =========================================================
    // NÂNG CẤP: GIAO DIỆN CHỌN NGÀY VÀ GIỜ/PHÚT
    // =========================================================
    @FXML
    private void handleAddProduct() {
        javafx.scene.control.Dialog<Object[]> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Đăng Bán Sản Phẩm");
        dialog.setHeaderText("Nhập thông tin, hình ảnh và thời gian Đấu giá");

        javafx.scene.control.ButtonType btnOk = new javafx.scene.control.ButtonType("Đăng Bán", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        javafx.scene.control.TextField txtName = new javafx.scene.control.TextField();
        txtName.setPromptText("Tên món hàng (VD: iPhone 15)");

        javafx.scene.control.TextField txtPrice = new javafx.scene.control.TextField();
        txtPrice.setPromptText("Giá khởi điểm (VNĐ)");

        // 1. Lịch chọn ngày
        javafx.scene.control.DatePicker dpEndDate = new javafx.scene.control.DatePicker();
        dpEndDate.setValue(java.time.LocalDate.now().plusDays(1));
        dpEndDate.setPrefWidth(120);

        // 2. Dropdown chọn Giờ (00 -> 23)
        javafx.scene.control.ComboBox<String> cbHour = new javafx.scene.control.ComboBox<>();
        for (int i = 0; i <= 23; i++) cbHour.getItems().add(String.format("%02d", i));
        cbHour.setValue("20"); // Mặc định 20h tối

        // 3. Dropdown chọn Phút (00, 05, 10... 55)
        javafx.scene.control.ComboBox<String> cbMinute = new javafx.scene.control.ComboBox<>();
        for (int i = 0; i < 60; i += 5) cbMinute.getItems().add(String.format("%02d", i));
        cbMinute.setValue("00");

        // Gộp Ngày + Giờ + Phút vào 1 hàng cho gọn
        javafx.scene.layout.HBox timeBox = new javafx.scene.layout.HBox(5);
        timeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        timeBox.getChildren().addAll(dpEndDate, new javafx.scene.control.Label(" Lúc: "), cbHour, new javafx.scene.control.Label(":"), cbMinute);

        javafx.scene.control.TextArea txtDesc = new javafx.scene.control.TextArea();
        txtDesc.setPromptText("Mô tả chi tiết tình trạng, xuất xứ, bảo hành...");
        txtDesc.setPrefRowCount(3);
        txtDesc.setWrapText(true);

        javafx.scene.control.Button btnChooseImage = new javafx.scene.control.Button("Chọn Ảnh...");
        javafx.scene.control.Label lblImageName = new javafx.scene.control.Label("Chưa chọn ảnh");

        final java.io.File[] selectedFile = new java.io.File[1];

        btnChooseImage.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Chọn ảnh sản phẩm");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            java.io.File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                selectedFile[0] = file;
                lblImageName.setText(file.getName());
            }
        });

        grid.add(new javafx.scene.control.Label("Tên sản phẩm:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new javafx.scene.control.Label("Giá khởi điểm:"), 0, 1);
        grid.add(txtPrice, 1, 1);

        // Gắn Box thời gian vào dòng thứ 3
        grid.add(new javafx.scene.control.Label("Kết thúc lúc:"), 0, 2);
        grid.add(timeBox, 1, 2);

        grid.add(new javafx.scene.control.Label("Mô tả chi tiết:"), 0, 3);
        grid.add(txtDesc, 1, 3);
        grid.add(new javafx.scene.control.Label("Ảnh sản phẩm:"), 0, 4);

        javafx.scene.layout.HBox imgBox = new javafx.scene.layout.HBox(10, btnChooseImage, lblImageName);
        grid.add(imgBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnOk) {
                // Ép kiểu Date + Hour + Minute thành chuỗi "yyyy-MM-dd HH:mm:00" gửi lên DB
                String dateStr = dpEndDate.getValue().toString();
                String timeStr = cbHour.getValue() + ":" + cbMinute.getValue() + ":00";
                String finalEndDateTime = dateStr + " " + timeStr;

                return new Object[]{txtName.getText(), txtPrice.getText(), finalEndDateTime, txtDesc.getText(), selectedFile[0]};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String name = ((String) result[0]).trim();
            String priceStr = ((String) result[1]).trim();
            String endTimeStr = (String) result[2]; // Nhận chuỗi thời gian hoàn chỉnh
            String description = ((String) result[3]).trim();
            java.io.File imgFile = (java.io.File) result[4];

            if (name.isEmpty() || priceStr.isEmpty() || endTimeStr.isEmpty()) {
                showError("Vui lòng điền đủ Tên, Giá và Ngày kết thúc!");
                return;
            }

            // Kiểm tra thời gian kết thúc phải lớn hơn thời gian hiện tại
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime endDateTime = LocalDateTime.parse(endTimeStr, fmt);
                if (endDateTime.isBefore(LocalDateTime.now())) {
                    showError("Thời gian kết thúc phải ở trong tương lai!");
                    return;
                }
            } catch (Exception ex) {
                showError("Lỗi định dạng thời gian!");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                String sellerName = com.bidhub.client.core.UserSession.getCurrentUser();

                byte[] imageBytes = new byte[0];
                String extension = "";
                if (imgFile != null) {
                    imageBytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
                    String fileName = imgFile.getName();
                    extension = fileName.substring(fileName.lastIndexOf("."));
                }

                Object[] payload = {sellerName, name, price, endTimeStr, description, imageBytes, extension};
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
            } catch (java.io.IOException e) {
                showError("Lỗi không thể đọc file ảnh: " + e.getMessage());
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