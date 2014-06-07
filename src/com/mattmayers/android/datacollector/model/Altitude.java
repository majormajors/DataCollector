package com.mattmayers.android.datacollector.model;

/**
 * Created by matt on 6/6/14.
 */
public class Altitude {
    private static final double METERS_IN_A_FOOT = 3.28084;

    private double altitudeInMeters;

    private Altitude(){
    }

    public static Altitude meters(double meters) {
        Altitude altitude = new Altitude();
        altitude.altitudeInMeters = meters;
        return altitude;
    }

    public static Altitude feet(double feet) {
        Altitude altitude = new Altitude();
        altitude.altitudeInMeters = feetToMeters(feet);
        return altitude;
    }

    private static double feetToMeters(double feet) {
        return feet / METERS_IN_A_FOOT;
    }

    private static double metersToFeet(double meters) {
        return meters * METERS_IN_A_FOOT;
    }

    public double meters() {
        return altitudeInMeters;
    }

    public double feet() {
        return metersToFeet(altitudeInMeters);
    }
}
