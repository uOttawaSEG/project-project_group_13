package com.example.otams.data;

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
