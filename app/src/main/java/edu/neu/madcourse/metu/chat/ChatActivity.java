package edu.neu.madcourse.metu.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.BitmapUtils;
import edu.neu.madcourse.metu.utils.FakeDatabase;

public class ChatActivity extends AppCompatActivity {
    private User receiverUser;
    private String username;

    // UI components
    private ProgressBar progressBar;
    private FrameLayout sendButton;
    private EditText inputMessage;
    private AppCompatImageView backButton;
    private AppCompatImageView moreInfoButton;
    private TextView receiverName;

    // recycler view
    private RecyclerView chatHistory;
    private List<ChatItem> chatItemList;
    private ChatHistoryAdapter chatHistoryAdapter;

    // for multi threading
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // initialize the executor service
        executorService = Executors.newFixedThreadPool(1);

        // initialize UI components
        progressBar = findViewById(R.id.progressBarChatActivity);
        // set the progress bar to be visible
        progressBar.setVisibility(View.VISIBLE);

        sendButton = findViewById(R.id.layoutSend);
        inputMessage = findViewById(R.id.inputMessage);
        backButton = findViewById(R.id.imageBack);
        moreInfoButton = findViewById(R.id.imageMore);
        receiverName = findViewById(R.id.contactName);

        // init the current username
        loadUsername();

        // initialize chat item list
        chatItemList = new ArrayList<>();

        // set listeners
        setListeners();

        // init data view
        init(savedInstanceState);

    }

    /**
     * load the username of current user.
     */
    private void loadUsername() {
        this.username = getSharedPreferences("METU_APP", MODE_PRIVATE)
                .getString("USERNAME", "");
    }

    /**
     * retrieve the current username, receiver and chat history.
     * @param savedInstanceState
     */
    private void init(Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey("SIZE")) {
            // retrieve from savedInstanceState
            // retrieve the current receiver
            this.receiverUser = (User) savedInstanceState.getSerializable("RECEIVER");
            receiverName.setText(receiverUser.getUsername());
            // get the size of chat items
            int size = savedInstanceState.getInt("SIZE");
            // retrieve the chat items
            ChatItem chatItem;
            for (int i = 0; i < size; i++) {
                chatItem = (ChatItem) savedInstanceState.getSerializable("CHAT_ITEM" + i);
                this.chatItemList.add(chatItem);
            }

            // init the recycler view
            initRecyclerView();
            // dismiss the progress bar
            progressBar.setVisibility(View.GONE);

        } else {
            // get data from intent
            Bundle extras = getIntent().getExtras();
            // get current receiver
            this.receiverUser = (User) extras.getSerializable("RECEIVER");

            receiverName.setText(receiverUser.getUsername());
            // todo: fetch chat history data from database
            // todo: fetch chat history in background thread
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    // onPreExecute
                    // doInBackground
                    fetchData();

                    // onPostExecute
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initRecyclerView();
                            // dismiss the progress bar
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            });

        }

    }

    private void initRecyclerView() {
        // initialize recycler view and set layout manager
        chatHistory = findViewById(R.id.chatHistory);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatHistory.setLayoutManager(linearLayoutManager);

        // initialize and set the adapter
        Bitmap avatar = null;
        if (receiverUser.getAvatar() != null) {
            avatar = BitmapUtils.getBitmapFromString(receiverUser.getAvatar());
        }
        chatHistoryAdapter = new ChatHistoryAdapter(this, this.chatItemList, this.username, avatar);
        chatHistory.setAdapter(chatHistoryAdapter);
    }

    /**
     * fetch chat history from database.
     */
    private void fetchData() {
        List<ChatItem> chatHistory = FakeDatabase.getChatHistory();

        for (ChatItem chatItem: chatHistory) {
            this.chatItemList.add(chatItem);
        }
    }

    private void setListeners() {
        // back button
        backButton.setOnClickListener(v -> onBackPressed());

        // send button
        sendButton.setOnClickListener(v -> sendMessage());

        // todo: more info button
    }

    private Bitmap getBitmapFromString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void sendMessage() {
        String inputText = inputMessage.getText().toString();
        // trim the white space
        inputText = inputText.trim();
        if (inputText != null && inputText.length() > 0) {
            ChatItem message = new ChatItem();
            message.setSender(this.username);
            message.setReceiver(this.receiverUser.getUsername());
            message.setTimeStamp(System.currentTimeMillis());
            message.setMessage(inputText);
            this.chatItemList.add(message);
            // todo: call the api to actually send this message
            // notify the dataset changes
            this.chatHistoryAdapter.notifyItemChanged(this.chatItemList.size() - 1);
            // scroll to the end
            chatHistory.smoothScrollToPosition(chatItemList.size() - 1);
        }
        // set the text back to null
        this.inputMessage.setText(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // get the size of current chat history
        int size = this.chatItemList == null ? 0 : this.chatItemList.size();
        // save the size
        outState.putInt("SIZE", size);
        // save chat items
        for (int i = 0; i < size; i++) {
            outState.putSerializable("CHAT_ITEM" + i, this.chatItemList.get(i));
        }
        // save current receiver
        outState.putSerializable("RECEIVER", this.receiverUser);
    }
}