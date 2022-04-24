package edu.neu.madcourse.metu.explore;

import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.utils.Constants;

public class SendMessageOnClickListener implements View.OnClickListener {
    private RecommendedUser recommendedUser;
    private String userId;
    private String nickname;
    private String avatarUri;

    public SendMessageOnClickListener(RecommendedUser recommendedUser) {
        this.recommendedUser = recommendedUser;
        this.userId = App.getUserId();
        this.nickname = App.getNickname();
        this.avatarUri = App.getAvatarUri();
    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ConnectionUser connectionUser = new ConnectionUser();
        connectionUser.setUserId(recommendedUser.getUserId());
        connectionUser.setIsLiked(recommendedUser.getIsLiked());
        connectionUser.setNickname(recommendedUser.getNickname());
        connectionUser.setAvatarUri(recommendedUser.getAvatarUri());

        intent.putExtra("RECEIVER", connectionUser);
        // fetch the connection_id
        FirebaseDatabase.getInstance().getReference(Constants.USERS_STORE)
                .child(userId)
                .child(Constants.USER_CONNECTIONS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(userId + recommendedUser.getUserId())) {
                            intent.putExtra("CONNECTION_ID", userId + recommendedUser.getUserId());
                        } else if (snapshot.hasChild(recommendedUser.getUserId() + userId)) {
                            intent.putExtra("CONNECTION_ID", recommendedUser.getUserId() + userId);
                        } else {
                            intent.putExtra("CONTENTION_ID", "");
                        }

                        view.getContext().startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
