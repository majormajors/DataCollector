package com.mattmayers.android.datacollector.model;

import com.mattmayers.android.datacollector.provider.ReadingProvider;

import java.util.Date;
import java.util.List;

/**
 * Created by matt on 8/2/14.
 */
public class Session {
    public Date start_time;
    public Date stop_time;
    public Reading base_reading;
    public Reading[] readings;

    private Session() {
    }

    public Session(ReadingProvider provider) {
        this.start_time = provider.getStartTime();
        this.stop_time = provider.getStopTime();
        this.base_reading = provider.getBaseReading();

        List<Reading> r = provider.getReadings();
        this.readings = r.toArray(new Reading[r.size()]);
    }
}
