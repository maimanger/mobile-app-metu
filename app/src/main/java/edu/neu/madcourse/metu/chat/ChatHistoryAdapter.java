package edu.neu.madcourse.metu.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.ChatItem;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ChatItem> chatItemList;
    private String username;
    private final Bitmap receiverAvatar;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatHistoryAdapter(Context context, List<ChatItem> chatItemList, String username, Bitmap receiverAvatar) {
        this.context = context;
        this.chatItemList = chatItemList;
        this.username = username;
        this.receiverAvatar = receiverAvatar;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_sent_message, parent,false));
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceiveMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_received_message, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // check if the timestamp is the same with the previous chat item
        boolean showTimestamp = true;
        if (position > 0 && chatItemList.get(position).generateFormattedTimestamp()
                .equals(chatItemList.get(position - 1).generateFormattedTimestamp())) {
            showTimestamp = false;
        }

        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatItemList.get(position), showTimestamp);
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED){
            ((ReceiveMessageViewHolder) holder).setData(chatItemList.get(position), receiverAvatar, showTimestamp);
        }
    }

    @Override
    public int getItemCount() {
        return chatItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatItemList.get(position).getSender().equals(this.username)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    // send messages
    public class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView sentTimestamp;
        TextView messageSent;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSent = itemView.findViewById(R.id.textMessageSent);
            sentTimestamp = itemView.findViewById(R.id.sentTimestamp);
        }

        void setData(ChatItem chatItem, boolean showTimestamp) {
            if (chatItem == null) {
                return;
            }

            messageSent.setText(chatItem.getMessage());

            // hide the timestamp
            if (!showTimestamp) {
                sentTimestamp.setText(null);
                sentTimestamp.setVisibility(View.GONE);
            } else {
                sentTimestamp.setText(chatItem.generateFormattedTimestamp());
                // remember to set it back to visible
                sentTimestamp.setVisibility(View.VISIBLE);
            }
        }
    }

    // receive messages
    public class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {
        TextView receiveTimestamp;
        TextView messageReceived;
        TextView senderName;
        ImageView senderAvatar;

        public ReceiveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveTimestamp = itemView.findViewById(R.id.receivedTimestamp);
            messageReceived = itemView.findViewById(R.id.textMessageReceived);
            senderName = itemView.findViewById(R.id.senderName);
            senderAvatar = itemView.findViewById(R.id.senderAvatar);
        }

        void setData(ChatItem chatItem, Bitmap receiverAvatar, boolean showTimestamp) {
            if (chatItem == null) {
                return;
            }

            senderName.setText(chatItem.getSender());

            messageReceived.setText(chatItem.getMessage());

            if (receiverAvatar != null) {
                senderAvatar.setImageBitmap(receiverAvatar);
            }

            if (!showTimestamp) {
                receiveTimestamp.setText(null);
                receiveTimestamp.setVisibility(View.GONE);
            } else {
                receiveTimestamp.setText(chatItem.generateFormattedTimestamp());
                // remember to set it back to visible
                receiveTimestamp.setVisibility(View.VISIBLE);
            }
        }
    }
}

