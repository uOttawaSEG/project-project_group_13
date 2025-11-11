package com.example.otams.data;

import com.google.firebase.Timestamp;

import java.util.ArrayList;


public class Session {

    private boolean auto_approve;
    private String location;
    private String course_code;
    private Timestamp start_time;
    private Timestamp end_time;
    private String tutor_id;
    private ArrayList<String> students;

    private String session_id;

    public Session(String course_code, boolean auto_approve, String location, Timestamp start_time, Timestamp end_time, String tutorID) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.tutor_id = tutorID;
        this.course_code = course_code;
        this.auto_approve = auto_approve;
        this.location = location;
        this.students = new ArrayList<>();
    }

    public Session() {
        // default Constructor
        this.students = new ArrayList<>();
    }

    public Session(String tutorID) {
        this.tutor_id = tutorID;
    }

    public String getSessionId() {
        return session_id;
    }

    public String setSessionId(String session_id) {
        return this.session_id = session_id;
    }

    public boolean isAutoApprove() {
        return auto_approve;
    }

    public void setAutoApprove(boolean auto_approve) {
        this.auto_approve = auto_approve;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCourseCode() {
        return course_code;
    }

    public void setCourseCode(String course_code) {
        this.course_code = course_code;
    }

    public Timestamp getStartTime() {
        return start_time;
    }

    public void setStartTime(Timestamp startTime) {
        this.start_time = startTime;
    }

    public Timestamp getEndTime() {
        return end_time;
    }

    public void setEndTime(Timestamp endTime) {
        this.end_time = endTime;
    }

    public String getTutor() {
        return tutor_id;
    }

    public void setTutor(String tutor) {
        this.tutor_id = tutor;
    }

    public ArrayList<String> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<String> students) {
        this.students = students;
    }

    public void addStudent(String student) {
        if (this.students == null) {
            this.students = new ArrayList<>();
        }
        this.students.add(student);
    }

    public void removeStudent(String student) {
        if (this.students != null) {
            this.students.remove(student);
        }
    }

}
