package edu.neu.madcourse.metu.profile;

//package com.example.handyopinion;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import edu.neu.madcourse.metu.R;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName, etEmail, etLocation, etBirthday, etGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        viewInitializations();
    }

    void viewInitializations() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etLocation = findViewById(R.id.et_location);
        etBirthday = findViewById(R.id.et_birthday);
        etGender = findViewById(R.id.et_gender);

        // To show back button in actionbar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Checking if the input in form is valid
    boolean validateInput() {
        if (etName.getText().toString().equals("")) {
            etName.setError("Please Enter Name");
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

        if (etBirthday.getText().toString().equals("")) {
            etBirthday.setError("Please Enter Your Birthday");
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

            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String location = etLocation.getText().toString();
            String birthday = etBirthday.getText().toString();
            String gender = etGender.getText().toString();

            Toast.makeText(this,"Profile Update Successfully",Toast.LENGTH_SHORT).show();
            // Here you can call you API

        }
    }

}