package edu.neu.madcourse.metu.explore;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.List;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.daos.RecommendedProfile;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.GenderUtils;

public class RecommendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<RecommendedProfile> recommends;
    private String username;

    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_RIGHT = 2;

    public RecommendsAdapter(Context context, List<RecommendedProfile> recommends, String username) {
        this.context = context;
        this.recommends = recommends;
        this.username = username;
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
        LikeButton toggleButton;

        public RecommendProfileLeftViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.loadPhotoLeft);
            blurredProfileImg = itemView.findViewById(R.id.blurredProfilePhotoLeft);
            recommendUsername = itemView.findViewById(R.id.recUsernameLeft);
            recommendUserGender = itemView.findViewById(R.id.recUserGenderLeft);
            sendMessage = itemView.findViewById(R.id.sendMessageLeft);
            toggleButton = itemView.findViewById(R.id.likeToggleLeft);
        }

        public void setData(RecommendedProfile profile) {
            this.toggleButton.setLiked(profile.isLiked());
            // add the blur effect
            blurredProfileImg.setImageBitmap(BitmapUtils.blurBitmap(context, BitmapUtils.getBitmapFromString(profile.getRecommendUser().getProfilePhoto()), 5));

            recommendUsername.setText(profile.getRecommendUser().getUsername());
            // add text view effect
            recommendUsername.setShadowLayer(15, 0, 0, ContextCompat.getColor(context, R.color.white));

            // todo: more gender identity
            if (profile.getRecommendUser().getGender() == GenderUtils.FEMALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_female);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink)));
            } if (profile.getRecommendUser().getGender() == GenderUtils.MALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_male);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));
            }

            // todo: sendMessage
            // add listener
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("RECEIVER", profile.getRecommendUser());
                    view.getContext().startActivity(intent);
                }
            });

            // toggle
            toggleButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    profile.setLiked(true);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    profile.setLiked(false);
                }
            });
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
        LikeButton toggleButton;

        public RecommendProfileRightViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.loadPhotoRight);
            blurredProfileImg = itemView.findViewById(R.id.blurredProfilePhotoRight);
            recommendUsername = itemView.findViewById(R.id.recUsernameRight);
            recommendUserGender = itemView.findViewById(R.id.recUserGenderRight);
            sendMessage = itemView.findViewById(R.id.sendMessageRight);
            toggleButton = itemView.findViewById(R.id.likeToggleRight);
        }

        public void setData(RecommendedProfile profile) {
            this.toggleButton.setLiked(profile.isLiked());
            // create the blur effect
            blurredProfileImg.setImageBitmap(BitmapUtils.blurBitmap(context, BitmapUtils.getBitmapFromString(profile.getRecommendUser().getProfilePhoto()), 5));

            recommendUsername.setText(profile.getRecommendUser().getUsername());
            // add text effect
            recommendUsername.setShadowLayer(15, 0, 0, ContextCompat.getColor(context, R.color.white));

            // todo: more gender identity
            if (profile.getRecommendUser().getGender() == GenderUtils.FEMALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_female);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.pink)));
            } if (profile.getRecommendUser().getGender() == GenderUtils.MALE) {
                recommendUserGender.setImageResource(R.drawable.ic_gender_male);
                ImageViewCompat.setImageTintList(recommendUserGender, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));
            }

            // todo: sendMessage
            // add listener
            sendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("RECEIVER", profile.getRecommendUser());
                    view.getContext().startActivity(intent);
                }
            });

            toggleButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    profile.setLiked(true);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    profile.setLiked(false);
                }
            });
        }
    }
}
