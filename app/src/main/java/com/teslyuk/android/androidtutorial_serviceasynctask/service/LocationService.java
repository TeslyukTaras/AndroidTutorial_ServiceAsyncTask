package com.teslyuk.android.androidtutorial_serviceasynctask.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.teslyuk.android.androidtutorial_serviceasynctask.model.Point;
import com.teslyuk.android.androidtutorial_serviceasynctask.model.Way;
import com.teslyuk.android.androidtutorial_serviceasynctask.utils.LocationListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by taras.teslyuk on 12/9/15.
 */
public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;

    public static final long CHECK_INTERVAL = 5 * 1000; // x seconds

    /**
     * interface for clients that bind
     */
    IBinder mBinder = new MyBinder();

    /**
     * indicates whether onRebind should be used
     */
    boolean mAllowRebind;

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private Way way;

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
//            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        Log.d(TAG, "Service binned");
        mAllowRebind = false;
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();

        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new CheckByIntervalTask(), 0, CHECK_INTERVAL);

        way = new Way();
    }

    class CheckByIntervalTask extends TimerTask {
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    checkLocation();
                }
            });
        }
    }

    private void checkLocation() {
//        Log.d(TAG, "checkLocation");
        if (mLocationListeners[0].getmLastLocation() != null /*||
                mLocationListeners[1].getmLastLocation() != null*/) {
//            Log.d(TAG, "mLocationListeners[0].isChanged(): " + mLocationListeners[0].isChanged() + " mLocationListeners[0].getmLastLocation(): " + mLocationListeners[0].getmLastLocation());
//            Log.d(TAG, "mLocationListeners[1].isChanged(): " + mLocationListeners[1].isChanged() + " mLocationListeners[1].getmLastLocation(): " + mLocationListeners[1].getmLastLocation());

            Location location = mLocationListeners[0].isChanged() ? mLocationListeners[0].getmLastLocation() : null;
//            if(location == null) location = mLocationListeners[1].isChanged() ? mLocationListeners[1].getmLastLocation() : null;

            if (location != null) {
                mLocationListeners[0].setChanged(false);
//                mLocationListeners[1].setChanged(false);

                Point point = new Point(location.getLatitude(), location.getLongitude());
                Log.d(TAG, " add point: lat: " + point.lat + " lon: " + point.lon);
                way.addPoint(point);
            }
        }
    }

    public Way getWay() {
        return way;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

//        try {
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.NETWORK_PROVIDER, LocationListener.LOCATION_INTERVAL, LocationListener.LOCATION_DISTANCE,
//                    mLocationListeners[1]);
//        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
//        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
//        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LocationListener.LOCATION_INTERVAL, LocationListener.LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    public class MyBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }
}