package com.example.otams.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import com.example.otams.R;
import com.example.otams.data.UserRole;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class fragment_homepage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_homepage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView centerText = view.findViewById(R.id.centerText);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            centerText.setText(R.string.not_logged_in);
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Debug: Log the user ID
        android.util.Log.d("Homepage", "Current user ID: " + userId);

        // Check user role from users collection
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    android.util.Log.d("Homepage", "Document exists: " + documentSnapshot.exists());
                    if (documentSnapshot.exists()) {
                        String roles = documentSnapshot.getString("role");
                        String status = documentSnapshot.getString("status");
                        String email = documentSnapshot.getString("username");
                        if (email == null) {
                            email = documentSnapshot.getString("email");
                        }

                        // Check if user is approved
                        if (!"ACTIVE".equals(status)) {
                            centerText.setText("Your account is " + status + ". Please contact admin. 11111111");
                            return;
                        }
                        UserRole role;
                        try {
                            role = UserRole.valueOf(roles);
                        } catch (IllegalArgumentException | NullPointerException e) {
                            centerText.setText(R.string.your_role_could_not_be_determined);
                            return;
                        }
                        if (role != null) {
                            switch (role) {
                                case ADMIN:
                                    // Navigate to admin fragment
                                    androidx.navigation.Navigation.findNavController(view)
                                            .navigate(R.id.action_fragment_homepage_to_adminFragment);
                                    break;
                                case STUDENT:
                                    centerText.setText(String.format("%s %s",
                                            getString(R.string.you_belong_to_the_student_collection), email));
                                    break;
                                case TUTOR:
                                    centerText.setText(String.format("%s %s",
                                            getString(R.string.you_belong_to_the_tutor_collection), email));
                                    break;
                                default:
                                    centerText.setText(R.string.your_role_could_not_be_determined);
                                    break;
                            }
                        } else {
                            centerText.setText(R.string.your_role_could_not_be_determined);
                        }
                    } else {
                        android.util.Log.w("Homepage", "User document not found for ID: " + userId);
                        centerText.setText(R.string.your_role_could_not_be_determined);
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("Homepage", "Error fetching user document", e);
                    centerText.setText(String.format("Error checking user role: %s", e.getMessage()));
                });

    }
}
