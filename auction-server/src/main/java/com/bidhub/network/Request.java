package com.bidhub.network;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L; // Mã nhận dạng phiên bản class

    private String action;
    private Object payload; // Đổi String thành Object để sau này gửi thẳng User, Item luôn!

    public Request(String action, Object payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() { return action; }
    public Object getPayload() { return payload; }
}