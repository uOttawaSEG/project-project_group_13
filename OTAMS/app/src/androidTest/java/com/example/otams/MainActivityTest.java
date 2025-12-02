package com.example.otams;

import static org.junit.Assert.assertNotNull;

import android.widget.EditText;
import android.widget.Button;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private EditText username;
    private EditText password;
    private Button login;

    @Before
    public void setUp() {
        activityRule.getScenario().onActivity(activity -> {
            username = activity.findViewById(R.id.username);
            password = activity.findViewById(R.id.password);
            login = activity.findViewById(R.id.login);
        });
    }

    @Test
    public void checkInputs() {
        assertNotNull(username);
        assertNotNull(password);
        assertNotNull(login);
    }
}
