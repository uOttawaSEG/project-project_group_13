package com.example.otams.data;

import androidx.annotation.NonNull;

import com.example.otams.data.model.LoggedInUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class RegisterDataSource {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CompletableFuture<Result<LoggedInUser>> register(String email, String password, User userProfile) {
        CompletableFuture<Result<LoggedInUser>> future = new CompletableFuture<>();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Determine role and collection

                            String collection;
                            if (userProfile.getRole() == UserRole.STUDENT) {

                                collection = "students";
                            } else if (userProfile.getRole() == UserRole.TUTOR) {

                                collection = "tutors";
                            } else {
                                future.complete(new Result.Error(new Exception("Invalid user profile type")));
                                return;
                            }

                            // First store user profile with role in main users collection
                            db.collection("users")
                                    .document(uid)
                                    .set(java.util.Map.of(
                                            "role", userProfile.getRole().toString(),
                                            "email", email,
                                            "uid", uid))
                                    .addOnFailureListener(e -> {
                                        future.complete(new Result.Error(
                                                new Exception("Failed to create user record: " + e.getMessage())));
                                    });

                            // Then store detailed profile in role-specific collection
                            db.collection(collection)
                                    .document(uid)
                                    .set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        LoggedInUser loggedInUser = new LoggedInUser(uid, email);
                                        future.complete(new Result.Success<>(loggedInUser));
                                    })
                                    .addOnFailureListener(e -> {
                                        future.complete(new Result.Error(new Exception("Registration succeeded but profile save failed: " + e.getMessage())));
                                    });
                        } else {
                            future.complete(new Result.Error(
                                    new Exception("User creation succeeded but FirebaseUser is null")));
                        }
                    } else {
                        future.complete(new Result.Error(task.getException()));
                    }
                });

        return future;
    }
}
