package com.example.otams.data;

public class Student extends Client {

    private String program_of_study;

    public Student(String Email, String Password, String First_Name, String Last_Name, String Phone_Number,
            String Future_Session, String Past_Session, String program_of_study) {
        super(Email, Password, First_Name, Last_Name, Phone_Number, Future_Session, Past_Session);
        this.program_of_study = program_of_study;
        setRole(UserRole.STUDENT);
    }

    public Student(String Email, String Password, String First_Name, String Last_Name, String Phone_Number,
            String program_of_study) {
        super(Email, Password, First_Name, Last_Name, Phone_Number);
        this.program_of_study = program_of_study;
        setRole(UserRole.STUDENT);
    }

    public String getProgram_of_study() {
        return program_of_study;
    }

    public void setProgram_of_study(String program_of_study) {
        this.program_of_study = program_of_study;
    }
}
