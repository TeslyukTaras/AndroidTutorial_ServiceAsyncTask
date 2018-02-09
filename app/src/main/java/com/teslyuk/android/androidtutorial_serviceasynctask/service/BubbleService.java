package com.teslyuk.android.androidtutorial_serviceasynctask.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.teslyuk.android.androidtutorial_serviceasynctask.R;

/**
 * @author Maksym Syniutka
 *         Date: 30-Jan-18
 *         Time: 7:06 PM
 */

public class BubbleService extends Service {
    private WindowManager mWindowManager;
    private View mRootView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                //Be careful as width and height parameters will override width and height values
                //determined withing your layout file, in our case, in bubble_layout.xml
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
                getResources().getDimensionPixelOffset(R.dimen.bubble_view_width),
                getResources().getDimensionPixelOffset(R.dimen.bubble_view_height),
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT
        );
        params.dimAmount = 0.7f;
        params.gravity = Gravity.CENTER;

        mRootView = LayoutInflater.from(this)
                .inflate(R.layout.bubble_layout, null, false);

        ImageButton closeBubbleBtn = mRootView.findViewById(R.id.bubble_layout_remove_view_btn);
        closeBubbleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeView();
                stopSelf();
            }
        });

//        view.bringToFront();
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                System.out.println();
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                        params.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);

                        mWindowManager.updateViewLayout(view, params);
                        return true;
                    default:
                        return false;
                }
            }
        });
        mWindowManager.addView(mRootView, params);
    }

    @Override
    public void onDestroy() {
        removeView();
        super.onDestroy();
    }

    private void removeView() {
        //Catching exception because view could have already been removed from windowManager.
        try {
            mWindowManager.removeViewImmediate(mRootView);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
