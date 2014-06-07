package com.mattmayers.android.datacollector.events;

import com.mattmayers.android.datacollector.BusDriver;
import com.mattmayers.android.datacollector.model.Reading;

public class AltitudeChangedEvent {
    final private Reading mReading;

    public AltitudeChangedEvent(Reading reading) {
        mReading = reading;
    }

    public Reading getReading() {
        return mReading;
    }

    public static void post(Reading reading) {
        BusDriver.getBus().post(new AltitudeChangedEvent(reading));
    }
}
