package edu.neu.madcourse.metu.contacts;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.stream.Collectors;

public class ContactsPagerAdapter extends FragmentStateAdapter {
    private String[] tabTitles = new String[] {"Friends", "Mets"};
    private ContactsActivity context;


    public ContactsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        context = (ContactsActivity)fragmentActivity;
    }

    public void setContext(ContactsActivity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ContactsFragment(
                        context.getContactsList().stream()
                        .filter(contact -> contact.getConnectionPoint() > 0)
                        .collect(Collectors.toList()));
            default:
                return new ContactsFragment(
                        context.getContactsList().stream()
                        .filter(contact -> contact.getConnectionPoint() == 0)
                        .collect(Collectors.toList()));
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }

    public String getTabTitle(int position) {
        return tabTitles[position];
    }
}
