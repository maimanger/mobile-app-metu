package edu.neu.madcourse.metu.explore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import edu.neu.madcourse.metu.App;
import edu.neu.madcourse.metu.R;
import edu.neu.madcourse.metu.explore.daos.PreferenceSetting;
import edu.neu.madcourse.metu.models.User;
import edu.neu.madcourse.metu.utils.Constants;

public class ExploreSettingDialog extends AppCompatDialogFragment {
    private String userId;
    private User loginUser;
    private boolean isFirstTime;

    private boolean isLocationPermitted;
    private ProgressBar progressBar;
    private RangeSlider ageRangeSlider;
    private CheckBox womanSelected;
    private CheckBox manSelected;
    private CheckBox otherSelected;
    private TextView title;
    private SwitchMaterial showPeopleNearMe;
    // activity
    private ExploringSettingDialogListener listener;

    private void checkPermission() {
        this.isLocationPermitted = RecommendationUtils.checkLocationPermission(getActivity().getApplicationContext());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (ExploringSettingDialogListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // load username
        loadUser(savedInstanceState);
        loadStyle(savedInstanceState);
        checkPermission();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.explore_setting_dialog, null);

        progressBar = new ProgressBar(getActivity().getApplicationContext());
        progressBar.setVisibility(View.VISIBLE);
        ageRangeSlider = view.findViewById(R.id.ageRangeSlider);
        womanSelected = view.findViewById(R.id.genderWoman);
        manSelected = view.findViewById(R.id.genderMan);
        otherSelected = view.findViewById(R.id.genderMore);
        title = view.findViewById(R.id.exploringSettingTitle);
        showPeopleNearMe = view.findViewById(R.id.isShowPeopleNearMe);

        if (isFirstTime) {
            title.setText("Set your preference and meet new friends!");
            builder.setView(view)
                    .setCancelable(false)
                    //.setTitle("Set your preference and meet new friends!")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PreferenceSetting setting = saveSettings(view);
                            listener.applyPreference(setting);
                            dialogInterface.cancel();
                            //getActivity().finish();
                            return;
                        }
                    });
        } else {
            title.setText("Edit your explore preference");
            builder.setView(view)
                    //.setTitle("Edit your explore preference")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PreferenceSetting setting = saveSettings(view);
                        }
                    });
        }

        // read previous setting
        fetchCurrentSetting();

        return builder.create();
    }

    private void showToast(String s) {
        Toast.makeText(getActivity().getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    
    private void fetchCurrentSetting() {
        if (!isLocationPermitted) {
            showPeopleNearMe.setChecked(false);
            showPeopleNearMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (compoundButton.isChecked()) {
                        showToast("Please grant METU permission to user your location");
                        compoundButton.setChecked(false);
                    }
                }
            });
        }

        if (isFirstTime) {
            // location
            showPeopleNearMe.setChecked(isLocationPermitted);

            // gender preference
            otherSelected.setChecked(true);
            manSelected.setChecked(true);
            womanSelected.setChecked(true);
            // age
            int age = loginUser.getAge();
            ageRangeSlider.setValues((float) Math.max(18, age-10), (float) Math.min(100, age+10));

            progressBar.setVisibility(View.GONE);
        } else {
            RecommendationUtils.fetchPreference(userId, (setting) -> {
                if (setting == null) {
                    showPeopleNearMe.setChecked(isLocationPermitted);

                    otherSelected.setChecked(true);
                    manSelected.setChecked(true);
                    womanSelected.setChecked(true);

                    int age = loginUser.getAge();

                    ageRangeSlider.setValues((float) Math.max(18, age-10), (float) Math.max(100, age+10));

                    progressBar.setVisibility(View.GONE);
                } else {
                    showPeopleNearMe.setChecked(isLocationPermitted && setting.getShowPeopleNearMe());

                    otherSelected.setChecked(setting.otherSelected());
                    manSelected.setChecked(setting.maleSelected());
                    womanSelected.setChecked(setting.femaleSelected());

                    float ageMin = setting.getAgeMin();
                    float ageMax = setting.getAgeMax();

                    if (ageMin >= 18 && ageMax <= 100 && ageMin <= ageMax) {
                        ageRangeSlider.setValues(ageMin, ageMax);
                    } else {
                        int age = loginUser.getAge();
                        ageRangeSlider.setValues((float) Math.max(18, age-10), (float) Math.max(100, age+10));
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void loadUser(Bundle bundle) {
        // load userId
        this.userId = getArguments().getString("USER_ID");
        this.loginUser = ((App) getActivity().getApplication()).getLoginUser();
    }

    private void loadStyle(Bundle bundle) {
        this.isFirstTime = getArguments().getBoolean("IS_FIRST_TIME");
    }

    private PreferenceSetting saveSettings(View view) {
        if (userId == null) {
            return null;
        }
        progressBar.setVisibility(View.VISIBLE);

        PreferenceSetting setting = new PreferenceSetting();
        setting.setUserId(userId);

        setting.setShowPeopleNearMe(showPeopleNearMe.isChecked());

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

        return setting;
    }

    public interface ExploringSettingDialogListener {
        void applyPreference(PreferenceSetting setting);
    }


}
