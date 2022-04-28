package edu.neu.madcourse.metu.profile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.firebase.database.FirebaseDatabase;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.explore.AbstractLikeButtonOnClickListener;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.models.User;
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

        // init the like button
        FloatingActionButton profile_like_button = view.findViewById(R.id.profile_like_button);
        // get current loginUser
        User loginUser = ((App) getActivity().getApplication()).getLoginUser();
        // fetch profile user
        FirebaseService.getInstance().fetchUserProfileData(profileUserId, (receiverProfile) -> {
            // set the likeButtonOnClickListener
            ConnectionUser receiver = receiverProfile.convertToConnectionUser();
            profile_like_button.setOnClickListener(new AbstractLikeButtonOnClickListener(
                    loginUser.convertToConnectionUser(),
                    receiver) {
                @Override
                protected void switchViewToBeLiked() {
                    profile_like_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.browser_actions_bg_grey)));
                    profile_like_button.getDrawable().mutate().setTint(getResources().getColor(R.color.like));
                    //profile_like_button.setForegroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.like)));
                    profile_like_button.setOnClickListener(null);
                    profile_like_button.setClickable(false);
                }
            });

        });


//        String loginUserId = ((App) getActivity().getApplication()).getLoginUser().getUserId();
        // String loginUserId = "tom@tomDOTcom";
//        FirebaseService.getInstance().fetchUserProfileData(loginUserId,
//                loginUser -> FirebaseService.getInstance().fetchUserProfileData(profileUserId,
//                        profileUser -> {
//                            RecommendedUser recommendedUser = new RecommendedUser();
//                            recommendedUser.setUserId(profileUser.getUserId());
//                            recommendedUser.setNickname(profileUser.getNickname());
//                            recommendedUser.setAvatarUri(profileUser.getAvatarUri());
//                            recommendedUser.setGender(profileUser.getGender());
//                            recommendedUser.setIsLiked(true);
//                            Button profile_like_button = view.findViewById(R.id.profile_like_button);
//                            profile_like_button.setOnClickListener(new LikeButtonOnClickListener(
//                                    profile_like_button, recommendedUser, loginUser));
//                        }));
    }
}
