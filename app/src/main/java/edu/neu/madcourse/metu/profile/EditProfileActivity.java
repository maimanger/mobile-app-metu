package edu.neu.madcourse.metu.profile;

//package com.example.handyopinion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import edu.neu.madcourse.metu.MainActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.profile.imageUpload.UploadActivity;
import edu.neu.madcourse.metu.service.FirebaseService;

public class EditProfileActivity extends AppCompatActivity {

    EditText etUsername, etEmail, etLocation, etAge, etGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        viewInitializations();
        ImageView uploadImageView = findViewById(R.id.edit_profile_image);

        uploadImageView.setOnClickListener(view -> {
            Intent intent = new Intent(EditProfileActivity.this, UploadActivity.class);
            startActivity(intent);
        });
    }

    void viewInitializations() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etLocation = findViewById(R.id.et_location);
        etAge = findViewById(R.id.et_age);
        etGender = findViewById(R.id.et_gender);

        // To show back button in actionbar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Checking if the input in form is valid
    boolean validateInput() {
        if (etUsername.getText().toString().equals("")) {
            etUsername.setError("Please Enter Username");
            return false;
        }

        if (etEmail.getText().toString().equals("")) {
            etEmail.setError("Please Enter Your Email");
            return false;
        }

        if (etLocation.getText().toString().equals("")) {
            etLocation.setError("Please Enter Location");
            return false;
        }

        if (etAge.getText().toString().equals("")) {
            etAge.setError("Please Enter Your Age");
            return false;
        }

        if (etGender.getText().toString().equals("")) {
            etGender.setError("Please Enter Gender ");
            return false;
        }

        // checking the proper email format
        if (!isEmailValid(etEmail.getText().toString())) {
            etEmail.setError("Please Enter Valid Email");
            return false;
        }

        return true;
    }

    boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Hook Click Event

    public void performEditProfile (View v) {
        if (validateInput()) {
            // Input is valid, here send data to your server
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String location = etLocation.getText().toString();
            Integer age = Integer.parseInt(etAge.getText().toString());
            String gender = etGender.getText().toString();

            // Write user data to firebase
            FirebaseService.getInstance().updateUserProfile(username, email, location, age, gender);
            Toast.makeText(this,"Profile Update Successfully",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

