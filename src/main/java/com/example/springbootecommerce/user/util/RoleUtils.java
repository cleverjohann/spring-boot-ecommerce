package com.example.springbootecommerce.user.util;

public class RoleUtils {
    public static final String ROLE_PREFIX = "ROLE_";

    public static String withPrefix(String roleName) {
        if (roleName == null) return null;
        return roleName.startsWith(ROLE_PREFIX) ? roleName : ROLE_PREFIX + roleName;
    }
}
