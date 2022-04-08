package edu.neu.madcourse.metu.contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import edu.neu.madcourse.metu.R;

public class ContactsActivity extends AppCompatActivity {

    private ContactsPagerAdapter contactsPagerAdapter;
    private ViewPager contactsViewPager;
    private TabLayout contactsTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactsViewPager = findViewById(R.id.viewpager_contacts);
        // TODO: pass contacts list into Adapter, to render different fragments
        contactsPagerAdapter = new ContactsPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        contactsViewPager.setAdapter(contactsPagerAdapter);

        contactsTabs = findViewById(R.id.tabLayout_contacts);
        contactsTabs.setupWithViewPager(contactsViewPager);
        contactsTabs.getTabAt(0).setText("Friends");
        contactsTabs.getTabAt(1).setText("Mets");
    }
}