package com.example.otams.data;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Session {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Tutor tutor;
    private ArrayList<Student> students;

    public Session(LocalDateTime startTime, LocalDateTime endTime, Tutor tutor) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutor = tutor;
    }

    public Session() {
        // default Constructor
    }

    public Session(Tutor tutor) {
        this.tutor = tutor;
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
