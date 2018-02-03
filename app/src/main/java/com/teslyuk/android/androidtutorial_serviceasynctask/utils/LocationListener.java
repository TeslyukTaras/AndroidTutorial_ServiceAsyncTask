package com.teslyuk.android.androidtutorial_serviceasynctask.utils;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by taras.teslyuk on 12/9/15.
 */
public class LocationListener implements android.location.LocationListener {
    private static final String TAG = "LocationListener";

    public static final int LOCATION_INTERVAL = 1000;
    public static final float LOCATION_DISTANCE = 10f;

    private Location mLastLocation;
    private boolean changed = true;

    public LocationListener(String provider) {
        Log.e(TAG, "LocationListener " + provider);
        mLastLocation = new Location(provider);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged: " + location);
        mLastLocation.set(location);
        changed = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e(TAG, "onStatusChanged: " + provider);
    }

    public Location getmLastLocation() {
        if (Math.abs(mLastLocation.getLongitude()) > 0.000000001 &&
                Math.abs(mLastLocation.getLatitude()) > 0.000000001)
            return mLastLocation;
        else return null;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean isChanged) {
        this.changed = isChanged;
    }

}
