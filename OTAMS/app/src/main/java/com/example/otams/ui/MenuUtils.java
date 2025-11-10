package com.example.otams.ui;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;

public class MenuUtils {
    public static void setupLogoutMenu(Fragment fragment, FirebaseManager firebaseManager) {
        fragment.requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    firebaseManager.signOut();
                    NavController navController = Navigation.findNavController(fragment.requireView());
                    navController.navigate(R.id.loginFragment);
                    Toast.makeText(fragment.requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        }, fragment.getViewLifecycleOwner());
    }
}