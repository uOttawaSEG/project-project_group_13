package com.example.otams;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.otams.databinding.FragmentLoginBinding;

public class MainActivity extends AppCompatActivity {

    private FragmentLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = FragmentLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        start();
    }

    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void start() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername();
                updateButtonState();

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateUsername();
                updateButtonState();
            }
        };
        binding.username.addTextChangedListener(watcher);
        binding.password.addTextChangedListener(watcher);
    }

    private void updateButtonState() {
        boolean enabled = !binding.username.getText().toString().trim().isEmpty()
                && !binding.password.getText().toString().trim().isEmpty();
        binding.login.setEnabled(enabled);
    }

    private void validateUsername() {
        String u = binding.username.getText().toString().trim();
        if (u.isEmpty()) {
            binding.username.setError(null);
            return;
        }
        if (!isValidUsername(u)) {
            binding.username.setError("Invalid username");
        } else {
            binding.username.setError(null);
        }
    }

    private boolean isValidUsername(String u) {
        if (u.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(u).matches();
        } else {
            return u.length() >= 3 && u.matches("[A-Za-z0-9._-]+");
        }
    }

}