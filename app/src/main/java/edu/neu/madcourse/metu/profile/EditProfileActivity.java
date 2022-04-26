package edu.neu.madcourse.metu.profile;

//package com.example.handyopinion;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.imageUpload.UploadActivity;
import edu.neu.madcourse.metu.service.FirebaseService;

public class EditProfileActivity extends BaseCalleeActivity {

    EditText etNickname, etLocation, etAge, etGender;
    private Uri imageFilePath;
    private Uri imageFirebaseUri;
    private Bitmap avatarBitmap;
    private ActivityResultLauncher<Intent> uploadActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        String userId = getIntent().getExtras().getString("userId");
        viewInitializations();

        ImageView uploadImageView = findViewById(R.id.edit_profile_image);
//        ImageView avatarImageView = findViewById(R.id.imageProfile);
        uploadActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            imageFilePath = Uri.parse(data.getStringExtra("imageFilePath"));
                            imageFirebaseUri = Uri.parse(data.getStringExtra("imageFirebaseUri"));

                            Log.e("onCreate", userId);
                            FirebaseService.getInstance().updateUserAvatar(userId, imageFirebaseUri.toString());
                            try {
                                avatarBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFilePath);
                                uploadImageView.setImageBitmap(avatarBitmap);
                                Log.e("imageFilePath_: ", "avatar updated");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


        uploadImageView.setOnClickListener(view -> {
            Intent intent = new Intent(EditProfileActivity.this, UploadActivity.class);
            uploadActivityResultLauncher.launch(intent);

        });
    }

    void viewInitializations() {
        etNickname = findViewById(R.id.et_username);
        etLocation = findViewById(R.id.et_location);
        etAge = findViewById(R.id.et_age);
        etGender = findViewById(R.id.et_gender);
    }

    // Checking if the input in form is valid
    boolean validateInput() {
        if (etNickname.getText().toString().equals("")) {
            etNickname.setError("Please Enter Username");
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

        return true;
    }

    boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Hook Click Event

    public void performEditProfile(View v) {
        if (validateInput()) {
            // Input is valid, here send data to your server
            String nickname = etNickname.getText().toString();
            String location = etLocation.getText().toString();
            Integer age = Integer.parseInt(etAge.getText().toString());
            String gender = etGender.getText().toString();

            Integer genderInt = 2;
            if (gender.toLowerCase().equals("male")) {
                genderInt = 0;
            } else if (gender.toLowerCase().equals("female")) {
                genderInt = 1;
            }

            // Write user data to firebase
            // User loginUser = ((App) getApplication()).getLoginUser();
            User loginUser = new User();
            loginUser.setEmail("tom@tom.com");
            loginUser.setNickname(nickname);
            loginUser.setLocation(location);
            loginUser.setAge(age);
            loginUser.setGender(genderInt);

            FirebaseService.getInstance().updateUserProfile(loginUser);
            Toast.makeText(this, "Profile Update Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

