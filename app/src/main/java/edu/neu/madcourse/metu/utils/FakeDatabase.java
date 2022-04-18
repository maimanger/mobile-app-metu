package edu.neu.madcourse.metu.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.metu.models.ChatItem;
import edu.neu.madcourse.metu.contacts.daos.User;

public class FakeDatabase {
    private static List<ChatItem> chatItemList;
    public static User receiver;
    public static User sender;

    static {

        receiver = new User();
        receiver.setUsername("Harry Potter");

        sender = new User();
        sender.setUsername("Ron Weasley");

        chatItemList = new ArrayList<>();

        chatItemList.add(new ChatItem(sender.getUsername(), "So... so it’s true!", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(sender.getUsername(), "I mean, do you really have the?", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(receiver.getUsername(), "The what?", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(receiver.getUsername(), "The scar?", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(receiver.getUsername(), "Oh. Yeah.", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(sender.getUsername(), "Wicked!", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(receiver.getUsername(), "Bertie Bott’s Every Flavor Beans?", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(sender.getUsername(), "They mean every flavor.", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(sender.getUsername(), "There’s chocolate and peppermint and also...", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(sender.getUsername(), "...spinach, liver and tripe.", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(sender.getUsername(), "George sweared he got a booger-flavored one once.", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(receiver.getUsername(), "Are they real frogs?", System.currentTimeMillis()));
        chatItemList.add(new ChatItem(sender.getUsername(), "It’s a spell.", System.currentTimeMillis()));


        User user = new User();
        user.setUsername("Hermione Granger");

    }

    public static List<ChatItem> getChatHistory() {
        return chatItemList;
    }

    public static void addNewMessage(ChatItem newChatItem) {
        chatItemList.add(newChatItem);
    }

    public static String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
