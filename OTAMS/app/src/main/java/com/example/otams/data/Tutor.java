package com.example.otams.data;

public class Tutor extends Client {

    private String Highest_Degree;
    private String Courses_Offered;

    public Tutor(String Email, String Password, String First_Name, String Last_Name, String Phone_Number,
            String Future_Session, String Past_Session, String Highest_Degree, String Courses_Offered) {
        super(Email, Password, First_Name, Last_Name, Phone_Number, Future_Session, Past_Session);

        this.Highest_Degree = Highest_Degree;
        this.Courses_Offered = Courses_Offered;
        setRole(UserRole.TUTOR);
    }

    public Tutor(String Email, String Password, String First_Name, String Last_Name, String Phone_Number,
            String Highest_Degree, String Courses_Offered) {
        super(Email, Password, First_Name, Last_Name, Phone_Number);
        this.Highest_Degree = Highest_Degree;
        this.Courses_Offered = Courses_Offered;
        setRole(UserRole.TUTOR);
    }

    public String getHighest_Degree() {
        return Highest_Degree;
    }

    public void setHighest_Degree(String Highest_Degree) {
        this.Highest_Degree = Highest_Degree;
    }

    public String getCourses_Offered() {
        return Courses_Offered;
    }

    public void setCourses_Offered(String Courses_Offered) {
        this.Courses_Offered = Courses_Offered;

    }
}
