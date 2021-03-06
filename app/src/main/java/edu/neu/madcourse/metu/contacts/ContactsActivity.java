package edu.neu.madcourse.metu.contacts;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.BaseCalleeActivity;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.SettingActivity;
import edu.neu.madcourse.metu.models.Contact;
import edu.neu.madcourse.metu.chat.RecentConversationActivity;
import edu.neu.madcourse.metu.explore.ExploringActivity;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.profile.UserProfileActivity;
import edu.neu.madcourse.metu.service.FirebaseService;


public class ContactsActivity extends BaseCalleeActivity {

    private ContactsPagerAdapter contactsPagerAdapter;
    private ViewPager2 contactsViewPager;
    private TabLayout contactsTabs;
    private ProgressBar loadingProgress;
    private Handler handler = new Handler();
    BottomNavigationView bottomNavigationView;

    private List<Contact> contactsList;

    private ImageView setting;

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

        // actionbar
        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Contacts");

        // bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navi);
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

        // setting
        setting = findViewById(R.id.btn_contacts_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactsActivity.this, SettingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }



    private void renderContactsPager() {
        runOnUiThread(() -> {
            if (contactsPagerAdapter == null) {
                contactsPagerAdapter = new ContactsPagerAdapter(ContactsActivity.this);
                contactsViewPager.setAdapter(contactsPagerAdapter);
                new TabLayoutMediator(contactsTabs, contactsViewPager,
                        (tab, position) ->
                                tab.setText(contactsPagerAdapter.getTabTitle(position)))
                        .attach();
            } else {
                contactsViewPager.setAdapter(contactsPagerAdapter);
            }

            loadingProgress.setVisibility(ProgressBar.INVISIBLE);
        });
    }

    private void initFromFetching() {
        if (loadingProgress.getVisibility() == View.INVISIBLE) {
            runOnUiThread(() -> {
                loadingProgress.setVisibility(View.VISIBLE);
            });
        }

        loginUser = ((App)getApplication()).getLoginUser();
        if (loginUser != null) {
            String myUserId = loginUser.getUserId();
            Map<String, Boolean> myConnections = loginUser.getConnections();

            new Thread(() -> {
                // Fetch Contacts from Firebase
                FirebaseService.getInstance().fetchContacts(myUserId, myConnections,
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
    }

    public List<Contact> getContactsList() {
        return contactsList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.menu_contacts);
        initFromFetching();
    }


    @Override
    protected void onStop() {
        super.onStop();
        contactsViewPager.setAdapter(null);
        loadingProgress.setVisibility(ProgressBar.VISIBLE);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int size = contactsList == null ? 0 : contactsList.size();
        outState.putInt("SIZE", size);
        for (int i = 0; i < size; i++) {
            outState.putParcelable("CONTACT" + i, contactsList.get(i));
        }
    }


    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {
        super.onPeersOnlineStatusChanged(map);
        boolean changed = false;
        synchronized (this) {
            for (Contact contact : contactsList) {
                if (map.containsKey(contact.getContactUserId())) {
                    contact.setOnline(map.get(contact.getContactUserId()) == 0);
                    changed = true;
                }
            }
        }
        if (changed) {
            runOnUiThread(() -> {
                contactsViewPager.setAdapter(null);
                contactsViewPager.setAdapter(contactsPagerAdapter);
            });
        }
    }

    @Override
    protected void refreshAppLoginUser() {
        super.refreshAppLoginUser();
        initFromFetching();
    }
}