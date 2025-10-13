package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.RegistertextView).setOnClickListener(v-> {
            Intent intent =  new Intent(RegisterActivity.this,LoginActivity.class);
            startActivity(intent);
        });
        // the spinner will hide the widgets program of study, highest degree and courses offered and only display them when the user toggles the role
        Spinner rolespinner = findViewById(R.id.registerSpinner);
        EditText programOfStudyEditText = findViewById(R.id.programOfStudyEditText);
        EditText highestDegreeEditText = findViewById(R.id.highestDegreeEditText);
        EditText coursesOfferedEditText = findViewById(R.id.coursesOfferedEditText);
        programOfStudyEditText.setVisibility(View.GONE);
        highestDegreeEditText.setVisibility(View.GONE);
        coursesOfferedEditText.setVisibility(View.GONE);
        rolespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedROLE = parent.getItemAtPosition(position).toString();
                if (selectedROLE.equals("Student")) {
                    programOfStudyEditText.setVisibility(View.VISIBLE);
                    highestDegreeEditText.setVisibility(View.GONE);
                    coursesOfferedEditText.setVisibility(View.GONE);
                } else if (selectedROLE.equals("Tutor")) {
                    programOfStudyEditText.setVisibility(View.GONE);
                    highestDegreeEditText.setVisibility(View.VISIBLE);
                    coursesOfferedEditText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing
            }
        })
    ;}
}