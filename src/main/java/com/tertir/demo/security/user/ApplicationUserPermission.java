package com.tertir.demo.security.user;

public enum ApplicationUserPermission {

    GENERATE_QR("qr:generate"),
    DELETE_QR("qr:delete");

    private final String permission;

    ApplicationUserPermission(String permission){
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
