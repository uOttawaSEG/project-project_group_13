package com.example.otams.data;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Session {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String tutorID;
    private ArrayList<String> studentID;

    public Session(LocalDateTime startTime, LocalDateTime endTime, String tutorID) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutorID = tutorID;
    }

    public Session() {
        //default Constructor
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTutorID() {
        return tutorID;
    }

    public void setTutorID(String tutorID) {
        this.tutorID = tutorID;
    }

    public ArrayList<String> getStudentID() {
        return studentID;
    }

    public void setStudentID(ArrayList<String> studentID) {
        this.studentID = studentID;
    }

    public void addStudent(String studentID) {
        if (this.studentID == null) {
            this.studentID = new ArrayList<>();
        }
        this.studentID.add(studentID);
    }

    public void removeStudent(String studentID) {
        if (this.studentID != null) {
            this.studentID.remove(studentID);
        }
    }


}
