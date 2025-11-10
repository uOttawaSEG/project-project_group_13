package com.example.otams.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;
import com.example.otams.data.UserRole;
import com.example.otams.data.UserStatus;
import com.example.otams.databinding.FragmentHomepageBinding;

public class fragment_homepage extends Fragment {
    private FragmentHomepageBinding binding;
    private FirebaseManager firebaseManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomepageBinding.inflate(inflater, container, false);
        firebaseManager = FirebaseManager.getInstance();
        requireActivity().setTitle("Home");
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Inflate your menu here
                menuInflater.inflate(R.menu.menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    // Handle logout click
                    firebaseManager.signOut();
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.loginFragment);
                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());

        TextView centerText = view.findViewById(R.id.centerText);

        if (firebaseManager.getCurrentUser() == null) {
            centerText.setText(R.string.not_logged_in);
            return;
        }

        firebaseManager.getFirestore().collection("users")
                .document(firebaseManager.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserRole role = UserRole.valueOf(documentSnapshot.getString("role"));
                        UserStatus status = UserStatus.valueOf(documentSnapshot.getString("status"));
                        String email = documentSnapshot.getString("username");

                        if (status == UserStatus.APPROVED) {

                            if (role == UserRole.ADMIN) {
                                NavController navController = Navigation.findNavController(requireView());
                                navController.navigate(R.id.action_fragment_homepage_to_adminFragment);
                            }

                            if (role == UserRole.TUTOR) {
                                NavController navController = Navigation.findNavController(requireView());
                                navController.navigate(R.id.action_fragment_homepage_to_fragment_tutor);
                            }

                            if (role == UserRole.STUDENT) {
                                NavController navController = Navigation.findNavController(requireView());
                                navController.navigate(R.id.action_fragment_homepage_to_fragment_tutor);
                            }
                        }else{
                            binding.centerText.setText(String.format("%s%s%s", getString(R.string.your_account), status, getString(R.string.contact_admin)));
                        }


                    } else {
                        // Document does not exist
                        binding.centerText.setText(R.string.user_data_not_found_contact_admin);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(requireContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                });

    }
}
