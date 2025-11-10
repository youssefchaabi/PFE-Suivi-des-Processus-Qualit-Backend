package com.pfe.qualite.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public PasswordHasher() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public static void main(String[] args) {
        PasswordHasher hasher = new PasswordHasher();

        String password = "admin123";

        String hashed = hasher.hashPassword(password);
        System.out.println(password + " : " + " --> " + hashed);
    }
}
