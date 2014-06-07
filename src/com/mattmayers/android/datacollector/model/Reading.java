package com.mattmayers.android.datacollector.model;

/**
 * Created by matt on 6/6/14.
 */
public class Reading {
    public long millis;
    public float pressure;
    public transient Altitude altitude;

    public Reading() {
    }

    public Reading(long millis, float pressure) {
        this.millis = millis;
        this.pressure = pressure;
    }

    public Reading(long millis, float pressure, Altitude altitude) {
        this(millis, pressure);
        this.altitude = altitude;
    }
}
