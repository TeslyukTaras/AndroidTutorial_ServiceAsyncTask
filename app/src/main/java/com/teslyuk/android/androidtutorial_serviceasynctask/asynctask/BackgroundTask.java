package com.teslyuk.android.androidtutorial_serviceasynctask.asynctask;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by taras.teslyuk on 12/9/15.
 */
public class BackgroundTask extends AsyncTask<String, String, String> {
    private static final String TAG = BackgroundTask.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute");
    }

    @Override
    protected String doInBackground(String... args) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 100; i++) {
            builder.append("i: " + i + " i^2: " + (i * i));
        }

        return builder.toString();
    }

    protected void onPostExecute(String args) {
        super.onPostExecute(args);
        Log.d(TAG, "onPostExecute args: " + args);
    }
}