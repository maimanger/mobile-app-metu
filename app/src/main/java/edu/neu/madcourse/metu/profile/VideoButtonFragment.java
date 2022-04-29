package edu.neu.madcourse.metu.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.video.VideoActivity;

public class VideoButtonFragment extends Fragment {
    private static final String ARG_CONTACT_USER_ID = "contactUserId";
    private static final String ARG_CONTACT_NAME = "contactName";
    private static final String ARG_CONTACT_AVATAR_URI = "contactAvatarUri";
    private static final String ARG_CONNECTION_POINT = "connectionPoint";
    private static final String ARG_CONNECTION_ID = "connectionId";
    private static final String TAG = "VideoButtonFragment";

    private String contactUserId;
    private String contactName;
    private String contactAvatarUri;
    private int connectionPoint;
    private String connectionId;

    public VideoButtonFragment() {
        // Required empty public constructor
    }

    public static VideoButtonFragment newInstance(String contactUserId, String contactName,
                                                  String contactAvatarUri, int connectionPoint,
                                                  String connectionId) {
        VideoButtonFragment fragment = new VideoButtonFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTACT_USER_ID, contactUserId);
        args.putString(ARG_CONTACT_NAME, contactName);
        args.putString(ARG_CONTACT_AVATAR_URI, contactAvatarUri);
        args.putInt(ARG_CONNECTION_POINT, connectionPoint);
        args.putString(ARG_CONNECTION_ID, connectionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactUserId = getArguments().getString(ARG_CONTACT_USER_ID);
            contactName = getArguments().getString(ARG_CONTACT_NAME);
            contactAvatarUri = getArguments().getString(ARG_CONTACT_AVATAR_URI);
            connectionPoint = getArguments().getInt(ARG_CONNECTION_POINT);
            connectionId = getArguments().getString(ARG_CONNECTION_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_button, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView videoChat = view.findViewById(R.id.button_profile_video);
        videoChat.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), VideoActivity.class);
            intent.putExtra("CALLEE_ID", contactUserId);
            intent.putExtra("CALLEE_NAME", contactName);
            intent.putExtra("CALLEE_AVATAR", contactAvatarUri);
            intent.putExtra("CONNECTION_POINT", connectionPoint);
            intent.putExtra("CONNECTION_ID", connectionId);
            startActivity(intent);
        });
    }
}