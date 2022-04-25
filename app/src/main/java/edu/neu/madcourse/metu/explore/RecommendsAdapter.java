package edu.neu.madcourse.metu.explore;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.Constants;

public class RecommendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<RecommendedUser> recommends;
    private User loginUser;

    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_RIGHT = 2;

    public RecommendsAdapter(Context context, List<RecommendedUser> recommends, User loginUser) {
        this.context = context;
        this.recommends = recommends;
        this.loginUser = loginUser;
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
                toggleButton.setOnClickListener(new LikeButtonOnClickListener(toggleButton, recommendedUser, loginUser));
            }

            // add the blur effect
            // todo: read from uri
            new BitmapUtils.DownloadBlurredImageTask(blurredProfileImg, context, 10).execute(
                    recommendedUser.getAvatarUri()
            );

            recommendUsername.setText(recommendedUser.getNickname());
            // add text view effect
            recommendUsername.setShadowLayer(15, 0, 0, ContextCompat.getColor(context, R.color.white));

            // todo: more gender identity
            if (recommendedUser.getGender() == Constants.GENDER_FEMALE_INT) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_female);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink)));
            } if (recommendedUser.getGender() == Constants.GENDER_MALE_INT) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_male);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));
            }

            // add listener
            sendMessage.setOnClickListener(new SendMessageOnClickListener(recommendedUser, loginUser));
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
            progressBar.setVisibility(View.VISIBLE);
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
                toggleButton.setOnClickListener(new LikeButtonOnClickListener(toggleButton, recommendedUser, loginUser));
            }


            // add the blur effect
            if (recommendedUser.getAvatarUri() != null) {
                new BitmapUtils.DownloadBlurredImageTask(blurredProfileImg, context, 10).execute(
                        recommendedUser.getAvatarUri()
                );
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }


            recommendUsername.setText(recommendedUser.getNickname());
            // add text view effect
            recommendUsername.setShadowLayer(15, 0, 0, ContextCompat.getColor(context, R.color.white));

            // todo: more gender identity
            if (recommendedUser.getGender() == Constants.GENDER_FEMALE_INT) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_female);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink)));
            } if (recommendedUser.getGender() == Constants.GENDER_MALE_INT) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_male);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));
            } else {
                recommendUserGender.setImageResource(R.drawable.ic_gender_undefine);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_200)));
            }

            // add listener
            sendMessage.setOnClickListener(new SendMessageOnClickListener(recommendedUser, loginUser));

        }
    }
}
