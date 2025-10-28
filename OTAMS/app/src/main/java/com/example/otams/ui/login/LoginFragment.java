package com.example.otams.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.otams.R;
import com.example.otams.databinding.FragmentLoginBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // Sign out any existing user when returning to login screen
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        // Initially hide spinner
        loadingProgressBar.setVisibility(View.GONE);

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                NavController navController = Navigation.findNavController(view);

                if (loginResult.getError() != null) {

                    Bundle bundle = new Bundle();
                    bundle.putString("username", username);
                    bundle.putString("password", password);

                    showLoginFailed(loginResult.getError());

                }
                if (loginResult.getSuccess() != null) {
                    // Check user status before navigating
                    checkUserStatusAndNavigate(view, username);
                }
                loginViewModel.clearLoginResult();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString("username", username);

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_loginFragment_to_fragment_signup, bundle);
            }
        });

    }

    private void checkUserStatusAndNavigate(View view, String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showLoginFailed(R.string.login_failed);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        String role = documentSnapshot.getString("role");

                        if ("ACTIVE".equals(status)) {
                            // User is approved - proceed to homepage
                            String uname = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                            Bundle bundle = new Bundle();
                            bundle.putString("username", uname);

                            Toast.makeText(getContext(),
                                    getString(R.string.welcome) + " " + uname,
                                    Toast.LENGTH_SHORT).show();

                            Navigation.findNavController(view)
                                    .navigate(R.id.action_loginFragment_to_fragment_homepage, bundle);

                        } else if ("SUSPENDED".equals(status)) {
                            // User was rejected
                            auth.signOut();
                            Toast.makeText(getContext(),
                                    "Your registration request was rejected.\n\n" +
                                            "If you believe this was a mistake, please contact administration at:\n" +
                                            "Phone: 1-800-OTAMS-ADMIN (1-800-682-6723)\n" +
                                            "Email: admin@otams.com",
                                    Toast.LENGTH_LONG).show();

                        } else if ("PENDING".equals(status)) {
                            // User is waiting for approval
                            auth.signOut();
                            Toast.makeText(getContext(),
                                    "Your registration request is pending administrator approval.\n\n" +
                                            "You will be able to log in once your account has been approved.\n" +
                                            "Please check back later.",
                                    Toast.LENGTH_LONG).show();

                        } else {
                            // Unknown status
                            auth.signOut();
                            Toast.makeText(getContext(),
                                    "Account status unknown. Please contact administration.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // User document not found
                        auth.signOut();
                        Toast.makeText(getContext(),
                                "User account not found. Please register first.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    auth.signOut();
                    Toast.makeText(getContext(),
                            "Error checking account status: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + " "
                + model.getDisplayName().substring(0, model.getDisplayName().indexOf('@'));
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }

        // todo this will be our main page

        // startActivity(new Intent(getContext(), MainActivity.class));
        // requireActivity().finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}