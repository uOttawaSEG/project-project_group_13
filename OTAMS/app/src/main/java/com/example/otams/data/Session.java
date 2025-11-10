package com.example.otams.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import com.google.firebase.Timestamp;


public class Session {

    private String course_code;
    private Timestamp  start_time;
    private Timestamp  end_time;
    private Tutor tutor;
    private ArrayList<Student> students;

    public Session(String course_code, Timestamp  start_time, Timestamp  end_time, Tutor tutor) {
        this.start_time = start_time;
        this.end_time = end_time;
        this.tutor = tutor;
        this.course_code = course_code;
    }

    public Session() {
        // default Constructor
    }

    public Session(Tutor tutor) {
        this.tutor = tutor;
    }

    public String getCourseCode() {
        return course_code;
    }

    public void setCourseCode(String course_code) {
        this.course_code = course_code;
    }

    public Timestamp  getStartTime() {
        return start_time;
    }

    public void setStartTime(Timestamp  startTime) {
        this.start_time = startTime;
    }

    public Timestamp  getEndTime() {
        return end_time;
    }

    public void setEndTime(Timestamp  endTime) {
        this.end_time = endTime;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }

    public void addStudent(Student student) {
        if (this.students == null) {
            this.students = new ArrayList<>();
        }
        this.students.add(student);
    }

    public void removeStudent(Student student) {
        if (this.students != null) {
            this.students.remove(student);
        }
    }

}
