package com.teslyuk.android.androidtutorial_serviceasynctask.model;

/**
 * Created by taras.teslyuk on 12/9/15.
 */
public class Point {
    public double lat, lon;

    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "lat: " + lat + " lon: " + lon;
    }
}
