package com.bidhub.network;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;
    private String message;
    private Object data;

    public Response(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Response success(String message, Object data) {
        return new Response("SUCCESS", message, data);
    }

    public static Response error(String message) {
        return new Response("ERROR", message, null);
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }

    public boolean isSuccess() { return "SUCCESS".equals(status); }
}