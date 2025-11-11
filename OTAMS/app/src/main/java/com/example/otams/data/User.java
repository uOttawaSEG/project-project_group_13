package com.example.otams.data;

public class User {
    private String username;
    private String password;
    private UserRole role;
    private UserStatus status;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

}

class Admin extends User {

    public Admin(String username, String password) {
        super(username, password);
        setRole(UserRole.ADMIN);
    }

    public void accept() {
        // System.out.println("Admin " + username + " request accepted.");
    }

    public void reject() {
        // System.out.println("Admin " + username + " request rejected.");
    }
}
