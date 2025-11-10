package com.example.otams.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.otams.data.FirebaseManager;
import com.example.otams.databinding.FragmentStudentBinding;


public class fragment_student extends Fragment {

    private FirebaseManager firebaseManager;
    private FragmentStudentBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        binding = FragmentStudentBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Tutor");
        firebaseManager = FirebaseManager.getInstance();

        return binding.getRoot();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // assuming you're using ViewBinding
    }

}