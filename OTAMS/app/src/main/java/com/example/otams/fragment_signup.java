package com.example.otams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class fragment_signup extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioGroup roleGroup = view.findViewById(R.id.role_group);
        LinearLayout studentFields = view.findViewById(R.id.student_fields);
        LinearLayout tutorFields = view.findViewById(R.id.tutor_fields);

        // Default state: Student selected
        studentFields.setVisibility(View.VISIBLE);
        tutorFields.setVisibility(View.GONE);

        // Listen for role selection changes
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_student) {
                studentFields.setVisibility(View.VISIBLE);
                tutorFields.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_tutor) {
                studentFields.setVisibility(View.GONE);
                tutorFields.setVisibility(View.VISIBLE);
            }
        });
    }
}
