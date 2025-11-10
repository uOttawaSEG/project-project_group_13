package com.example.otams;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up navigation host and controller
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        // AppBarConfiguration: define which fragments have no back button
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.loginFragment).build();

        // Connect toolbar with NavController and AppBarConfiguration
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        Log.d("MainActivity", "NavController setup complete");
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = NavHostFragment.findNavController(Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)));
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
