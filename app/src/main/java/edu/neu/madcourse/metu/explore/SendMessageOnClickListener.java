package edu.neu.madcourse.metu.explore;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.MessageSendingUtils;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.Constants;

public class SendMessageOnClickListener implements View.OnClickListener {
    private ConnectionUser receiver;
    private ConnectionUser sender;

    public SendMessageOnClickListener(ConnectionUser receiver, ConnectionUser sender) {
        this.receiver = receiver;
        this.sender = sender;
    }


    @Override
    public void onClick(View view) {
        if (receiver == null || sender == null) {
            Toast.makeText(view.getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        // fetch the connectionId
        MessageSendingUtils.fetchConnectionId(receiver.getUserId(), sender.getUserId(), (connectionId) -> {
            if (connectionId == null) {
                intent.putExtra("CONNECTION_ID", "");
            } else {
                intent.putExtra("CONNECTION_ID", connectionId);
            }

            intent.putExtra("RECEIVER", receiver);
            view.getContext().startActivity(intent);
        });

//        ConnectionUser connectionUser = new ConnectionUser();
//        connectionUser.setUserId(recommendedUser.getUserId());
//        connectionUser.setIsLiked(recommendedUser.getIsLiked());
//        connectionUser.setNickname(recommendedUser.getNickname());
//        connectionUser.setAvatarUri(recommendedUser.getAvatarUri());
//
//        intent.putExtra("RECEIVER", connectionUser);
//        // fetch the connection_id
//        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
//                .child(loginUser.getUserId())
//                .child(Constants.USER_CONNECTIONS)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChild(loginUser.getUserId() + recommendedUser.getUserId())) {
//                            intent.putExtra("CONNECTION_ID", loginUser.getUserId() + recommendedUser.getUserId());
//                        } else if (snapshot.hasChild(recommendedUser.getUserId() + loginUser.getUserId())) {
//                            intent.putExtra("CONNECTION_ID", recommendedUser.getUserId() + loginUser.getUserId());
//                        } else {
//                            intent.putExtra("CONTENTION_ID", "");
//                        }
//
//                        view.getContext().startActivity(intent);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
    }
}
