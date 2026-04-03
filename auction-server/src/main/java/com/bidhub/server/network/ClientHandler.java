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

                Response response;

                // Xử lý logic
                if ("LOGIN".equals(request.getAction())) {
                    response = Response.success("Đăng nhập thành công (Java thuần)!", request.getPayload());
                }
                else if ("REGISTER".equals(request.getAction())) {
                    try {
                        // 1. Lấy mảng dữ liệu Client gửi lên
                        String[] data = (String[]) request.getPayload();
                        String username = data[0];
                        String password = data[1];
                        String email = data[2];
                        String role = data[3];

                        // 2. Tạo đối tượng User tương ứng
                        com.bidhub.server.model.user.User newUser;
                        if ("SELLER".equals(role)) {
                            newUser = new com.bidhub.server.model.user.Seller(username, password, email);
                        } else {
                            newUser = new com.bidhub.server.model.user.Bidder(username, password, email);
                        }

                        // 3. Gọi DAO để lưu vào Database SQLite
                        com.bidhub.server.dao.UserDAO userDao = new com.bidhub.server.dao.UserDAO();
                        userDao.save(newUser);

                        // 4. Báo thành công về cho Client
                        response = Response.success("Đăng ký thành công! Hãy đăng nhập.", null);

                    } catch (Exception e) {
                        e.printStackTrace();
                        response = Response.error("Lỗi khi lưu vào Database: " + e.getMessage());
                    }
                }
                else {
                    response = Response.error("Hành động không hợp lệ");
                }

                // Gửi Object về cho Client
                out.writeObject(response);
                out.flush();
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