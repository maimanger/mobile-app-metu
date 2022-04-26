package edu.neu.madcourse.metu.explore.daos;

public class PreferenceSetting {
    private String userId;
    private String locationPreference;
    private int genderPreference;
    private float ageMin;
    private float ageMax;

    public PreferenceSetting() {
        ageMax = 25;
        ageMin = 70;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationPreference() {
        return locationPreference;
    }

    public void setLocationPreference(String locationPreference) {
        this.locationPreference = locationPreference;
    }

    public int getGenderPreference() {
        return genderPreference;
    }

    public void setGenderPreference(int genderPreference) {
        this.genderPreference = genderPreference;
    }

    public float getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(float ageMin) {
        this.ageMin = ageMin;
    }

    public float getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(float ageMax) {
        this.ageMax = ageMax;
    }
}