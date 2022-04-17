package edu.neu.madcourse.metu.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.metu.R;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryHolder> {
    private List<Story> storyList;

    public class StoryHolder extends RecyclerView.ViewHolder {
        public ImageView storyImage;

        public StoryHolder(View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.story_image);

//            itemView.setOnClickListener((View v) -> {
//                int position = getLayoutPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    StoryAdapter.this.clickListener.onClick(position);
//                }
//            });
        }
    }

    public StoryAdapter(List<Story> storyList) {
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public StoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_story, parent, false);
        return new StoryAdapter.StoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryHolder holder, int position) {
        Story currentStory = storyList.get(position);
        holder.storyImage.setImageResource(currentStory.getStoryImageSrc());

    }

    @Override
    public int getItemCount() {
        return storyList == null ? 0 : storyList.size();
    }
}
