package edu.neu.madcourse.metu.contacts;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.models.Contact;
import edu.neu.madcourse.metu.utils.Utils;

public class ContactsAdapter  extends RecyclerView.Adapter<ContactsAdapter.ContactsHolder>{
    private List<Contact> contactsList;
    CustomedItemClickListener clickListener;

    public class ContactsHolder extends RecyclerView.ViewHolder{
        ShapeableImageView contactAvatar;
        ImageView onlineStatus;
        TextView contactName;
        ImageView friendStar;
        TextView friendLevel;

        public ContactsHolder(View itemView) {
            super(itemView);
            contactAvatar = itemView.findViewById(R.id.image_contactAvatar);
            onlineStatus = itemView.findViewById(R.id.image_onlineStatus);
            contactName = itemView.findViewById(R.id.text_contactName);
            friendStar = itemView.findViewById(R.id.image_friendStar);
            friendLevel = itemView.findViewById(R.id.text_friendLevel);

            itemView.setOnClickListener((View v) -> {
                int position = getLayoutPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ContactsAdapter.this.clickListener.onClick(position);
                }
            });
        }
    }

    public ContactsAdapter(List<Contact> contactsList, CustomedItemClickListener clickListener) {
        this.contactsList = contactsList;
        this.clickListener = clickListener;
    }


    @NonNull
    @Override
    public ContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_contact, parent, false);
        return new ContactsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsHolder holder, int position) {
        Contact currContact = contactsList.get(position);
        holder.contactName.setText(currContact.getContactName());
        holder.onlineStatus.setVisibility(currContact.isOnline() ? View.VISIBLE : View.INVISIBLE);
        //holder.contactAvatar.setImageResource(R.drawable.ic_user_avatar);
        //new Utils.DownloadImageTask(holder.contactAvatar).execute(currContact.getContactAvatarUri());
        Utils.loadImgUri(currContact.getContactAvatarUri(), holder.contactAvatar);

        int friendLevel = Utils.calculateFriendLevel(currContact.getConnectionPoint());
        initFriendLevelView(holder, friendLevel);
    }

    @Override
    public int getItemCount() {
        return contactsList == null ? 0 : contactsList.size();
    }


    private void initFriendLevelView(ContactsHolder holder, int friendLevel) {
        switch (friendLevel) {
            case 1:
                holder.friendStar.setColorFilter(Color.parseColor("#199ebd"));
                holder.friendStar.setVisibility(View.VISIBLE);
                holder.friendLevel.setText(Integer.toString(friendLevel));
                break;
            case 2:
                holder.friendStar.setColorFilter(Color.parseColor("#c0448f"));
                holder.friendStar.setVisibility(View.VISIBLE);
                holder.friendLevel.setText(Integer.toString(friendLevel));
                break;
            case 3:
                holder.friendStar.setColorFilter(Color.parseColor("#8c0161"));
                holder.friendStar.setVisibility(View.VISIBLE);
                holder.friendLevel.setText(Integer.toString(friendLevel));
                break;
            default:
                holder.friendStar.setVisibility(View.INVISIBLE);
                holder.friendLevel.setVisibility(View.INVISIBLE);
                break;
        }
    }

}
