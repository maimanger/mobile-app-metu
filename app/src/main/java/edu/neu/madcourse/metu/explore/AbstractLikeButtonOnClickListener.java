package edu.neu.madcourse.metu.explore;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.chat.MessageSendingUtils;
import edu.neu.madcourse.metu.models.ConnectionUser;
import edu.neu.madcourse.metu.utils.Constants;

public abstract class AbstractLikeButtonOnClickListener implements View.OnClickListener {

    protected ConnectionUser sender;
    protected ConnectionUser receiver;

    public AbstractLikeButtonOnClickListener(ConnectionUser sender, ConnectionUser receiver) {
        this.sender = sender;
        this.receiver = receiver;

    }

    @Override
    public void onClick(View view) {
        // 1. check if the receiver is liked
        if (receiver.getIsLiked()) {
            switchViewToBeLiked();
            return;
        }

        switchViewToBeLiked();
        MessageSendingUtils.sendLike(sender, receiver);



    }

    /**
     * what the view is going to display if the sender hits the like
     * 1. set the background of the button to be ic_liked
     * 2. set the button to be unclickable
     * 3. set the listener of the button to be null
     */
    protected void switchViewToBeLiked() {

    }




}
