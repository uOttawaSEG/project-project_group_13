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



                            // Then store detailed profile in role-specific collection
                            db.collection("users")
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
