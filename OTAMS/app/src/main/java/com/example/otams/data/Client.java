package com.example.otams.data;

import com.google.firebase.Timestamp;

public class Client extends User {
    private String first_name;
    private String last_name;
    private String phone_number;
    private Timestamp future_session;
    private Timestamp past_session;

    public Client(String email, String password, String first_name, String last_name, String phone_number, Timestamp future_session, Timestamp past_session) {
        super(email, password);
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.future_session = future_session;
        this.past_session = past_session;
    }

    public Client(String email, String password, String first_name, String last_name, String phone_number) {
        super(email, password);
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.future_session = null;
        this.past_session = null;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Timestamp getFuture_session() {
        return future_session;
    }

    public void setFuture_session(Timestamp future_session) {
        this.future_session = future_session;
    }

    public Timestamp getPast_session() {
        return past_session;
    }

    public void setPast_session(Timestamp past_session) {
        this.past_session = past_session;
    }

    public void accept() {
        // System.out.println("Session accepted.");
    }

    public void reject() {
        // System.out.println("Session rejected.");
    }
}
