package edu.neu.madcourse.metu.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.profile.imageUpload.UploadActivity;

public class AddStoryButtonFragment extends Fragment {
    private Uri imageFilePath;
    private String imageFirebaseUri;
    private ActivityResultLauncher<Intent> uploadActivityResultLauncher;
    private OnStoryDataPass dataPasser;

    public interface OnStoryDataPass {
        public void onStoryDataPass(Uri localPath, String storyImageUri) throws IOException;
    }

    public AddStoryButtonFragment() {
        // Required empty public constructor
    }

    public static AddStoryButtonFragment newInstance() {
        return new AddStoryButtonFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_story_button, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton addStoryButton = view.findViewById(R.id.add_story_button);
        addStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UploadActivity.class);
                uploadActivityResultLauncher.launch(intent);
            }
        });

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (OnStoryDataPass) context;
        uploadActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageFilePath = Uri.parse(data.getStringExtra("imageFilePath"));
                        imageFirebaseUri = data.getStringExtra("imageFirebaseUri");
                        Log.e("imageFilePath: ", imageFilePath.toString());
                        try {
                            dataPasser.onStoryDataPass(imageFilePath, imageFirebaseUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}



