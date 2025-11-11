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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
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
        firestore.collection("sessions")
                .whereEqualTo("tutor", getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Session> sessions = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Session session = doc.toObject(Session.class);
                        if (session != null && session.getStartTime() != null &&
                                session.getStartTime().compareTo(Timestamp.now()) < 0) {
                            sessions.add(session);
                        }
                    }
                    callback.onSessionsFetched(sessions);
                })
                .addOnFailureListener(callback::onError);
    }

    public void fetchFutureSessions(SessionFetchCallback callback) {
        firestore.collection("sessions")
                .whereEqualTo("tutor", getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Session> sessions = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Session session = doc.toObject(Session.class);
                        if (session != null && session.getStartTime() != null &&
                                session.getStartTime().compareTo(Timestamp.now()) > 0) {
                            sessions.add(session);
                        }
                    }
                    callback.onSessionsFetched(sessions);
                })
                .addOnFailureListener(callback::onError);
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
        firestore.collection("sessions").whereGreaterThan("start_time", Timestamp.now()).orderBy("start_time", Query.Direction.ASCENDING).get().addOnSuccessListener(querySnapshot -> {

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Session session = doc.toObject(Session.class);
                if (session != null) {
                    futureSessions.add(session);
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error loading sessions", e));
        return futureSessions;
    }

    public ArrayList<Session> getPastSessions() {
        ArrayList<Session> pastSessions = new ArrayList<>();
        firestore.collection("sessions").whereLessThanOrEqualTo("start_time", Timestamp.now()).orderBy("start_time", Query.Direction.ASCENDING).get().addOnSuccessListener(querySnapshot -> {

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Session session = doc.toObject(Session.class);
                if (session != null) {
                    pastSessions.add(session);
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error loading sessions", e));
        return pastSessions;
    }

    public interface SessionFetchCallback {
        void onSessionsFetched(List<Session> sessions);

        void onError(Exception e);
    }
}
