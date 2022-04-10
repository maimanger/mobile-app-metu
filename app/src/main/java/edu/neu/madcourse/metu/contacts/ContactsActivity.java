package edu.neu.madcourse.metu.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.metu.R;

public class ContactsActivity extends AppCompatActivity {

    private ContactsPagerAdapter contactsPagerAdapter;
    private ViewPager2 contactsViewPager;
    private TabLayout contactsTabs;
    private ProgressBar loadingProgress;
    private Handler handler = new Handler();

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

        initContactsListFromBundle(savedInstanceState);
        initContactsPager();
    }


    private void initContactsPager() {
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
                                (tab, position) -> tab.setText(contactsPagerAdapter.getTabTitle(position))).attach();
                    }
                });
            }
        }).start();
    }

    private void initContactsListFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("SIZE")) {
            int size = savedInstanceState.getInt("SIZE");
            contactsList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Contact contact = (Contact) savedInstanceState.getParcelable("CONTACT" + i);
                contactsList.add(contact);
            }
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
                    i);
            contactsList.add(newFriend);
        }
        for (int i = 0; i < 20; i++) {
            Contact newMet = new Contact(
                    new Timestamp(System.currentTimeMillis()).toString(),
                    i + "@abc.com",
                    "Met Name" + i,
                    i % 2 == 0,
                    0);
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
    protected void onStop() {
        super.onStop();
        contactsPagerAdapter = null;
        contactsViewPager.setAdapter(null);
        contactsViewPager.removeAllViews();
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
}