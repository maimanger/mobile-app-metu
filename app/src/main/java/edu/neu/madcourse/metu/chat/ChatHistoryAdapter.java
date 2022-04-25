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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.utils.Constants;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ChatItem> chatItemList;
    private ConnectionUser receiver;
    private String userId;
    private String connectionId;
    private Bitmap receiverAvatar;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatHistoryAdapter(Context context, List<ChatItem> chatItemList, String userId, String connectionId, Bitmap receiverAvatar, ConnectionUser receiver) {
        this.context = context;
        this.chatItemList = chatItemList;
        this.userId = userId;
        this.connectionId = connectionId;
        this.receiverAvatar = receiverAvatar;
        this.receiver = receiver;
    }

    public void updateConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public void updateReceiverAvatar(Bitmap avatar) {
        this.receiverAvatar = avatar;
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

//        if (position == getItemCount() - 1) {
//            reference.child("lastMessage").setValue(chatItemList.get(position));
//        }

        // check if the timestamp is the same with the previous chat item
        boolean showTimestamp = true;

        if (position > 0 && chatItemList.get(position).generateFormattedTimestamp()
                .equals(chatItemList.get(position - 1).generateFormattedTimestamp())) {
            showTimestamp = false;
        }

        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatItemList.get(position), showTimestamp);



        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED){
            ((ReceiveMessageViewHolder) holder).setData(receiver, chatItemList.get(position), receiverAvatar, showTimestamp);

            // todo: check if it is the last message and it's unread
            // check if it is the last message
            if (position == getItemCount() - 1) {
                // set it to be read in the 'Connection'
                FirebaseDatabase.getInstance().getReference(Constants.CONNECTIONS_STORE)
                        .child(connectionId).child(Constants.CONNECTION_LAST_MESSAGE)
                        .child(Constants.MESSAGE_IS_READ)
                        .setValue(true);
                // todo: check if it is work
                chatItemList.get(position).setIsRead(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatItemList.get(position).getSender().equals(this.userId)) {
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

        void setData(ConnectionUser receiver, ChatItem chatItem, Bitmap receiverAvatar, boolean showTimestamp) {
            if (chatItem == null) {
                return;
            }

            senderName.setText(receiver.getNickname());

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

