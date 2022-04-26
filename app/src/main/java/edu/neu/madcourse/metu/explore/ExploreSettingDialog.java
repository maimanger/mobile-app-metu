package edu.neu.madcourse.metu.explore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.explore.daos.PreferenceSetting;
import edu.neu.madcourse.metu.utils.Constants;

public class ExploreSettingDialog extends AppCompatDialogFragment {
    private String userId;
    // todo: location
    private ProgressBar progressBar;
    private RangeSlider ageRangeSlider;
    private CheckBox womanSelected;
    private CheckBox manSelected;
    private CheckBox otherSelected;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // load username
        loadUser(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.explore_setting_dialog, null);

        builder.setView(view)
                .setTitle("Explore Settings")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveSettings(view);
                    }
                });

        progressBar = view.findViewById(R.id.progressBarExploreSetting);
        progressBar.setVisibility(View.VISIBLE);
        ageRangeSlider = view.findViewById(R.id.ageRangeSlider);
        womanSelected = view.findViewById(R.id.genderWoman);
        manSelected = view.findViewById(R.id.genderMan);
        otherSelected = view.findViewById(R.id.genderMore);

        
        // read previous setting
        fetchCurrentSetting();

        return builder.create();
    }
    
    private void fetchCurrentSetting() {
        FirebaseDatabase.getInstance().getReference(Constants.EXPLORE_SETTINGS_STORE)
                .orderByChild(Constants.USER_USER_ID)
                .equalTo(userId)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot setting: snapshot.getChildren()) {
                                // todo: location
                                String location = setting.child(Constants.EXPLORE_SETTING_LOCATION).getValue(String.class);
                                // 0: only male checked
                                // 1: only female checked
                                // 2: only other checked
                                // 3: male and female
                                // 4: male and other
                                // 5: female and other
                                // 6: all
                                if (setting.hasChild(Constants.EXPLORE_SETTING_GENDER)) {
                                    int gender = setting.child(Constants.EXPLORE_SETTING_GENDER).getValue(int.class);
                                    switch (gender) {
                                        case 0:
                                            womanSelected.setChecked(false);
                                            manSelected.setChecked(true);
                                            otherSelected.setChecked(false);
                                            break;
                                        case 1:
                                            womanSelected.setChecked(true);
                                            manSelected.setChecked(false);
                                            otherSelected.setChecked(false);
                                            break;
                                        case 2:
                                            womanSelected.setChecked(false);
                                            manSelected.setChecked(false);
                                            otherSelected.setChecked(true);
                                            break;
                                        case 3:
                                            womanSelected.setChecked(true);
                                            manSelected.setChecked(true);
                                            otherSelected.setChecked(false);
                                            break;
                                        case 4:
                                            womanSelected.setChecked(false);
                                            manSelected.setChecked(true);
                                            otherSelected.setChecked(true);
                                            break;
                                        case 5:
                                            womanSelected.setChecked(true);
                                            manSelected.setChecked(false);
                                            otherSelected.setChecked(true);
                                            break;
                                        case 6:
                                            womanSelected.setChecked(true);
                                            manSelected.setChecked(true);
                                            otherSelected.setChecked(true);
                                            break;
                                        default:
                                            womanSelected.setChecked(false);
                                            manSelected.setChecked(false);
                                            otherSelected.setChecked(false);
                                            break;
                                    }
                                }

                                if (setting.hasChild(Constants.EXPLORE_SETTING_AGE_MIN)
                                        && setting.hasChild(Constants.EXPLORE_SETTING_AGE_MAX)) {
                                    float ageMin = setting.child(Constants.EXPLORE_SETTING_AGE_MIN).getValue(float.class);
                                    float ageMax = setting.child(Constants.EXPLORE_SETTING_AGE_MAX).getValue(float.class);

                                    if (ageMin >= 18 && ageMax <= 100 && ageMin <= ageMax) {
                                        ageRangeSlider.setValues(ageMin, ageMax);
                                    }


                                }

                                progressBar.setVisibility(View.GONE);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUser(Bundle bundle) {
        // load userId
        this.userId = getArguments().getString("USER_ID");
    }

    private void saveSettings(View view) {
        if (userId == null) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        // todo: location
        PreferenceSetting setting = new PreferenceSetting();
        setting.setUserId(userId);
        List<Float> values = ageRangeSlider.getValues();
        if (values.size() == 2) {
            float ageMin = values.get(0);
            float ageMax = values.get(1);

            setting.setAgeMin(ageMin);
            setting.setAgeMax(ageMax);
        }

        boolean womanChecked = womanSelected.isChecked();
        boolean manChecked = manSelected.isChecked();
        boolean moreChecked = otherSelected.isChecked();

        int genderPreference = 7;

        if (manChecked && !womanChecked && !moreChecked) {
            genderPreference = Constants.EXPLORE_MAN_ONLY;
        } else if (!manChecked && womanChecked && !moreChecked) {
            genderPreference = Constants.EXPLORE_WOMAN_ONLY;
        } else if (!manChecked && !womanChecked && moreChecked) {
            genderPreference = Constants.EXPLORE_OTHER_ONLY;
        } else if (manChecked && womanChecked && !moreChecked) {
            genderPreference = Constants.EXPLORE_MAN_WOMAN;
        } else if (manChecked && !womanChecked && moreChecked) {
            genderPreference = Constants.EXPLORE_MAN_OTHER;
        } else if (!manChecked && womanChecked && moreChecked) {
            genderPreference = Constants.EXPLORE_WOMAN_OTHER;
        } else if (manChecked && womanChecked && moreChecked) {
            genderPreference = Constants.EXPLORE_ALL;
        }

        setting.setGenderPreference(genderPreference);

        // save the setting
        FirebaseDatabase.getInstance().getReference(Constants.EXPLORE_SETTINGS_STORE)
                .child(userId)
                .setValue(setting)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(view.getContext(), "Gotcha!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(view.getContext(), "OOOPS! Something went wrong...Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
