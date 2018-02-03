package com.teslyuk.android.androidtutorial_serviceasynctask.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taras.teslyuk on 12/9/15.
 */
public class Way {
    private static final String TAG = Way.class.getSimpleName();

    private List<Point> points;
    private double totalDistance;//total way
    private double directDistance;// distance between start point and current point

    public Way() {
        points = new ArrayList<>();
        totalDistance = 0;
        directDistance = 0;
    }

    public void addPoint(Point point) {
        points.add(point);
        if (points.size() >= 2) {
            totalDistance += getDistanceBetween2Points(points.get(points.size() - 2), point);
            directDistance = getDistanceBetween2Points(points.get(0), point);
        }
    }

    private double getDistanceBetween2Points(Point from, Point to) {
        double R = 6371 * 1000; // Radius of the earth in m
        double dLat = deg2rad(to.lat - from.lat);  // deg2rad below
        double dLon = deg2rad(to.lon - to.lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(from.lat)) * Math.cos(deg2rad(to.lat)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in m
        Log.d(TAG, "+distance: " + d);
        return d;
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

    public List<Point> getPoints() {
        return points;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getDirectDistance() {
        return directDistance;
    }
}
