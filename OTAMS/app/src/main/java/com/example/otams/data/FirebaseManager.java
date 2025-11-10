package com.example.otams.data;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signUp(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    /**
     * Save a user profile (Student, Tutor, etc.) to Firestore.
     * @param uid The Firebase Auth UID.
     * @param userProfile The user profile object to save.
     * @param onSuccess Callback on success.
     * @param onFailure Callback on failure with Exception.
     */
    public void saveUserProfile(String uid, Object userProfile,
                                OnSuccessListener<Void> onSuccess,
                                OnFailureListener onFailure) {
        firestore.collection("users")
                .document(uid)
                .set(userProfile)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
