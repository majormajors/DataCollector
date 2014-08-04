package com.mattmayers.android.datacollector.provider;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.mattmayers.android.datacollector.model.Altitude;
import com.mattmayers.android.datacollector.model.Reading;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by matt on 6/6/14.
 */
public class SensorReadingProvider extends ReadingProvider {
    private final SensorManager mSensorManager;
    private final Sensor mPressure;

    private long mStartMillis;
    private Date mStartTime;
    private Date mStopTime;

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_PRESSURE) {
                return;
            }

            Reading reading = new Reading();
            reading.millis = System.currentTimeMillis() - mStartMillis;
            reading.pressure = event.values[0];
            onReading(reading);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public SensorReadingProvider(Context context) {
        super(context);
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    public Date getStartTime() {
        return mStartTime;
    }

    @Override
    public Date getStopTime() {
        return mStopTime;
    }

    @Override
    public long getStartMillis() {
        return mStartMillis;
    }

    @Override
    protected void onStart() {
        mStartTime = Calendar.getInstance().getTime();
        mStartMillis = System.currentTimeMillis();

        mSensorManager.registerListener(new SensorEventListener() {
            private static final int READING_COUNT = 10;
            private int i = 0;
            private float mSum = 0.0f;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() != Sensor.TYPE_PRESSURE) {
                    return;
                }

                if (i++ < READING_COUNT) {
                    mSum += event.values[0];
                } else {
                    mSensorManager.unregisterListener(this);
                    float average = mSum / READING_COUNT;
                    setBaseReading(new Reading(0, average, Altitude.meters(0.0)));
                    onCalibrated();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, mPressure, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        mStopTime = Calendar.getInstance().getTime();
    }

    @Override
    protected void onCalibrated() {
        mSensorManager.registerListener(mSensorListener, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
