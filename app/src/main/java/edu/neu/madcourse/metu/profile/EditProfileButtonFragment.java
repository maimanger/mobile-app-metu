package edu.neu.madcourse.metu.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import edu.neu.madcourse.metu.R;


public class EditProfileButtonFragment extends Fragment {
    public EditProfileButtonFragment() {
        // Required empty public constructor
    }

    public static EditProfileButtonFragment newInstance(String userId) {
        EditProfileButtonFragment fragment = new EditProfileButtonFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile_button, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button edit = view.findViewById(R.id.edit_profile_button);
        edit.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("userId", getArguments().getString("userId"));
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
}