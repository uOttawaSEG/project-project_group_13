package com.example.otams.data;

public class Student extends Client {

    private String Program_Of_Study;

    public Student(String Email, String Password, String First_Name, String Last_Name, String Phone_Number,
            String Future_Session, String Past_Session, String Program_Of_Study) {
        super(Email, Password, First_Name, Last_Name, Phone_Number, Future_Session, Past_Session);
        this.Program_Of_Study = Program_Of_Study;
        setRole(UserRole.STUDENT);
    }

    public Student(String Email, String Password, String First_Name, String Last_Name, String Phone_Number,
            String Program_Of_Study) {
        super(Email, Password, First_Name, Last_Name, Phone_Number);
        this.Program_Of_Study = Program_Of_Study;
        setRole(UserRole.STUDENT);
    }

    public String getProgram_Of_Study() {
        return Program_Of_Study;
    }

    public void setProgram_Of_Study(String Program_Of_Study) {
        this.Program_Of_Study = Program_Of_Study;
    }
}
