package com.example.otams.data;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        setupFirestoreCache();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    private void setupFirestoreCache() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setLocalCacheSettings(PersistentCacheSettings.newBuilder().setSizeBytes(1024 * 1024 * 100).build()).build();

        firestore.setFirestoreSettings(settings);
    }

    public FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    public void signOut() {
        if (auth != null) {
            auth.signOut();
        }
    }

    public void signIn(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public Task<Object> getUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }
        String uid = currentUser.getUid();

        return firestore.collection("users").document(uid).get().continueWith(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw task.getException() != null ? task.getException() : new Exception("Failed to get document");
            }
            DocumentSnapshot document = task.getResult();
            if (!document.exists()) {
                throw new Exception("User profile does not exist");
            }
            String role = document.getString("role");
            if ("TUTOR".equalsIgnoreCase(role)) {
                return document.toObject(Tutor.class);
            } else if ("STUDENT".equalsIgnoreCase(role)) {
                return document.toObject(Student.class);
            }
            throw new Exception("Unknown user role");
        });
    }

    public void fetchPastSessions(SessionFetchCallback callback) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("User not logged in"));
            return;
        }

        String tutorId = currentUser.getUid();
        Log.d("FirebaseManager", "Fetching past sessions for tutor: " + tutorId);

        firestore.collection("sessions").whereEqualTo("tutor", tutorId).whereLessThanOrEqualTo("startTime", Timestamp.now()).orderBy("startTime", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("FirebaseManager", "Past sessions query successful, found: " + queryDocumentSnapshots.size() + " documents");
            ArrayList<Session> sessions = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Session session = doc.toObject(Session.class);
                if (session != null) {
                    session.setSessionId(doc.getId());
                    sessions.add(session);
                    Log.d("FirebaseManager", "Added past session: " + doc.getId() + " with start time: " + session.getStartTime());
                } else {
                    Log.d("FirebaseManager", "Failed to convert document to Session object: " + doc.getId());
                }
            }
            callback.onSessionsFetched(sessions);
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error fetching past sessions: " + e.getMessage(), e);
            callback.onError(e);
        });
    }

    public void fetchFutureSessions(SessionFetchCallback callback) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("User not logged in"));
            return;
        }

        String tutorId = currentUser.getUid();
        Log.d("FirebaseManager", "Fetching future sessions for tutor: " + tutorId);

        firestore.collection("sessions").whereEqualTo("tutor", tutorId).whereGreaterThan("startTime", Timestamp.now()).orderBy("startTime", Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d("FirebaseManager", "Future sessions query successful, found: " + queryDocumentSnapshots.size() + " documents");
            ArrayList<Session> sessions = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Session session = doc.toObject(Session.class);
                if (session != null) {
                    session.setSessionId(doc.getId());
                    sessions.add(session);
                    Log.d("FirebaseManager", "Added future session: " + doc.getId() + " with start time: " + session.getStartTime());
                } else {
                    Log.d("FirebaseManager", "Failed to convert document to Session object: " + doc.getId());
                }
            }
            callback.onSessionsFetched(sessions);
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error fetching future sessions: " + e.getMessage(), e);
            callback.onError(e);
        });
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signUp(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public void saveUserProfile(String uid, Object userProfile, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firestore.collection("users").document(uid).set(userProfile).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }


    public ArrayList<Session> getFutureSessions() {
        ArrayList<Session> futureSessions = new ArrayList<>();
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return futureSessions;
        }

        firestore.collection("sessions").whereEqualTo("tutor", currentUser.getUid()).whereGreaterThan("startTime", Timestamp.now()).orderBy("startTime", Query.Direction.ASCENDING).get().addOnSuccessListener(querySnapshot -> {
            futureSessions.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Session session = doc.toObject(Session.class);
                if (session != null) {
                    session.setSessionId(doc.getId());
                    futureSessions.add(session);
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error loading future sessions", e));
        return futureSessions;
    }

    public ArrayList<Session> getPastSessions() {
        ArrayList<Session> pastSessions = new ArrayList<>();
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return pastSessions;
        }

        firestore.collection("sessions").whereEqualTo("tutor", currentUser.getUid()).whereLessThanOrEqualTo("startTime", Timestamp.now()).orderBy("startTime", Query.Direction.DESCENDING).get().addOnSuccessListener(querySnapshot -> {
            pastSessions.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Session session = doc.toObject(Session.class);
                if (session != null) {
                    session.setSessionId(doc.getId());
                    pastSessions.add(session);
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error loading past sessions", e));
        return pastSessions;
    }

    public void acceptStudentToSession(String sessionId, String studentId, StudentStatusCallback callback) {
        Map<String, Object> updates = new HashMap<>();

        // Remove from pending and rejected, add to accepted
        updates.put("acceptedStudents", FieldValue.arrayUnion(studentId));
        updates.put("pendingStudents", FieldValue.arrayRemove(studentId));
        updates.put("rejectedStudents", FieldValue.arrayRemove(studentId));

        firestore.collection("sessions").document(sessionId).update(updates).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseManager", "Student accepted: " + studentId);
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error accepting student: " + e.getMessage(), e);
            callback.onError(e);
        });
    }

    public void rejectStudentFromSession(String sessionId, String studentId, StudentStatusCallback callback) {
        Map<String, Object> updates = new HashMap<>();

        // Remove from pending and accepted, add to rejected
        updates.put("rejectedStudents", FieldValue.arrayUnion(studentId));
        updates.put("pendingStudents", FieldValue.arrayRemove(studentId));
        updates.put("acceptedStudents", FieldValue.arrayRemove(studentId));

        firestore.collection("sessions").document(sessionId).update(updates).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseManager", "Student rejected: " + studentId);
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error rejecting student: " + e.getMessage(), e);
            callback.onError(e);
        });
    }

    public Task<String> getTutorNameTask(String tutorId) {
        if (tutorId == null || tutorId.isEmpty()) {
            return Tasks.forResult("");
        }

        return firestore.collection("users").document(tutorId).get().continueWith(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Tutor tutor = document.toObject(Tutor.class);
                if (tutor != null) {
                    String firstName = tutor.getFirst_name();
                    String lastName = tutor.getLast_name();
                    return formatName(firstName, lastName);
                }
            }
            return "";
        });
    }

    private String formatName(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }

    public void enrollStudentInSession(String sessionId, String studentId, StudentStatusCallback callback) {
        firestore.collection("sessions").document(sessionId).get().addOnSuccessListener(documentSnapshot -> {
            Session session = documentSnapshot.toObject(Session.class);
            if (session != null) {
                Map<String, Object> updates = new HashMap<>();

                if (session.isAutoApprove()) {
                    // Auto-approve: add to accepted, remove from others
                    updates.put("acceptedStudents", FieldValue.arrayUnion(studentId));
                    updates.put("pendingStudents", FieldValue.arrayRemove(studentId));
                    updates.put("rejectedStudents", FieldValue.arrayRemove(studentId));
                } else {
                    // Manual approval: add to pending, remove from others
                    updates.put("pendingStudents", FieldValue.arrayUnion(studentId));
                    updates.put("acceptedStudents", FieldValue.arrayRemove(studentId));
                    updates.put("rejectedStudents", FieldValue.arrayRemove(studentId));
                }

                firestore.collection("sessions").document(sessionId).update(updates).addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseManager", "Student enrolled: " + studentId);
                    callback.onSuccess();
                }).addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error enrolling student: " + e.getMessage(), e);
                    callback.onError(e);
                });
            } else {
                callback.onError(new Exception("Session not found"));
            }
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error getting session: " + e.getMessage(), e);
            callback.onError(e);
        });
    }

    public void moveStudentBetweenStatus(String sessionId, String studentId, String fromStatus, String toStatus, StudentStatusCallback callback) {
        Map<String, Object> updates = new HashMap<>();

        // Remove from current status
        switch (fromStatus) {
            case "pending":
                updates.put("pendingStudents", FieldValue.arrayRemove(studentId));
                break;
            case "accepted":
                updates.put("acceptedStudents", FieldValue.arrayRemove(studentId));
                break;
            case "rejected":
                updates.put("rejectedStudents", FieldValue.arrayRemove(studentId));
                break;
        }

        // Add to new status
        switch (toStatus) {
            case "pending":
                updates.put("pendingStudents", FieldValue.arrayUnion(studentId));
                break;
            case "accepted":
                updates.put("acceptedStudents", FieldValue.arrayUnion(studentId));
                break;
            case "rejected":
                updates.put("rejectedStudents", FieldValue.arrayUnion(studentId));
                break;
        }

        firestore.collection("sessions").document(sessionId).update(updates).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseManager", "Student moved from " + fromStatus + " to " + toStatus);
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error moving student: " + e.getMessage(), e);
            callback.onError(e);
        });
    }

    // Student session management methods
    public void fetchStudentSessions(String studentId, SessionFetchCallback callback) {
        // Get all sessions where student is in any status
        firestore.collection("sessions").whereArrayContainsAny("acceptedStudents", Arrays.asList(studentId)).get().addOnSuccessListener(acceptedSnapshot -> {
            firestore.collection("sessions").whereArrayContainsAny("pendingStudents", Arrays.asList(studentId)).get().addOnSuccessListener(pendingSnapshot -> {
                firestore.collection("sessions").whereArrayContainsAny("rejectedStudents", Arrays.asList(studentId)).get().addOnSuccessListener(rejectedSnapshot -> {
                    List<Session> allSessions = new ArrayList<>();

                    // Combine all sessions
                    allSessions.addAll(processSnapshot(acceptedSnapshot, "accepted", studentId));
                    allSessions.addAll(processSnapshot(pendingSnapshot, "pending", studentId));
                    allSessions.addAll(processSnapshot(rejectedSnapshot, "rejected", studentId));

                    // Sort by date (most recent first)
                    allSessions.sort((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()));

                    callback.onSessionsFetched(allSessions);
                });
            });
        }).addOnFailureListener(callback::onError);
    }

    private List<Session> processSnapshot(QuerySnapshot snapshot, String status, String studentId) {
        List<Session> sessions = new ArrayList<>();
        for (DocumentSnapshot doc : snapshot) {
            Session session = doc.toObject(Session.class);
            if (session != null) {
                session.setSessionId(doc.getId());
                // Store student status in session object for easy access
                sessions.add(session);
            }
        }
        return sessions;
    }


    public void searchSessionsByCourse(String courseCode, String studentId, SessionFetchCallback callback) {
        // Get current time to filter future sessions
        Timestamp now = Timestamp.now();


        // If search query is empty, return empty results
        if (courseCode.isEmpty()) {
            callback.onSessionsFetched(new ArrayList<>());
            return;
        }


        String endPrefix = courseCode + "\uf8ff";

        // Query for sessions where courseCode starts with the search query
        firestore.collection("sessions").whereGreaterThanOrEqualTo("courseCode", courseCode).whereLessThanOrEqualTo("courseCode", endPrefix).whereGreaterThan("startTime", now).get().addOnSuccessListener(querySnapshot -> {
            List<Session> availableSessions = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot) {
                Session session = doc.toObject(Session.class);
                if (session != null) {
                    session.setSessionId(doc.getId());

                    // Only include sessions where student is not enrolled
                    String studentStatus = session.getStudentStatus(studentId);
                    if ("none".equals(studentStatus)) {
                        availableSessions.add(session);
                    }
                }
            }

            // Sort by start time (earliest first)
            availableSessions.sort((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));
            callback.onSessionsFetched(availableSessions);
        }).addOnFailureListener(callback::onError);
    }

    public void requestSession(String sessionId, String studentId, StudentStatusCallback callback) {
        firestore.collection("sessions").document(sessionId).get().addOnSuccessListener(documentSnapshot -> {
            Session session = documentSnapshot.toObject(Session.class);
            if (session != null) {
                Map<String, Object> updates = new HashMap<>();

                if (session.isAutoApprove()) {
                    updates.put("acceptedStudents", FieldValue.arrayUnion(studentId));
                } else {
                    updates.put("pendingStudents", FieldValue.arrayUnion(studentId));
                }

                firestore.collection("sessions").document(sessionId).update(updates).addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseManager", "Session requested successfully");
                    callback.onSuccess();
                }).addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error requesting session", e);
                    callback.onError(e);
                });
            } else {
                callback.onError(new Exception("Session not found"));
            }
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error getting session", e);
            callback.onError(e);
        });
    }

    public void cancelSession(String sessionId, String studentId, StudentStatusCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("acceptedStudents", FieldValue.arrayRemove(studentId));
        updates.put("pendingStudents", FieldValue.arrayRemove(studentId));
        updates.put("cancelledStudents", FieldValue.arrayUnion(studentId));

        firestore.collection("sessions").document(sessionId).update(updates).addOnSuccessListener(aVoid -> {
            Log.d("FirebaseManager", "Session cancelled successfully");
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            Log.e("FirebaseManager", "Error cancelling session", e);
            callback.onError(e);
        });
    }


    public void rateTutor(String tutorId, String studentId, int rating, StudentStatusCallback callback) {
        firestore.collection("users").document(tutorId).get().addOnSuccessListener(documentSnapshot -> {
            Tutor tutor = documentSnapshot.toObject(Tutor.class);
            if (tutor != null) {
                // Update tutor's rating
                Map<String, Object> updates = new HashMap<>();
                updates.put("ratings." + studentId, rating);

                firestore.collection("users").document(tutorId).update(updates).addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseManager", "Tutor rated successfully");
                    callback.onSuccess();
                }).addOnFailureListener(callback::onError);
            } else {
                callback.onError(new Exception("Tutor not found"));
            }
        }).addOnFailureListener(callback::onError);
    }

    public void getTutorWithRating(String tutorId, TutorFetchCallback callback) {
        firestore.collection("users").document(tutorId).get().addOnSuccessListener(documentSnapshot -> {
            Tutor tutor = documentSnapshot.toObject(Tutor.class);
            if (tutor != null) {
                callback.onTutorFetched(tutor);
            } else {
                callback.onError(new Exception("Tutor not found"));
            }
        }).addOnFailureListener(callback::onError);
    }

    public interface TutorFetchCallback {
        void onTutorFetched(Tutor tutor);

        void onError(Exception e);
    }

    public interface SessionFetchCallback {
        void onSessionsFetched(List<Session> sessions);

        void onError(Exception e);
    }

    public interface StudentStatusCallback {
        void onSuccess();

        void onError(Exception e);
    }
}