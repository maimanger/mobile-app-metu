package edu.neu.madcourse.metu.profile;

//package com.example.handyopinion;

import static edu.neu.madcourse.metu.utils.Constants.GENDER_MAP;

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

import com.squareup.picasso.Callback;

import java.io.IOException;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.imageUpload.UploadActivity;
import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.utils.Utils;

public class EditProfileActivity extends BaseCalleeActivity {

    EditText etNickname, etLocation, etAge, etGender;
    private Uri imageFilePath;
    private Uri imageFirebaseUri;
    private Bitmap avatarBitmap;
    private ActivityResultLauncher<Intent> uploadActivityResultLauncher;
    private String profileUserId;
    ImageView uploadImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileUserId = getIntent().getExtras().getString("userId");
        viewInitializations();

        uploadImageView = findViewById(R.id.edit_profile_image);
        String loginUserAvatarUri = ((App)getApplication()).getLoginUser().getAvatarUri();
        Utils.loadImgUri(loginUserAvatarUri, uploadImageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception e) {
                uploadImageView.setImageResource(R.drawable.user_avatar);
            }
        });

        uploadActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageFilePath = Uri.parse(data.getStringExtra("imageFilePath"));
                        imageFirebaseUri = Uri.parse(data.getStringExtra("imageFirebaseUri"));

                        // TODO: Should be implemented in Button(R.id.bt_register) onclickListener
                        FirebaseService.getInstance().updateUserAvatar(profileUserId, imageFirebaseUri.toString());
                        try {
                            avatarBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFilePath);
                            uploadImageView.setImageBitmap(avatarBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                });

        uploadImageView.setOnClickListener(view -> {
            Intent intent = new Intent(EditProfileActivity.this, UploadActivity.class);
            uploadActivityResultLauncher.launch(intent);
        });

        findViewById(R.id.image_editProfile).setOnClickListener(view -> {
            Intent intent = new Intent(EditProfileActivity.this, UploadActivity.class);
            uploadActivityResultLauncher.launch(intent);
        });

    }

    void viewInitializations() {
        etNickname = findViewById(R.id.et_username);
        etLocation = findViewById(R.id.et_location);
        etAge = findViewById(R.id.et_age);
        etGender = findViewById(R.id.et_gender);
        ImageView uploadImageView = findViewById(R.id.edit_profile_image);



        FirebaseService.getInstance().fetchUserProfileData(profileUserId, user -> {
            etNickname.setText(user.getNickname());
            etLocation.setText(user.getLocation());
            etAge.setText(String.valueOf(user.getAge()));
            etGender.setText(GENDER_MAP.get(user.getGender()));
            new Utils.DownloadImageTask(uploadImageView).execute(user.getAvatarUri());
        });
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
            loginUser.setEmail("tom@tom.com");  // TODO(xin): to remove
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