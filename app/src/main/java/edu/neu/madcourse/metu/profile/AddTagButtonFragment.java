package edu.neu.madcourse.metu.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import edu.neu.madcourse.metu.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddTagButtonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTagButtonFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private List<Tag> tagList;
//    private TagAdapter tagAdapter;
//    private String mParam2;
    private String userInputTagText;
    private OnDataPass dataPasser;

    public AddTagButtonFragment() {
        // Required empty public constructor
    }

    public interface OnDataPass {
        public void onDataPass(String data);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddTagButtonFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddTagButtonFragment newInstance() {
        AddTagButtonFragment fragment = new AddTagButtonFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, tagList);
//        args.putString(ARG_PARAM2, tagAdapter);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            tagList = getArguments().getString(ARG_PARAM1);
//            tagAdapter = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_tag_button, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("New tag name");
        // Set up the input
        final EditText input = new EditText(view.getContext());

        FloatingActionButton addTagButton = view.findViewById(R.id.add_tag_button);
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                if (input.getParent() != null) {
                    ((ViewGroup) input.getParent()).removeView(input);
                }
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userInputTagText = input.getText().toString();
//                        int pos = tagList.size();
//                        addTag(pos, userInputTagText);
                        dataPasser.onDataPass(userInputTagText);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

//        Button edit = view.findViewById(R.id.edit_profile_button);
//        edit.setOnClickListener(view1 -> {
//            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
//            startActivity(intent);
//        });
    }


}