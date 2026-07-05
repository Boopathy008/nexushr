package com.nexushr.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String storedHash = "$2a$12$xg42yMmxu6Nj535mYXner.QMaF3954xxtKouFV1pYRni3QhrlHUyq";
        String password = "Admin@1234";

        boolean matches = encoder.matches(password, storedHash);
        System.out.println("VERIFY:" + matches);

        // Generate a fresh hash too
        String newHash = encoder.encode(password);
        System.out.println("FRESH_HASH:" + newHash);
    }
}
