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

        binding.login.setOnClickListener(v -> {
            validateUsername();
            validatePassword();
            updateButtonState();

            String u = binding.username.getText().toString().trim();
            String p = binding.password.getText().toString().trim();
            if (!isValidUsername(u) || !isValidPassword(p)) {
                // keep focus on the first invalid field
                if (!isValidUsername(u))
                    binding.username.requestFocus();
                else
                    binding.password.requestFocus();
                return;
            }

            // proceed with login

            binding.login.setEnabled(false);
            binding.login.setText(R.string.authenticating);
        });

    }

    private void updateButtonState() {
        boolean enabled = !binding.username.getText().toString().trim().isEmpty()
                && !binding.password.getText().toString().trim().isEmpty();
        binding.login.setEnabled(enabled);
    }

    // Generated from GEMINI LLM
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

    private void validatePassword() {
        String p = binding.password.getText().toString().trim();
        if (p.isEmpty()) {
            binding.password.setError(null);
            return;
        }
        if (!isValidPassword(p)) {
            binding.password.setError("Password must be at least 8 characters long");
        } else {
            binding.password.setError(null);
        }
    }

    // Generated from GEMINI LLM
    private boolean isValidUsername(String u) {
        u = u.trim();
        if (u.isEmpty())
            return false;

        if (u.contains("@")) {
            // try Android's EMAIL pattern first, fallback to a permissive regex
            if (Patterns.EMAIL_ADDRESS.matcher(u).matches())
                return true;
            String fallback = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            return u.matches(fallback);
        }
        return false;

    }

    private boolean isValidPassword(String p) {
        // additional checks can be added here
        return p.length() >= 8;
    }

}