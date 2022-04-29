package edu.neu.madcourse.metu.explore;

import android.util.Log;

import java.util.Comparator;

import edu.neu.madcourse.metu.explore.daos.PreferenceSetting;
import edu.neu.madcourse.metu.explore.daos.RecommendedUser;
import edu.neu.madcourse.metu.utils.Constants;

public class RecommendedUserComparator implements Comparator<RecommendedUser> {
    private boolean maleSelected;
    private boolean femaleSelected;
    private boolean otherSelected;
    private String location;
    private float ageMin;
    private float ageMax;

    public RecommendedUserComparator(PreferenceSetting preferenceSetting) {
        maleSelected = preferenceSetting == null? true:preferenceSetting.maleSelected();
        femaleSelected = preferenceSetting == null? true:preferenceSetting.femaleSelected();
        otherSelected = preferenceSetting == null? true:preferenceSetting.otherSelected();

        // age
        ageMax = preferenceSetting == null? 100:preferenceSetting.getAgeMax();
        ageMin = preferenceSetting == null? 100:preferenceSetting.getAgeMin();

        // todo: location
        location = preferenceSetting == null? null: preferenceSetting.getLocationPreference();

    }

    @Override
    public int compare(RecommendedUser user1, RecommendedUser user2) {
        int s1 = getScore(user1);
        int s2 = getScore(user2);
        if (s1 > s2) {
            return -1;
        } else if (s1 == s2 && user1.getLastLoginTime() > user2.getLastLoginTime())
            return -1;
        else {
            return 1;
        }
    }

    private int getScore(RecommendedUser user) {
        int score = 0;
        // if the user has been liked
        if (user.getIsLiked()) {
            score -= 200;
        }

        // if gender not matches -70 points
        if ((user.getGender() == Constants.GENDER_MALE_INT && !maleSelected)
                || (user.getGender() == Constants.GENDER_FEMALE_INT && !femaleSelected)
                || (user.getGender() == Constants.GENDER_UNDEFINE_INT && !otherSelected)
        ) {
            score -= 100;
            Log.d("comparator", user.getNickname() + " gender not matched -70: " + user.getGender());
        } else {
            Log.d("comparator", user.getNickname() + " gender matched " + user.getGender());
        }
        // if location matches +20 points
        if (location != null && location.length() > 0 && location.equals(user.getLocation())) {
            score += 20;
            Log.d("comparator", user.getNickname() + " location matched +20: " + user.getLocation() );
        } else {
            Log.d("comparator", user.getNickname() + " location not matched" + user.getLocation() );
        }

        // if age matches perfectly +15 points
        // if age within 5 years old ranges +5
        if (ageMin <= user.getAge() && user.getAge() <= ageMax) {
            score += 15;
            Log.d("comparator", user.getNickname() + " age matched well +15: " + user.getAge());
        } else {
            float diff = Math.min(Math.abs(user.getAge() - ageMin), Math.abs(user.getAge() - ageMax));

            if (diff <= 5) {
                Log.d("comparator", user.getNickname() + " age matched ok +5: " + user.getAge());
                score += 2;
            } else if (diff <= 10) {
                score -= 5;
            } else if (diff <= 20) {
                score -= 50;
            } else {
                score -= 70;
            }
        }

        Log.d("comparator", user.getNickname() + "'s final score: " + score);
        return score;
    }
}
