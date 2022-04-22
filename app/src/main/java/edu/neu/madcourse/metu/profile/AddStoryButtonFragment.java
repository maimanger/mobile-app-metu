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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddStoryButtonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddStoryButtonFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Uri imageFilePath;
    private ActivityResultLauncher<Intent> uploadActivityResultLauncher;
    private OnStoryDataPass dataPasser;

    public interface OnStoryDataPass {
        public void onStoryDataPass(Uri data) throws IOException;
    }

    public AddStoryButtonFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddStoryButtonFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddStoryButtonFragment newInstance(String param1, String param2) {
        AddStoryButtonFragment fragment = new AddStoryButtonFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_story_button, container, false);
    }


    //    Adding more stories
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton addStoryButton = view.findViewById(R.id.add_story_button);
        addStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UploadActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("srcClass", "AddStoryButtonFragment");
                intent.putExtras(bundle);
//                startActivity(intent);
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
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            imageFilePath = Uri.parse(data.getStringExtra("imageFilePath"));
                            Log.e("imageFilePath: ", imageFilePath.toString());
                            try {
                                dataPasser.onStoryDataPass(imageFilePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

//    public void setImageFilePath(Uri imageFilePath) {
//        this.imageFilePath = imageFilePath;
//        Log.e("imageFilePath: ", imageFilePath.toString());
//    }
}


