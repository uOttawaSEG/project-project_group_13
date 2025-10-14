package com.example.otams.data;

import androidx.annotation.NonNull;

import com.example.otams.data.model.LoggedInUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;

public class LoginDataSource {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public CompletableFuture<Result<LoggedInUser>> login(String username, String password) {
        CompletableFuture<Result<LoggedInUser>> future = new CompletableFuture<>();

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            LoggedInUser loggedInUser = new LoggedInUser(user.getUid(), user.getEmail());
                            future.complete(new Result.Success<>(loggedInUser));
                        } else {
                            future.complete(new Result.Error(new Exception("User not found")));
                        }
                    } else {
                        future.complete(new Result.Error(task.getException()));
                    }
                });

        return future;
    }

    public void logout() {
        mAuth.signOut();
    }
}