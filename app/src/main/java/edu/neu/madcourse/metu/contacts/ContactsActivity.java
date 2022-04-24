package edu.neu.madcourse.metu.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.Connection;
import edu.neu.madcourse.metu.models.Contact;
import edu.neu.madcourse.metu.chat.ChatActivity;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.service.FirebaseService;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;


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
        fetchContactsList();

        //initContactsPager();

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




    private void renderContactsPager() {
        runOnUiThread(() -> {
            loadingProgress.setVisibility(ProgressBar.INVISIBLE);
            if (contactsPagerAdapter == null) {
                contactsPagerAdapter = new ContactsPagerAdapter(ContactsActivity.this);
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

    private void fetchContactsList() {
        String myUserId = ((App)getApplication()).getLoginUser().getUserId();
        Map<String, Boolean> myConnections = ((App)getApplication()).getLoginUser().getConnections();

        new Thread(() -> {
            // Fetch Contacts from Firebase
            FirebaseService.getInstance().fetchConnections(myUserId, myConnections,
                    (List<Contact> fetchedContacts) -> {
                        Log.d(TAG, "fetchContactsList: " + fetchedContacts.size());

                        // Initialize contactsList member
                        synchronized (this) {
                            contactsList = new ArrayList<>();
                            for (Contact c : fetchedContacts) {
                                contactsList.add(c);
                            }
                        }
                        renderContactsPager();

                        Set<String> contactsId = fetchedContacts.stream()
                                .map(Contact::getContactUserId).collect(Collectors.toSet());

                        // Subscribe contacts online status from Agora Rtm (Realtime update)
                        ((App)getApplication()).rtmSubscribePeer(contactsId);

                        // Query contacts online status from Agora Rtm (one time)
                        /*((App)getApplication()).queryPeerOnlineStatus(contactsId,
                                new ResultCallback<Map<String, Boolean>>() {
                                    @Override
                                    public void onSuccess(Map<String, Boolean> peerOnlineStatus) {
                                        Log.d(TAG, "onSuccess: query peers online status");

                                        // Initialize contactsList member
                                        contactsList = new ArrayList<>();
                                        for (Contact c : fetchedContacts) {
                                            c.setOnline(peerOnlineStatus.getOrDefault(
                                                    c.getContactUserId(), false));
                                            contactsList.add(c);
                                        }
                                        renderContactsPager();
                                    }

                                    @Override
                                    public void onFailure(ErrorInfo errorInfo) { }
                                });*/
                    });
        }).start();
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
        synchronized (this) {
            for (Contact contact : contactsList) {
                contact.setOnline(map.getOrDefault(contact.getContactUserId(), 2) == 0);
            }
        }
        runOnUiThread(() -> {
            contactsViewPager.setAdapter(null);
            contactsViewPager.setAdapter(contactsPagerAdapter);
        });

    }
}