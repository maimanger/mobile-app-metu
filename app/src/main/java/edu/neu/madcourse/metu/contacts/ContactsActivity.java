package edu.neu.madcourse.metu.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.Contact;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.profile.UserProfileActivity;


public class ContactsActivity extends BaseCalleeActivity {

    private ContactsPagerAdapter contactsPagerAdapter;
    private ViewPager2 contactsViewPager;
    private TabLayout contactsTabs;
    private ProgressBar loadingProgress;
    private Handler handler = new Handler();
    private int lastPage = 0;
    BottomNavigationView bottomNavigationView;


    private List<Contact> contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactsViewPager = findViewById(R.id.viewpager_contacts);
        contactsTabs = findViewById(R.id.tabLayout_contacts);
        loadingProgress = findViewById(R.id.progressBar_loadingContacts);
        loadingProgress.setVisibility(ProgressBar.VISIBLE);
        contactsTabs.getTabAt(0).setText("Friends");
        contactsTabs.getTabAt(1).setText("Mets");

        initDataFromBundle(savedInstanceState);
        initContactsPager(savedInstanceState);

        // actionbar
        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Contacts");

        // bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navi);
        bottomNavigationView.setSelectedItemId(R.id.menu_contacts);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.menu_explore:
                        startActivity(new Intent(getApplicationContext(), ExploringActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_contacts:
                        return true;
                    case R.id.menu_chats:
                        startActivity(new Intent(getApplicationContext(), RecentConversationActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menu_me:
                        startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }




    private void initContactsPager(Bundle savedInstanceState) {
        new Thread(() -> {
            if (contactsList == null) {
                fetchContactsList();
            }
            if (contactsList != null) {
                handler.post(() -> {
                    loadingProgress.setVisibility(ProgressBar.INVISIBLE);
                    if (contactsPagerAdapter == null) {
                        contactsPagerAdapter = new ContactsPagerAdapter(this);
                        contactsViewPager.setAdapter(contactsPagerAdapter);
                        new TabLayoutMediator(contactsTabs, contactsViewPager,
                                (tab, position) ->
                                        tab.setText(contactsPagerAdapter.getTabTitle(position)))
                                .attach();
                    }
                    // Make sure scroll to the last viewing page before rotation
                    contactsViewPager.setCurrentItem(lastPage);
                });
            }
        }).start();
    }

    private void initDataFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("SIZE")) {
            int size = savedInstanceState.getInt("SIZE");
            contactsList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Contact contact = (Contact) savedInstanceState.getParcelable("CONTACT" + i);
                contactsList.add(contact);
            }
            lastPage = savedInstanceState.getInt("PAGE");
        }
    }

    // TODO: Implement database fetching
    private void fetchContactsList() {
        contactsList = new ArrayList<>();
        for (int i = 1; i < 21; i++) {
            Contact newFriend = new Contact(
                    new Timestamp(System.currentTimeMillis()).toString(),
                    i + "@abc.com",
                    "Friend Name" + i,
                    i % 2 == 0,
                    i,
                    "friendavatar.png");
            contactsList.add(newFriend);
        }
        for (int i = 0; i < 20; i++) {
            Contact newMet = new Contact(
                    new Timestamp(System.currentTimeMillis()).toString(),
                    i + "@abc.com",
                    "Met Name" + i,
                    i % 2 == 0,
                    0,
                    "metavatar.png");
            contactsList.add(newMet);
        }
        // TODO: Only for testing, will be deleted after implementing database fetching
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<Contact> getContactsList() {
        return contactsList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contactsPagerAdapter != null && contactsViewPager.getAdapter() == null) {
            contactsViewPager.setAdapter(contactsPagerAdapter);
            // Make sure scroll to the last viewing page before rotation
            contactsViewPager.setCurrentItem(lastPage);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Store the last viewing page before rotation
        lastPage = contactsViewPager.getCurrentItem();
    }


    @Override
    protected void onStop() {
        super.onStop();
        contactsViewPager.setAdapter(null);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int size = contactsList == null ? 0 : contactsList.size();
        outState.putInt("SIZE", size);
        for (int i = 0; i < size; i++) {
            outState.putParcelable("CONTACT" + i, contactsList.get(i));
        }
        outState.putInt("PAGE", lastPage);
    }


    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {
        super.onPeersOnlineStatusChanged(map);
    }
}