package edu.neu.madcourse.metu.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class LocatorService extends Service {

    private final String TAG = getClass().getSimpleName();
    private boolean finished = false;
    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String countryName;
    private String cityName;
    private String stateName;
    private String userId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        userId = intent.getStringExtra("USER_ID");

        // Check previous permission
        int coarseLocationPermission = ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int fineLocationPermission = ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (coarseLocationPermission == PackageManager.PERMISSION_GRANTED ||
                fineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            locationRequest = LocationRequest.create();
            /*locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);*/
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        Location lastLocation = locationResult.getLastLocation();

                        // Retrieve coordinates
                        double latitude = lastLocation.getLatitude();
                        double longitude = lastLocation.getLongitude();

                        // Retrieve location
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (addresses != null && addresses.size() > 0) {
                            countryName = addresses.get(0).getCountryName();
                            cityName = addresses.get(0).getLocality();
                            stateName = addresses.get(0).getAdminArea();

                            // Update Firebase latest location
                            Map<String, String> locationMap = new HashMap<>();
                            locationMap.put("country", countryName);
                            locationMap.put("state", stateName);
                            locationMap.put("city", cityName);

                            FirebaseDatabase.getInstance().getReference().child("latestLocation")
                                    .child(userId).setValue(locationMap);
                            Log.d(TAG, "onStartCommand: Location fetched -- " + countryName + ", " + cityName);
                        }
                    }
                    finished = true;
                    Log.d(TAG, "onLocationResult: stopService");
                    stopService();
                }
            };

            getLocation();
        } else {
            Log.d(TAG, "onStartCommand: No location permissions, stopService");
            stopService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        /*Task<LocationSettingsResponse> settingsTask = checkLocationSetting();
        // Start updating location (Make sure all permissions and settings are checked)
        settingsTask.addOnSuccessListener((Executor) this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Stored in saved status, so that we can skip the permission and setting check,
                // and update locations directly onResume
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                        Looper.getMainLooper());
            }
        });
        settingsTask.addOnFailureListener((Executor) this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Location setting failed");
                stopSelf();
            }
        });*/

        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
    }

/*    private Task<LocationSettingsResponse> checkLocationSetting() {
        // check location settings
        LocationSettingsRequest.Builder settingRequestBuilder = new LocationSettingsRequest.Builder();
        settingRequestBuilder.addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getApplicationContext());
        return settingsClient.checkLocationSettings(settingRequestBuilder.build());
    }*/


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopService() {
        if (finished) {
            locationProviderClient.removeLocationUpdates(locationCallback);
        }
        stopSelf();
    }
}
