package edu.neu.madcourse.metu.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.utils.Utils;

public class StarButtonFragment extends Fragment {
    private static final String ARG_CONNECTION_POINT = "connectionPoint";

    private int connectionPoint;

    public StarButtonFragment() {
        // Required empty public constructor
    }

    public static StarButtonFragment newInstance(int connectionPoint) {
        StarButtonFragment fragment = new StarButtonFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CONNECTION_POINT, connectionPoint);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            connectionPoint = getArguments().getInt(ARG_CONNECTION_POINT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_star_button, container, false);
        //TODO: set friend level text and setColorFilter to the imageView! See ContactsAdapter
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView friendLevelTextView = view.findViewById(R.id.text_profile_star_friend_level);
        friendLevelTextView.setText(String.valueOf(Utils.calculateFriendLevel(connectionPoint)));
    }
}