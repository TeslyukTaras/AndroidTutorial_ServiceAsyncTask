package com.teslyuk.android.androidtutorial_serviceasynctask.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.teslyuk.android.androidtutorial_serviceasynctask.asynctask.BackgroundTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by taras.teslyuk on 25.07.2014.
 */
public class AutoStartService extends Service {
    private static final String TAG = AutoStartService.class.getSimpleName();

    public static boolean ServiceEnable;
    public static int ServicePeriod = 120000;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Toast.makeText(getApplicationContext(), "Service Created", Toast.LENGTH_LONG).show();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service Destroy", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Service onStartCommand ", Toast.LENGTH_LONG).show();
        makeTestTask();
        return super.onStartCommand(intent, flags, startId);
    }

    private void makeTestTask() {
        Log.d(TAG, "make test task");
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.execute("parameter!");
    }

}
