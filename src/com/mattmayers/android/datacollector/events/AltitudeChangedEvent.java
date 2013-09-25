package com.mattmayers.android.datacollector.events;

import com.mattmayers.android.datacollector.BusDriver;

public class AltitudeChangedEvent {
	final private float mAltitude;

	public AltitudeChangedEvent(float altitude) {
		mAltitude = altitude;
	}

	public float getAltitude() {
		return mAltitude;
	}

	public static void post(float altitude) {
		BusDriver.getBus().post(new AltitudeChangedEvent(altitude));
	}
}
