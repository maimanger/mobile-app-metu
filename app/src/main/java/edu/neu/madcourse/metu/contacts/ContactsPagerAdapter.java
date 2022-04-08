package edu.neu.madcourse.metu.contacts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class ContactsPagerAdapter extends FragmentPagerAdapter {

    public ContactsPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // TODO: pass contacts list into Fragment
        switch (position) {
            case 0:
                return new FriendFragment();
            case 1:
                return new MetFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
