package com.example.otams.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Session implements Parcelable {
    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
    private boolean isCancelled;
    private String sessionId;
    private String courseCode;
    private String location;
    private Timestamp startTime;
    private Timestamp endTime;
    private String tutor;
    private boolean autoApprove;
    // Separate lists for different statuses
    private List<String> pendingStudents;  // Students who requested enrollment
    private List<String> acceptedStudents; // Students who are accepted
    private List<String> rejectedStudents; // Students who are rejected
    private List<String> cancelledStudents;// Students who are cancelled


    // Empty constructor
    public Session() {
        this.pendingStudents = new ArrayList<>();
        this.acceptedStudents = new ArrayList<>();
        this.rejectedStudents = new ArrayList<>();
        this.cancelledStudents = new ArrayList<>();
        this.isCancelled = false;
    }

    // Constructor
    public Session(String courseCode, boolean autoApprove, String location, Timestamp startTime, Timestamp endTime, String tutor) {
        this.courseCode = courseCode;
        this.autoApprove = autoApprove;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutor = tutor;
        this.pendingStudents = new ArrayList<>();
        this.acceptedStudents = new ArrayList<>();
        this.rejectedStudents = new ArrayList<>();
        this.cancelledStudents = new ArrayList<>();
    }

    protected Session(Parcel in) {
        sessionId = in.readString();
        courseCode = in.readString();
        location = in.readString();
        startTime = in.readParcelable(Timestamp.class.getClassLoader());
        endTime = in.readParcelable(Timestamp.class.getClassLoader());
        tutor = in.readString();
        autoApprove = in.readByte() != 0;
        pendingStudents = in.createStringArrayList();
        acceptedStudents = in.createStringArrayList();
        rejectedStudents = in.createStringArrayList();
        isCancelled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sessionId);
        dest.writeString(courseCode);
        dest.writeString(location);
        dest.writeParcelable(startTime, flags);
        dest.writeParcelable(endTime, flags);
        dest.writeString(tutor);
        dest.writeByte((byte) (autoApprove ? 1 : 0));
        dest.writeStringList(pendingStudents);
        dest.writeStringList(acceptedStudents);
        dest.writeStringList(rejectedStudents);
        dest.writeByte((byte) (isCancelled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<String> getCancelledStudents() {
        return cancelledStudents;
    }

    public void setCancelledStudents(List<String> cancelledStudents) {
        this.cancelledStudents = cancelledStudents;
    }

    // Getters and setters
    public List<String> getPendingStudents() {
        return pendingStudents;
    }


    public void setPendingStudents(List<String> pendingStudents) {
        this.pendingStudents = pendingStudents;
    }

    public List<String> getAcceptedStudents() {
        return acceptedStudents;
    }

    public void setAcceptedStudents(List<String> acceptedStudents) {
        this.acceptedStudents = acceptedStudents;
    }

    public List<String> getRejectedStudents() {
        return rejectedStudents;
    }

    public void setRejectedStudents(List<String> rejectedStudents) {
        this.rejectedStudents = rejectedStudents;
    }

    // Helper methods
    public void addPendingStudent(String studentId) {
        if (!pendingStudents.contains(studentId)) {
            pendingStudents.add(studentId);
        }
    }

    public void acceptStudent(String studentId) {
        if (pendingStudents.contains(studentId)) {
            pendingStudents.remove(studentId);
        }
        if (rejectedStudents.contains(studentId)) {
            rejectedStudents.remove(studentId);
        }
        if (!acceptedStudents.contains(studentId)) {
            acceptedStudents.add(studentId);
        }
    }

    public void rejectStudent(String studentId) {
        if (pendingStudents.contains(studentId)) {
            pendingStudents.remove(studentId);
        }
        if (acceptedStudents.contains(studentId)) {
            acceptedStudents.remove(studentId);
        }
        if (!rejectedStudents.contains(studentId)) {
            rejectedStudents.add(studentId);
        }
    }

    // Get all enrolled students (accepted only)
    public ArrayList<String> getEnrolledStudents() {
        return new ArrayList<>(acceptedStudents);
    }

    // Check if student is enrolled


    // Check if student is pending


    // Auto-approve logic
    public void processAutoApprove(String studentId) {
        if (autoApprove) {
            acceptStudent(studentId);
        } else {
            addPendingStudent(studentId);
        }
    }

    public String getSessionId() {
        return sessionId;

    }

    public void setSessionId(String id) {
        this.sessionId = id;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTutor() {
        return tutor;
    }

    public void setTutor(String tutor) {
        this.tutor = tutor;
    }

    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    @Exclude
    public String getStudentStatus(String studentId) {
        if (acceptedStudents.contains(studentId)) return "accepted";
        if (pendingStudents.contains(studentId)) return "pending";
        if (rejectedStudents.contains(studentId)) return "rejected";
        if (cancelledStudents.contains(studentId)) return "cancelled";
        return "none";
    }

    @Exclude
    public boolean isStudentEnrolled(String studentId) {
        return acceptedStudents.contains(studentId);
    }

    @Exclude
    public boolean isStudentPending(String studentId) {
        return pendingStudents.contains(studentId);
    }

    @Exclude
    public boolean canStudentCancel(String studentId) {
        if (!acceptedStudents.contains(studentId) && !pendingStudents.contains(studentId)) {
            return false;
        }

        // Check if session starts within 24 hours
        long now = System.currentTimeMillis();
        long sessionStart = startTime.toDate().getTime();
        long twentyFourHours = 24 * 60 * 60 * 1000;

        return sessionStart - now > twentyFourHours;
    }

    @Exclude
    public boolean canTutorCancel(String tutorId) {
        // Check if this tutor owns the session
        if (!getTutor().equals(tutorId)) {
            return false;
        }

        // Check if session has already started
        Timestamp now = Timestamp.now();
        if (getStartTime().compareTo(now) <= 0) {
            return false;
        }

        // Tutors can cancel their sessions if no students are enrolled
        return getAcceptedStudents().isEmpty();
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}

