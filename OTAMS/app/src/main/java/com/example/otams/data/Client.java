package com.example.otams.data;

public class Client extends User {
    private String First_Name;
    private String Last_Name;
    private String Phone_Number;
    private String Future_Session;
    private String Past_Session;

    public Client(String Email, String Password, String First_Name, String Last_Name, String Phone_Number,
                  String Future_Session, String Past_Session) {
        super(Email, Password);
        this.First_Name = First_Name;
        this.Last_Name = Last_Name;
        this.Phone_Number = Phone_Number;
        this.Future_Session = Future_Session;
        this.Past_Session = Past_Session;
    }

    public Client(String Email, String Password, String First_Name, String Last_Name, String Phone_Number) {
        super(Email, Password);
        this.First_Name = First_Name;
        this.Last_Name = Last_Name;
        this.Phone_Number = Phone_Number;
        this.Future_Session = "";
        this.Past_Session = "";
    }

    public String getFirst_Name() {
        return First_Name;
    }

    public void setFirst_Name(String First_Name) {
        this.First_Name = First_Name;
    }

    public String getLast_Name() {
        return Last_Name;
    }

    public void setLast_Name(String Last_Name) {
        this.Last_Name = Last_Name;
    }

    public String getPhone_Number() {
        return Phone_Number;
    }

    public void setPhone_Number(String Phone_Number) {
        this.Phone_Number = Phone_Number;
    }

    public String getFuture_Session() {
        return Future_Session;
    }

    public void setFuture_Session(String Future_Session) {
        this.Future_Session = Future_Session;
    }

    public String getPast_Session() {
        return Past_Session;
    }

    public void setPast_Session(String Past_Session) {
        this.Past_Session = Past_Session;
    }

    public void accept() {
        //System.out.println("Session accepted.");
    }

    public void reject() {
        //System.out.println("Session rejected.");
    }
}
