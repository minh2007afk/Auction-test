package com.bidhub.server.model.user;

import com.bidhub.server.model.enums.UserRole;

public class Admin extends User {

    public Admin(String username, String passwordHash, String email) {
        super(username, passwordHash, email, UserRole.ADMIN);
    }

    // Hành động đặc thù của Admin: Khóa tài khoản
    public void deactivateUser(User target) {
        if (target instanceof Admin) {
            throw new IllegalArgumentException("Admin không thể khóa Admin khác");
        }
        target.deactivate();
    }

    @Override
    public String getInfo() {
        return super.getInfo() + " [ADMIN V.I.P]";
    }
}