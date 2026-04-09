package com.bidhub.server;

import com.bidhub.server.database.MigrationRunner;
import com.bidhub.server.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static final int DEFAULT_PORT = 8888;

    public static void main(String[] args) {
        System.out.println("=== BidHub Server đang khởi động ===");

        // 1. Khởi tạo Database
        MigrationRunner.runMigrations();
        System.out.println("BidHub Server đã sẵn sàng Database!");

        // =========================================================
        // BÁC BẢO VỆ: CHẠY NGẦM KIỂM TRA HẾT HẠN MỖI 5 GIÂY
        // =========================================================
        Thread expirationChecker = new Thread(() -> {
            com.bidhub.server.dao.AuctionDAO dao = new com.bidhub.server.dao.AuctionDAO();
            while (true) {
                try {
                    dao.closeExpiredAuctions();
                    Thread.sleep(5000); // Ngủ 5 giây rồi dậy đi tuần tiếp
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        expirationChecker.setDaemon(true); // Đảm bảo thread tự chết khi tắt Server
        expirationChecker.start();
        // =========================================================

        // 2. Mở cổng Socket Lắng nghe
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            System.out.println("🚀 Đang lắng nghe Client kết nối ở cổng " + DEFAULT_PORT + "...");

            // Vòng lặp vô hạn để đón khách mới
            while (true) {
                // Lệnh accept() sẽ "đứng đợi" ở đây cho đến khi có 1 Client nhấc máy gọi tới
                Socket clientSocket = serverSocket.accept();

                // Khi có khách, ném khách đó cho 1 nhân viên (ClientHandler) chạy ở luồng (Thread) mới
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Lỗi sập Server: " + e.getMessage());
        }
    }
}