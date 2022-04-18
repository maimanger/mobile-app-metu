package edu.neu.madcourse.metu.contacts;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.Contact;

public class ContactsFragment extends Fragment implements CustomedItemClickListener {
    private RecyclerView contactsRecyclerView;
    private ContactsAdapter contactsAdapter;
    private List<Contact> contactsList;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public ContactsFragment(List<Contact> contactsList) {
        this.contactsList = contactsList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactsRecyclerView = view.findViewById(R.id.recyclerView_contacts);
        contactsRecyclerView.setHasFixedSize(true);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        contactsAdapter = new ContactsAdapter(contactsList, this);
        contactsRecyclerView.setAdapter(contactsAdapter);
        return view;
    }

    @Override
    public void onClick(int position) {
        Contact clickedItem = contactsList.get(position);
        // TODO: Go to Profile Activity
        Toast.makeText(this.getContext(),
                "Clicked: Go to " + clickedItem.getContactName() + " Profile!",
                Toast.LENGTH_SHORT).show();
    }
}