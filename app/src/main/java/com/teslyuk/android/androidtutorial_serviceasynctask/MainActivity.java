package com.teslyuk.android.androidtutorial_serviceasynctask;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.teslyuk.android.androidtutorial_serviceasynctask.model.Point;
import com.teslyuk.android.androidtutorial_serviceasynctask.model.Way;
import com.teslyuk.android.androidtutorial_serviceasynctask.service.AutoStartService;
import com.teslyuk.android.androidtutorial_serviceasynctask.service.BubbleService;
import com.teslyuk.android.androidtutorial_serviceasynctask.service.LocationService;

import java.util.Calendar;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    private Button mStartButton;
    private Button mStopButton;
    private Button mBindButton;
    private Button mUnbindButton;
    private Button mStartBubbleServiceBtn;

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
        handler = new Handler();
        checkLocationPermission();
    }

    private void initView() {
        mStartButton = findViewById(R.id.btn_start);
        mStopButton = findViewById(R.id.btn_stop);
        mBindButton = findViewById(R.id.btn_bind);
        mUnbindButton = findViewById(R.id.btn_unbind);

        mTotalDistanceText = findViewById(R.id.tv_total_distance);
        mDirectDistanceText = findViewById(R.id.tv_direct_distance);
        mStartBubbleServiceBtn = findViewById(R.id.activity_main_start_bubble_service_btn);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mStartBubbleServiceBtn.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mBindButton.setOnClickListener(this);
        mUnbindButton.setOnClickListener(this);
        mStartBubbleServiceBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initLocationService();
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
            case R.id.activity_main_start_bubble_service_btn:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 0);
                    } else {
                        startService(new Intent(MainActivity.this, BubbleService.class));
                    }
                }
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

    private void lockServiceControls() {
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(false);
        mBindButton.setEnabled(false);
        mUnbindButton.setEnabled(false);
    }

    //Service
    private void initLocationService() {
        //don't run location service if there is no right permission
        if (hasLocationPermissions()) {
            initServiceConnection();
            intent = new Intent(MainActivity.this, LocationService.class);
            startService(intent);
            bindMonitorService();
        }
    }

    private void initServiceConnection() {
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

    //Location permissions

    public boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkLocationPermission() {
        if (!hasLocationPermissions()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRequestPermissionDialog();
            } else {
                // No explanation needed, we can request the permission.
                Log.d(TAG, "requestPermissions ACCESS_FINE_LOCATION");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void showRequestPermissionDialog() {
        Log.d(TAG, "showRequestPermissionDialog");
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_location_permission)
                .setMessage(R.string.text_location_permission)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "PERMISSION_GRANTED");
                    initLocationService();
                } else {
                    Log.d(TAG, "! PERMISSION_GRANTED");
                    lockServiceControls();
                }
                return;
            }

        }
    }
}
