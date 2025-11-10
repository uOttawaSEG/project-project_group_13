package com.example.otams;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.otams_nav, true)  // replace with your root nav_graph ID
                .build();

        navController.navigate(R.id.loginFragment, null, navOptions);
    }
}
