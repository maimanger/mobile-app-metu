package edu.neu.madcourse.metu.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.video.VideoActivity;

public class VideoButtonFragment extends Fragment {

    public VideoButtonFragment() {
        // Required empty public constructor
    }

    public static VideoButtonFragment newInstance() {
        return new VideoButtonFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
/*            intent.putExtra("CALLEE_ID", getContactUserId());
            intent.putExtra("CALLEE_NAME", clickedItem.getContactName());
            intent.putExtra("CALLEE_AVATAR", clickedItem.getContactAvatarUri());
            intent.putExtra("CONNECTION_POINT", clickedItem.getConnectionPoint());
            intent.putExtra("CONNECTION_ID", clickedItem.getConnectionId());*/
            startActivity(intent);
        });
    }
}