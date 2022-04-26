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
import android.widget.ImageView;

import edu.neu.madcourse.metu.App;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.explore.LikeButtonOnClickListener;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.imageUpload.Image;
import edu.neu.madcourse.metu.service.DataFetchCallback;
import edu.neu.madcourse.metu.service.FirebaseService;

public class LikeButtonFragment extends Fragment {
    private static final String ARG_PROFILE_USER_ID = "profileUserId";
    private String profileUserId;

    public LikeButtonFragment() {
        // Required empty public constructor
    }

    public static LikeButtonFragment newInstance(String profileUserId) {
        LikeButtonFragment fragment = new LikeButtonFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROFILE_USER_ID, profileUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profileUserId = getArguments().getString(ARG_PROFILE_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_like_button, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        String loginUserId = ((App) getActivity().getApplication()).getLoginUser().getUserId();
        String loginUserId = "tom@tomDOTcom";
        FirebaseService.getInstance().fetchUserProfileData(loginUserId,
                loginUser -> FirebaseService.getInstance().fetchUserProfileData(profileUserId,
                        profileUser -> {
                            RecommendedUser recommendedUser = new RecommendedUser();
                            recommendedUser.setUserId(profileUser.getUserId());
                            recommendedUser.setNickname(profileUser.getNickname());
                            recommendedUser.setAvatarUri(profileUser.getAvatarUri());
                            recommendedUser.setGender(profileUser.getGender());
                            recommendedUser.setIsLiked(true);
                            Button profile_like_button = view.findViewById(R.id.profile_like_button);
                            profile_like_button.setOnClickListener(new LikeButtonOnClickListener(
                                    profile_like_button, recommendedUser, loginUser));
                        }));
    }
}
