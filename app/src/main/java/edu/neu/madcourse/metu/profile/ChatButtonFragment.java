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

import edu.neu.madcourse.metu.App;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.explore.SendMessageOnClickListener;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.models.User;


public class ChatButtonFragment extends Fragment {
    private static final String ARG_TO_CHAT_USER = "toChatUser";
    private static final String ARG_IS_LIKED_BY_LOGIN_USER = "isLikedByLoginUser";
    private static final String ARG_LOGIN_USER_ID = "loginUserId";

    private User toChatUser;
    private Boolean isLikedByLoginUser;
    private String loginUserId;

    public ChatButtonFragment() {
        // Required empty public constructor
    }

    public static ChatButtonFragment newInstance(User toChatUser, Boolean isLikedByLoginUser,
                                                 String loginUserId) {
        ChatButtonFragment fragment = new ChatButtonFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TO_CHAT_USER, toChatUser);
        args.putBoolean(ARG_IS_LIKED_BY_LOGIN_USER, isLikedByLoginUser);
        args.putString(ARG_LOGIN_USER_ID, loginUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            toChatUser = getArguments().getParcelable(ARG_TO_CHAT_USER);
            isLikedByLoginUser = getArguments().getBoolean(ARG_IS_LIKED_BY_LOGIN_USER);
            loginUserId = getArguments().getString(ARG_LOGIN_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_button, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton chat = view.findViewById(R.id.chatting_button);

        ConnectionUser sender = ((App) getActivity().getApplication()).getLoginUser().convertToConnectionUser();
        ConnectionUser receiver = toChatUser.convertToConnectionUser();
        receiver.setIsLiked(isLikedByLoginUser);

        chat.setOnClickListener(new SendMessageOnClickListener(receiver, sender));

//        chat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getActivity(), ChatActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                ConnectionUser connectionUser = new ConnectionUser();
//                connectionUser.setUserId(toChatUser.getUserId());
//                connectionUser.setIsLiked(isLikedByLoginUser);
//                connectionUser.setNickname(toChatUser.getNickname());
//                connectionUser.setAvatarUri(toChatUser.getAvatarUri());
//                intent.putExtra("RECEIVER", connectionUser);
//                intent.putExtra("CONNECTION_ID", toChatUser.getUserId() + loginUserId);
//                startActivity(intent);
//            }
//        });
    }
}
