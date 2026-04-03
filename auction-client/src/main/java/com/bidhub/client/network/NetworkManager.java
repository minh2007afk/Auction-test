package com.bidhub.client.network;

import com.bidhub.network.Request;
import com.bidhub.network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkManager {

    private static NetworkManager instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void connect(String serverIp, int port) {
        try {
            socket = new Socket(serverIp, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("[Network] Đã kết nối Java thuần tới Server!");
        } catch (IOException e) {
            System.err.println("[Network] Lỗi kết nối!");
        }
    }

    public Response sendRequest(Request request) {
        if (out == null || in == null) {
            return Response.error("Chưa kết nối tới Server!");
        }
        try {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("Lỗi mạng: " + e.getMessage());
        }
    }
}