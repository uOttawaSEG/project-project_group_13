package com.example.otams.data;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Tutor extends Client {

    private String highest_degree;
    private String Courses_Offered;

    public Tutor(String Email, String Password, String First_Name, String Last_Name, String Phone_Number, ArrayList<String> Future_Session, ArrayList<String> Past_Session, String highest_degree, String Courses_Offered) {
        super(Email, Password, First_Name, Last_Name, Phone_Number, Future_Session, Past_Session);

        this.highest_degree = highest_degree;
        this.Courses_Offered = Courses_Offered;
        setRole(UserRole.TUTOR);
    }

    public Tutor(String Email, String Password, String First_Name, String Last_Name, String Phone_Number, String highest_degree, String Courses_Offered) {
        super(Email, Password, First_Name, Last_Name, Phone_Number);
        this.highest_degree = highest_degree;
        this.Courses_Offered = Courses_Offered;
        setRole(UserRole.TUTOR);
    }
    public Tutor() {
        super();
    }
    public String getHighest_degree() {
        return highest_degree;
    }

    public void setHighest_degree(String highest_degree) {
        this.highest_degree = highest_degree;
    }

    public String getCourses_Offered() {
        return Courses_Offered;
    }

    public void setCourses_Offered(String Courses_Offered) {
        this.Courses_Offered = Courses_Offered;

    }
}
