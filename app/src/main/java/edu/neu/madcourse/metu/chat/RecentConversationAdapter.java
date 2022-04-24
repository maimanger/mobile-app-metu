package edu.neu.madcourse.metu.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.Utils;
import edu.neu.madcourse.metu.chat.daos.RecentConversation;
import edu.neu.madcourse.metu.utils.BitmapUtils;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.RecentConversationViewHolder> {
    private Context context;
    private List<RecentConversation> recentConversationList;
    private String username;

    public RecentConversationAdapter(Context context, List<RecentConversation> recentConversationList, String username) {
        this.context = context;
        this.recentConversationList = recentConversationList;
        this.username = username;
    }

    @NonNull
    @Override
    public RecentConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentConversationViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_recent_conversation, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversationViewHolder holder, int position) {
        holder.setData(this.recentConversationList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.recentConversationList.size();
    }

    public class RecentConversationViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView contactAvatar;
        TextView recentContact;
        TextView recentMessage;
        TextView messageTimestamp;
        CardView recentContactCard;
        ImageView unreadMark;

        public RecentConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            recentContactCard = itemView.findViewById(R.id.recentContactCard);
            contactAvatar = itemView.findViewById(R.id.contactAvatar);
            recentContact = itemView.findViewById(R.id.recentContact);
            recentMessage = itemView.findViewById(R.id.recentMessage);
            messageTimestamp = itemView.findViewById(R.id.messageTimestamp);
            unreadMark = itemView.findViewById(R.id.unreadMark);
        }

        public void setData(RecentConversation conversation) {
            // todo: add default avatar
            if (conversation.getContactAvatar() != null
                    && conversation.getContactAvatar().length() > 0)  {
                new Utils.DownloadImageTask(contactAvatar).execute(conversation.getContactAvatar());
            } else {
                // set to be the default avatar
                contactAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            //contactAvatar.setImageBitmap(BitmapUtils.getBitmapFromString(conversation.getContactAvatar()));
            recentContact.setText(conversation.getRecentContactNickname());
            recentMessage.setText(conversation.getRecentMessage());
            messageTimestamp.setText(conversation.getConversationFormattedTimestamp());

            // if sender is the current user or the message is read
            // hide the unread mark
            if (conversation.getLastMessage().getSender().equals(username) || conversation.getLastMessage().getIsRead()) {
                unreadMark.setVisibility(View.GONE);
            } else {
                unreadMark.setVisibility(View.VISIBLE);
            }

            // set listener
            recentContactCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ChatActivity.class);

                    // put the receiver
                    intent.putExtra("RECEIVER", conversation.getRecentContact());
                    // put the connectionId
                    intent.putExtra("CONNECTION_ID", conversation.getConnectionId());
                    view.getContext().startActivity(intent);
                }
            });

        }
    }
}
