package com.mattmayers.android.datacollector.providers;

import android.content.Context;
import android.hardware.SensorManager;

import com.mattmayers.android.datacollector.events.AltitudeChangedEvent;
import com.mattmayers.android.datacollector.model.Altitude;
import com.mattmayers.android.datacollector.model.Reading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by matt on 6/6/14.
 */
public abstract class ReadingProvider {
    private Context mContext;

    private Reading mPressureAGL;
    private List<Reading> mReadings;
    private OnReadingListener mListener;

    public abstract Date getStartTime();
    public abstract Date getStopTime();
    protected abstract void onStart();
    protected abstract void onStop();
    protected abstract void onCalibrated();

    public ReadingProvider(Context context) {
        mContext = context.getApplicationContext();
    }

    public Context getContext() {
        return mContext;
    }

    protected void onReading(Reading reading) {
        float altitude = SensorManager.getAltitude(getPressureAGL().pressure, reading.pressure);
        reading.altitude = Altitude.meters(altitude);

        mReadings.add(reading);

        AltitudeChangedEvent.post(reading);

        if (mListener != null) {
            mListener.onReading(reading);
        }
    }

    public List<Reading> getReadings() {
        return Collections.unmodifiableList(mReadings);
    }

    public void setOnReadingListener(OnReadingListener listener) {
        mListener = listener;
    }

    protected final void setPressureAGL(Reading reading) {
        mPressureAGL = reading;
    }

    public Reading getPressureAGL() {
        return mPressureAGL;
    }

    public final void start() {
        onStart();
        if (mListener != null) {
            mListener.onStart();
        }
    }

    public final void stop() {
        onStop();
        if (mListener != null) {
            mListener.onStop();
        }
    }

    public final void clearData() {
        mReadings.clear();
    }

    public interface OnReadingListener {
        public void onReading(Reading reading);
        public void onStart();
        public void onStop();
    }
}
