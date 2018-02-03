package com.teslyuk.android.androidtutorial_serviceasynctask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.teslyuk.android.androidtutorial_serviceasynctask.model.Point;
import com.teslyuk.android.androidtutorial_serviceasynctask.model.Way;
import com.teslyuk.android.androidtutorial_serviceasynctask.service.AutoStartService;
import com.teslyuk.android.androidtutorial_serviceasynctask.service.LocationService;

import java.util.Calendar;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mStartButton;
    private Button mStopButton;
    private Button mBindButton;
    private Button mUnbindButton;

    private TextView mTotalDistanceText;
    private TextView mDirectDistanceText;

    private boolean isBound = false;
    private LocationService mMonitorService;
    private ServiceConnection serviceConnection;
    private Intent intent;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
        initService();

        handler = new Handler();
    }

    private void initView() {
        mStartButton = findViewById(R.id.btn_start);
        mStopButton = findViewById(R.id.btn_stop);
        mBindButton = findViewById(R.id.btn_bind);
        mUnbindButton = findViewById(R.id.btn_unbind);

        mTotalDistanceText = findViewById(R.id.tv_total_distance);
        mDirectDistanceText = findViewById(R.id.tv_direct_distance);
    }

    private void initListener() {
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mBindButton.setOnClickListener(this);
        mUnbindButton.setOnClickListener(this);
    }

    private void initService() {
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(TAG, "onServiceConnected");
                LocationService.MyBinder myBinder = (LocationService.MyBinder) binder;
                mMonitorService = myBinder.getService();
                isBound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                isBound = false;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
        bindMonitorService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDistanceChecker();
        startAutoStartService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDistanceChecker();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindMonitorService();
    }

    private void startAutoStartService() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        AutoStartService.ServiceEnable = sharedPrefs.getBoolean("perform_updates", true);
        AutoStartService.ServicePeriod = Integer.parseInt(sharedPrefs.getString("updates_interval", "300000"));

        if (AutoStartService.ServiceEnable) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 10);

            Intent startServiceIntent = new Intent(this, AutoStartService.class);

            PendingIntent pintent = PendingIntent.getService(this, 0, startServiceIntent, 0);

            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AutoStartService.ServicePeriod, pintent);

            startService(new Intent(this, AutoStartService.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.btn_start):
                startService(intent);
                break;
            case (R.id.btn_stop):
                stopService(intent);
                break;
            case (R.id.btn_bind):
                bindMonitorService();
                break;
            case (R.id.btn_unbind):
                unbindMonitorService();
                break;
            default:
                return;
        }

        Way way = mMonitorService.getWay();
        Log.d(TAG, way.getPoints().size() + "");
        for (Point point : way.getPoints()) {
            Log.d(TAG, point.toString());
        }
    }

    private void bindMonitorService() {
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void unbindMonitorService() {
        if (!isBound) {
            return;
        }
        unbindService(serviceConnection);
        isBound = false;
    }

    //balance checker
    Runnable mDistanceChecker = new Runnable() {
        @Override
        public void run() {
            updateDistance();
            //restart each updateInterval millis
            handler.postDelayed(mDistanceChecker, 1 * 1000);
        }
    };

    private void updateDistance() {
//        Log.d(TAG, "updateDistance: ");
        if (isBound) {
            Way way = mMonitorService.getWay();
//            Log.d(TAG, "update: " + way.getPoints().size());
            mTotalDistanceText.setText("total distance: " + way.getTotalDistance());
            mDirectDistanceText.setText("direct distance: " + way.getDirectDistance());
        }
    }

    void startDistanceChecker() {
        mDistanceChecker.run();
    }

    void stopDistanceChecker() {
        handler.removeCallbacks(mDistanceChecker);
    }
}
