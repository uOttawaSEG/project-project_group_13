package com.example.otams;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    private void hashPassword() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(this.password.getBytes());

            // Convert byte array into hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            this.password = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }

    }
}

class Admin extends User {

    public Admin(String username, String password) {
        super(username, password);
    }

    public void accept() {
        //System.out.println("Admin " + username + " request accepted.");
    }

    public void reject() {
        //System.out.println("Admin " + username + " request rejected.");
    }
}
