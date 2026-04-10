package com.bidhub.server.network;

import com.bidhub.network.Request;
import com.bidhub.network.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientIp;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIp = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            System.out.println("[Server] Có Client kết nối từ: " + clientIp);

            // LUÔN tạo Output trước Input để tránh Deadlock
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                // Đọc Object từ Client
                Request request = (Request) in.readObject();
                System.out.println("[Client " + clientIp + "] Yêu cầu: " + request.getAction());

                // Khởi tạo sẵn response bằng null để tránh lỗi compile
                Response response = null;

                // ==========================================================
                // XỬ LÝ LOGIC ĐĂNG NHẬP
                // ==========================================================
                if ("LOGIN".equals(request.getAction())) {
                    try {
                        String[] loginData = (String[]) request.getPayload();
                        String username = loginData[0];
                        String password = loginData[1];

                        com.bidhub.server.dao.UserDAO userDao = new com.bidhub.server.dao.UserDAO();
                        com.bidhub.server.model.user.User loggedInUser = userDao.authenticate(username, password);

                        if (loggedInUser != null) {
                            String[] userInfo = {loggedInUser.getUsername(), loggedInUser.getEmail()};
                            response = Response.success("Đăng nhập thành công!", userInfo);
                        } else {
                            response = Response.error("Sai tên đăng nhập hoặc mật khẩu!");
                        }
                    } catch (Exception e) {
                        response = Response.error("Lỗi dữ liệu đăng nhập: " + e.getMessage());
                    }
                }

                // ==========================================================
                // XỬ LÝ LOGIC ĐĂNG KÝ
                // ==========================================================
                else if ("REGISTER".equals(request.getAction())) {
                    try {
                        String[] data = (String[]) request.getPayload();
                        String username = data[0];
                        String password = data[1];
                        String email = data[2];
                        String role = data[3];

                        com.bidhub.server.model.user.User newUser;
                        if ("SELLER".equals(role)) {
                            newUser = new com.bidhub.server.model.user.Seller(username, password, email);
                        } else {
                            newUser = new com.bidhub.server.model.user.Bidder(username, password, email);
                        }

                        com.bidhub.server.dao.UserDAO userDao = new com.bidhub.server.dao.UserDAO();
                        userDao.save(newUser);

                        response = Response.success("Đăng ký thành công! Hãy đăng nhập.", null);

                    } catch (Exception e) {
                        e.printStackTrace();
                        response = Response.error("Lỗi khi lưu vào Database: " + e.getMessage());
                    }
                }

                // ==========================================================
                // LẤY DANH SÁCH SẢN PHẨM
                // ==========================================================
                else if ("GET_AUCTIONS".equals(request.getAction())) {
                    try {
                        com.bidhub.server.dao.AuctionDAO auctionDao = new com.bidhub.server.dao.AuctionDAO();
                        // Lấy danh sách từ Database
                        java.util.List<String[]> auctionList = auctionDao.getAuctionListForTable();

                        // Đóng gói gửi về cho Client
                        response = Response.success("Tải dữ liệu thành công", auctionList);
                    } catch (Exception e) {
                        response = Response.error("Lỗi khi tải danh sách: " + e.getMessage());
                    }
                }

                // ==========================================================
                // NẠP TIỀN
                // ==========================================================
                else if ("TOPUP".equals(request.getAction())) {
                    try {
                        // 1. Lấy thông tin Client gửi lên (Mảng gồm: [tên_tài_khoản, số_tiền])
                        Object[] payload = (Object[]) request.getPayload();
                        String username = (String) payload[0];
                        double amount = Double.parseDouble(payload[1].toString());

                        // 2. Gọi DAO để cập nhật Database
                        com.bidhub.server.dao.UserDAO userDao = new com.bidhub.server.dao.UserDAO();
                        boolean success = userDao.updateBalance(username, amount);

                        // 3. Báo cáo kết quả về Client
                        if (success) {
                            response = Response.success("Nạp thành công " + String.format("%,.0f VNĐ", amount), null);
                        } else {
                            response = Response.error("Lỗi: Tài khoản không tồn tại hoặc DB bị khóa!");
                        }
                    } catch (Exception e) {
                        response = Response.error("Sai định dạng gói tin nạp tiền: " + e.getMessage());
                    }
                }

                // ==========================================================
                // LẤY SỐ DƯ
                // ==========================================================
                else if ("GET_BALANCE".equals(request.getAction())) {
                    try {
                        String username = (String) request.getPayload(); // Client chỉ cần gửi tên tài khoản lên

                        com.bidhub.server.dao.UserDAO userDao = new com.bidhub.server.dao.UserDAO();
                        double balance = userDao.getBalance(username);

                        // Đóng gói số tiền gửi về
                        response = Response.success("OK", balance);
                    } catch (Exception e) {
                        response = Response.error("Lỗi lấy số dư: " + e.getMessage());
                    }
                }

                // ==========================================================
                // ĐẶT GIÁ (ĐẤU GIÁ)
                // ==========================================================
                else if ("PLACE_BID".equals(request.getAction())) {
                    try {
                        // 1. Bóc tách dữ liệu Client gửi lên: [Tên_User, Tên_Sản_Phẩm, Số_Tiền]
                        Object[] payload = (Object[]) request.getPayload();
                        String username = (String) payload[0];
                        String itemName = (String) payload[1];
                        double bidAmount = Double.parseDouble(payload[2].toString());

                        // 2. Nhờ DAO xử lý giao dịch
                        com.bidhub.server.dao.AuctionDAO auctionDao = new com.bidhub.server.dao.AuctionDAO();
                        String result = auctionDao.placeBid(username, itemName, bidAmount);

                        // 3. Trả kết quả về
                        if ("SUCCESS".equals(result)) {
                            response = Response.success("Bạn đã dẫn đầu với mức giá " + String.format("%,.0f VNĐ", bidAmount) + "!", null);
                        } else {
                            response = Response.error(result); // Trả về câu báo lỗi cụ thể (Thiếu tiền, Giá thấp...)
                        }
                    } catch (Exception e) {
                        response = Response.error("Sai định dạng gói tin đấu giá: " + e.getMessage());
                    }
                }

                // ==========================================================
                // ĐĂNG BÁN SẢN PHẨM MỚI (HỖ TRỢ LƯU ẢNH VÀ MÔ TẢ)
                // ==========================================================
                else if ("ADD_ITEM".equals(request.getAction())) {
                    try {
                        Object[] payload = (Object[]) request.getPayload();
                        String sellerName = (String) payload[0];
                        String itemName = (String) payload[1];
                        double startingPrice = Double.parseDouble(payload[2].toString());
                        String endTimeStr = (String) payload[3];
                        String description = (String) payload[4]; // Nhận mô tả
                        byte[] imageBytes = (byte[]) payload[5];  // Nhận hạt Byte của ảnh
                        String fileExtension = (String) payload[6]; // Nhận đuôi file (.png, .jpg)

                        // Mặc định nếu người dùng không chọn ảnh
                        String imagePath = "no_image.png";

                        // Nếu có dữ liệu ảnh truyền lên
                        if (imageBytes != null && imageBytes.length > 0) {
                            // Tạo thư mục uploads trên Server (Nằm ở gốc dự án)
                            java.io.File uploadDir = new java.io.File("uploads");
                            if (!uploadDir.exists()) {
                                uploadDir.mkdir();
                            }

                            // Đặt tên file bằng mã ngẫu nhiên UUID để không bao giờ bị trùng
                            String fileName = java.util.UUID.randomUUID().toString() + fileExtension;
                            java.io.File savedFile = new java.io.File(uploadDir, fileName);

                            // Ghi từ mảng Byte ra thành 1 file ảnh thật
                            java.nio.file.Files.write(savedFile.toPath(), imageBytes);

                            // Lấy đường dẫn lưu vào Database
                            imagePath = savedFile.getPath();
                        }

                        com.bidhub.server.dao.AuctionDAO auctionDao = new com.bidhub.server.dao.AuctionDAO();

                        // Gọi DAO truyền đủ 6 thông tin
                        if (auctionDao.addAuctionItem(sellerName, itemName, startingPrice, endTimeStr, description, imagePath)) {
                            response = Response.success("Đăng bán sản phẩm thành công!", null);
                        } else {
                            response = Response.error("Lỗi: Tên sản phẩm đã tồn tại hoặc hệ thống gặp sự cố!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response = Response.error("Dữ liệu gửi lên không hợp lệ!");
                    }
                }

                // ==========================================================
                // CÁC HÀNH ĐỘNG KHÔNG HỢP LỆ
                // ==========================================================
                else {
                    response = Response.error("Hành động không hợp lệ: " + request.getAction());
                }

                // Gửi trả kết quả về cho Client
                if (response != null) {
                    out.writeObject(response);
                    out.flush();
                }
            }

        } catch (EOFException e) {
            System.out.println("[Server] Client " + clientIp + " đã ngắt kết nối an toàn.");
        } catch (Exception e) {
            System.out.println("[Server] Lỗi kết nối với " + clientIp + ": " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}