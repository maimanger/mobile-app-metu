package edu.neu.madcourse.metu.profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;

import java.io.IOException;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.imageUpload.UploadActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import edu.neu.madcourse.metu.utils.Utils;

public class EditProfileActivity extends BaseCalleeActivity {

    EditText etNickname, etLocation, etAge;
    private Uri imageFilePath;
    private Uri imageFirebaseUri;
    private Bitmap avatarBitmap;
    private ActivityResultLauncher<Intent> uploadActivityResultLauncher;
    private String profileUserId;
    private ImageView uploadImageView;
    private String gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileUserId = getIntent().getExtras().getString("userId");
        viewInitializations();

        uploadImageView = findViewById(R.id.edit_profile_image);
        String loginUserAvatarUri = ((App) getApplication()).getLoginUser().getAvatarUri();
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
                        FirebaseService.getInstance().updateUserAvatar(profileUserId,
                                imageFirebaseUri.toString());
                        try {
                            avatarBitmap = MediaStore.Images.Media.getBitmap(getContentResolver()
                                    , imageFilePath);
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
        ImageView uploadImageView = findViewById(R.id.edit_profile_image);


        findViewById(R.id.btn_editProfile_back).setOnClickListener(View -> onBackPressed());

        Spinner spinner = findViewById(R.id.gender_spinner);

        String[] genders = getResources().getStringArray(R.array.genders);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, genders) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor((position == 0) ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*if (position > 0) {
                    gender = (String) parent.getItemAtPosition(position);
                }*/
                gender = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        FirebaseService.getInstance().fetchUserProfileDataOneTime(profileUserId, user -> {
            etNickname.setText(user.getNickname());
            etLocation.setText(user.getLocation());
            etAge.setText(String.valueOf(user.getAge()));
            spinner.setSelection(user.getGender() + 1);

            new Utils.DownloadImageTask(uploadImageView).execute(user.getAvatarUri());
        });
    }


    // Checking if the input in form is valid
    boolean validateInput() {
        if (etNickname.getText().toString().equals("")) {
            etNickname.setError("Please Enter Username");
            return false;
        }

        /*if (etLocation.getText().toString().equals("")) {
            etLocation.setError("Please Enter Location");
            return false;
        }*/

        if (etAge.getText().toString().equals("")) {
            etAge.setError("Please Enter Your Age");
            return false;
        }

        if (gender == null) {
            Toast.makeText(getApplicationContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
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

            Integer genderInt = 2;
            if (gender.toLowerCase().equals("male")) {
                genderInt = 0;
            } else if (gender.toLowerCase().equals("female")) {
                genderInt = 1;
            }

            // Write user data to firebase
            User loginUser = ((App) getApplication()).getLoginUser();
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