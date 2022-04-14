package edu.neu.madcourse.metu.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.metu.R;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagHolder> {
    private List<Tag> tagList;

    public class TagHolder extends RecyclerView.ViewHolder{
        public TextView tagText;

        public TagHolder(View itemView) {
            super(itemView);
            this.tagText = itemView.findViewById(R.id.card_tag) ;

        }
    }

    public TagAdapter(List<Tag> tagList) {
        this.tagList = tagList;
    }

    @NonNull
    @Override
    public TagHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_tag, parent, false);
        return new TagAdapter.TagHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagHolder holder, int position) {
        Tag currentTag = tagList.get(position);
        holder.tagText.setText(currentTag.getTagStr());

    }
    @Override
    public int getItemCount() { return tagList == null ? 0 : tagList.size();}
}
