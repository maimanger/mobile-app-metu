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
import java.util.Objects;

import edu.neu.madcourse.metu.R;

public class AddTagButtonFragment extends Fragment {
    private String userInputTagText;
    private OnDataPass dataPasser;

    public interface OnDataPass {
        public void onDataPass(String data);
    }

    public AddTagButtonFragment() {
        // Required empty public constructor
    }

    public static AddTagButtonFragment newInstance() {
        return new AddTagButtonFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_tag_button, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    // Adding more tags
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
    }
}