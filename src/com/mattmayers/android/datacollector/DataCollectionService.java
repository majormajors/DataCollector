package com.mattmayers.android.datacollector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.*;

public class DataCollectionService extends Service implements SensorEventListener {
	private static final String TAG = DataCollectionService.class.getName();
	private static final int NOTIFICATION_ID = 1000;

	public static final String EXTRA_SENSOR_SWITCH = "com.mattmayers.android.datacollector.EXTRA_SENSOR_SWITCH";
	public static final String ACTION_SERVICE_STARTED = "com.mattmayers.android.datacollector.ACTION_SERVICE_STARTED";
	public static final String ACTION_SERVICE_STOPPED = "com.mattmayers.android.datacollector.ACTION_SERVICE_STOPPED";

	public static boolean isRunning = false;

	private NotificationManager mNotifcationManager;
	private SensorManager mSensorManager;

	private Sensor mPressure;
	private Sensor mHumidity;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private Sensor mGyroscope;
	private Sensor mTemperature;

	private long mStartMillis;
	private Date mStartTime;

	private SortedMap<Long, float[]> mPressureValues = new TreeMap<Long, float[]>();
	private SortedMap<Long, float[]> mHumidityValues = new TreeMap<Long, float[]>();
	private SortedMap<Long, float[]> mAccelerometerValues = new TreeMap<Long, float[]>();
	private SortedMap<Long, float[]> mMagnetometerValues = new TreeMap<Long, float[]>();
	private SortedMap<Long, float[]> mGyroscopeValues = new TreeMap<Long, float[]>();
	private SortedMap<Long, float[]> mTemperatureValues = new TreeMap<Long, float[]>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mNotifcationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || intent.getBooleanExtra(EXTRA_SENSOR_SWITCH, true))
			startCollecting();
		else
			stopCollecting();
		return START_STICKY;
	}

	private void dumpSensorData() {
		mPressureValues.clear();
		mAccelerometerValues.clear();
		mHumidityValues.clear();
		mMagnetometerValues.clear();
		mGyroscopeValues.clear();
		mTemperatureValues.clear();
	}

	private void startCollecting() {
		mStartMillis = System.currentTimeMillis();
		mStartTime = Calendar.getInstance().getTime();

		if (mPressure != null)
			mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
		if (mHumidity != null)
			mSensorManager.registerListener(this, mHumidity, SensorManager.SENSOR_DELAY_NORMAL);
		if (mAccelerometer != null)
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		if (mMagnetometer != null)
			mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
		if (mGyroscope != null)
			mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
		if (mTemperature != null)
			mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);

		synchronized (this) {
			DataCollectionService.isRunning = true;
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setSmallIcon(android.R.drawable.ic_menu_info_details)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.running))
				.setOngoing(true);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, MainActivity.class), 0);
		builder.setContentIntent(pendingIntent);
		mNotifcationManager.notify(NOTIFICATION_ID, builder.build());

		sendBroadcast(new Intent(ACTION_SERVICE_STARTED));
	}

	private void stopCollecting() {
		mSensorManager.unregisterListener(this);
		dumpSensorData();

		synchronized (this) {
			DataCollectionService.isRunning = false;
		}

		mNotifcationManager.cancel(NOTIFICATION_ID);

		sendBroadcast(new Intent(ACTION_SERVICE_STOPPED));
		stopSelf();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		long millis = System.currentTimeMillis() - mStartMillis;

		switch (event.sensor.getType()) {
			case Sensor.TYPE_PRESSURE:
				mPressureValues.put(millis, event.values);
				break;
			case Sensor.TYPE_ACCELEROMETER:
				mAccelerometerValues.put(millis, event.values);
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				mHumidityValues.put(millis, event.values);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				mMagnetometerValues.put(millis, event.values);
				break;
			case Sensor.TYPE_GYROSCOPE:
				mGyroscopeValues.put(millis, event.values);
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				mTemperatureValues.put(millis, event.values);
				break;
		}

		Log.d(TAG, String.format("%s: %s", event.sensor.getName(), Arrays.toString(event.values)));
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}