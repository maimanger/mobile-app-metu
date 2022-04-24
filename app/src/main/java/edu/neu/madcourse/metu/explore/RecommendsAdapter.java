package edu.neu.madcourse.metu.explore;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.Utils;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.daos.RecommendedProfile;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.Constants;
import edu.neu.madcourse.metu.utils.GenderUtils;
import edu.neu.madcourse.metu.utils.MessagingService;

public class RecommendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<RecommendedUser> recommends;
    private String userId;

    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_RIGHT = 2;

    public RecommendsAdapter(Context context, List<RecommendedUser> recommends, String userId) {
        this.context = context;
        this.recommends = recommends;
        this.userId = userId;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return VIEW_TYPE_LEFT;
        }
        return VIEW_TYPE_RIGHT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LEFT) {
            return new RecommendProfileLeftViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_explore_card_left, parent,false));
        } else {
            return new RecommendProfileRightViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_explore_card_right, parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position % 2 == 0) {
            ((RecommendProfileLeftViewHolder) holder).setData(this.recommends.get(position));
        } else {
            ((RecommendProfileRightViewHolder) holder).setData(this.recommends.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return this.recommends.size();
    }

    // left
    public class RecommendProfileLeftViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        ImageView blurredProfileImg;
        TextView recommendUsername;
        ImageView recommendUserGender;
        AppCompatImageView sendMessage;
        //LikeButton toggleButton;
        Button toggleButton;

        public RecommendProfileLeftViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.loadPhotoLeft);
            blurredProfileImg = itemView.findViewById(R.id.blurredProfilePhotoLeft);
            recommendUsername = itemView.findViewById(R.id.recUsernameLeft);
            recommendUserGender = itemView.findViewById(R.id.recUserGenderLeft);
            sendMessage = itemView.findViewById(R.id.sendMessageLeft);
            toggleButton = itemView.findViewById(R.id.likeToggleLeft);
        }

        public void setData(RecommendedUser recommendedUser) {
            // this.toggleButton.setLiked(profile.isLiked());
            if (recommendedUser.getIsLiked()) {
                toggleButton.setBackgroundResource(R.drawable.ic_like);
            } else {
                toggleButton.setBackgroundResource(R.drawable.ic_unlike);
                // set listener
                // toggle button listener
                toggleButton.setOnClickListener(new LikeButtonOnClickListener(toggleButton, recommendedUser));
            }

            // add the blur effect
            // todo: read from uri
            new BitmapUtils.DownloadBlurredImageTask(blurredProfileImg, context, 5).execute(
                    recommendedUser.getAvatarUri()
            );

            recommendUsername.setText(recommendedUser.getNickname());
            // add text view effect
            recommendUsername.setShadowLayer(15, 0, 0, ContextCompat.getColor(context, R.color.white));

            // todo: more gender identity
            if (recommendedUser.getGender() == GenderUtils.FEMALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_female);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink)));
            } if (recommendedUser.getGender() == GenderUtils.MALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_male);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));
            }

            // add listener
            sendMessage.setOnClickListener(new SendMessageOnClickListener(recommendedUser));
        }
    }


    // todo: abstract
    // right
    public class RecommendProfileRightViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        ImageView blurredProfileImg;
        TextView recommendUsername;
        ImageView recommendUserGender;
        AppCompatImageView sendMessage;
        Button toggleButton;

        public RecommendProfileRightViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.loadPhotoRight);
            blurredProfileImg = itemView.findViewById(R.id.blurredProfilePhotoRight);
            recommendUsername = itemView.findViewById(R.id.recUsernameRight);
            recommendUserGender = itemView.findViewById(R.id.recUserGenderRight);
            sendMessage = itemView.findViewById(R.id.sendMessageRight);
            toggleButton = itemView.findViewById(R.id.likeToggleRight);
        }

        public void setData(RecommendedUser recommendedUser) {

            if (recommendedUser.getIsLiked()) {
                toggleButton.setBackgroundResource(R.drawable.ic_like);
            } else {
                toggleButton.setBackgroundResource(R.drawable.ic_unlike);
                toggleButton.setOnClickListener(new LikeButtonOnClickListener(toggleButton, recommendedUser));
            }


            // add the blur effect
            new BitmapUtils.DownloadBlurredImageTask(blurredProfileImg, context, 5).execute(
                    recommendedUser.getAvatarUri()
            );

            recommendUsername.setText(recommendedUser.getNickname());
            // add text view effect
            recommendUsername.setShadowLayer(15, 0, 0, ContextCompat.getColor(context, R.color.white));

            // todo: more gender identity
            if (recommendedUser.getGender() == GenderUtils.FEMALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_female);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink)));
            } if (recommendedUser.getGender() == GenderUtils.MALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_male);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));
            }

            // add listener
            sendMessage.setOnClickListener(new SendMessageOnClickListener(recommendedUser));

        }
    }
}
