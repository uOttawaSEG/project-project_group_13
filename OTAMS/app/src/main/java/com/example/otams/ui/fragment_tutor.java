package com.example.otams.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.otams.data.FirebaseManager;
import com.example.otams.data.MenuUtils;
import com.example.otams.databinding.FragmentTutorBinding;


public class fragment_tutor extends Fragment {
    private FirebaseManager firebaseManager;
    private FragmentTutorBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentTutorBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Tutor");
        firebaseManager = FirebaseManager.getInstance();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MenuUtils.setupLogoutMenu(this, firebaseManager);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // assuming you're using ViewBinding
    }


}