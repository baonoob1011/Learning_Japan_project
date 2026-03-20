package com.example.learningApp.common;

import java.util.List;

public final class RoleConstants {

    private RoleConstants() {}

    public static final String USER = "USER";
    public static final String USER_VIP = "USER_VIP";
    public static final String ADMIN = "ADMIN";

    public static final List<String> DEFAULT_ROLES =
            List.of(USER, USER_VIP, ADMIN);
}

