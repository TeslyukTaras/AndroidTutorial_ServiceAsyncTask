package com.teslyuk.android.androidtutorial_serviceasynctask.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teslyuk.android.androidtutorial_serviceasynctask.service.AutoStartService;

import java.util.Calendar;

public class AutoStartReceiver extends BroadcastReceiver {

    private static final String TAG = AutoStartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        AutoStartService.ServiceEnable = sharedPrefs.getBoolean("perform_updates", true);
        AutoStartService.ServicePeriod = Integer.parseInt(sharedPrefs.getString("updates_interval", "300000"));

        if(AutoStartService.ServiceEnable) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 10);

            Intent startServiceIntent = new Intent(context, AutoStartService.class);

            PendingIntent pintent = PendingIntent.getService(context, 0, startServiceIntent, 0);

            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AutoStartService.ServicePeriod, pintent);

            context.startService(new Intent(context, AutoStartService.class));
        }
    }

}