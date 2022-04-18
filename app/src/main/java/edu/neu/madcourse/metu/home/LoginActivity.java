package edu.neu.madcourse.metu.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import edu.neu.madcourse.metu.R;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Context mContext;
    private EditText mEmail, mPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.email_signinact);
        mPassword = findViewById(R.id.password_signinact);
        mContext = LoginActivity.this;
        init();
    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking string if null.");
        return string.equals("");
    }

    //----------------------------------------Firebase----------------------------------------

    private void init() {
        //initialize the button for logging in
        Button btnLogin = findViewById(R.id.loginbtn);
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in");
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (isStringNull(email) || isStringNull(password)) {
                    Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                }
            }
        });

        TextView linkSignUp = findViewById(R.id.register);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to register screen");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

}


