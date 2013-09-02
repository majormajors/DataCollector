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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class DataCollectionService extends Service implements SensorEventListener {
	private static final String TAG = DataCollectionService.class.getName();
	private static final String BASE_DIR = "/sdcard/DataCollector";
	private static final int NOTIFICATION_ID = 1000;

	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

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

	private float mPressureAGL;
	private long mStartMillis;

	private Date mStartTime;
	private Date mEndTime;

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
		if (intent == null || intent.getBooleanExtra(EXTRA_SENSOR_SWITCH, true)) {
			startCollecting();
			return START_STICKY;
		} else {
			stopCollecting();
			return START_NOT_STICKY;
		}
	}

	private void dumpSensorData() throws JSONException, IOException {
		JSONObject json = new JSONObject();
		json.put("startTime", DATE_FORMAT.format(mStartTime));
		json.put("endTime", DATE_FORMAT.format(mEndTime));
		json.put("barometer", buildJSON(mPressureValues));
		json.put("accelerometer", buildJSON(mAccelerometerValues));
		json.put("humidity", buildJSON(mHumidityValues));
		json.put("magnetometer", buildJSON(mMagnetometerValues));
		json.put("gyroscope", buildJSON(mGyroscopeValues));
		json.put("temperature", buildJSON(mTemperatureValues));
		writeFile(json);

		mPressureValues.clear();
		mAccelerometerValues.clear();
		mHumidityValues.clear();
		mMagnetometerValues.clear();
		mGyroscopeValues.clear();
		mTemperatureValues.clear();
	}

	private JSONArray buildJSON(SortedMap<Long, float[]> map) throws JSONException {
		JSONArray entries = new JSONArray();

		JSONArray values;
		for (Map.Entry<Long, float[]> entry : map.entrySet()) {
			values = new JSONArray();
			values.put(entry.getKey());
			values.put(entry.getValue()[0]);
			values.put(entry.getValue()[1]);
			values.put(entry.getValue()[2]);
			entries.put(values);
		}

		return entries;
	}

	private void writeFile(JSONObject json) throws IOException, JSONException {
		new File(BASE_DIR).mkdirs();
		String fileName = String.format("%s.json", DATE_FORMAT.format(mStartTime));
		FileWriter writer = new FileWriter(
				String.format("%s/%s", BASE_DIR, fileName), false);
		writer.write(json.toString(4));
		writer.close();
	}

	private void startCollecting() {
		if (mPressure != null)
			setPressureAGL();

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
		mEndTime = Calendar.getInstance().getTime();
		try {
			dumpSensorData();
		} catch (JSONException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		synchronized (this) {
			DataCollectionService.isRunning = false;
		}

		mNotifcationManager.cancel(NOTIFICATION_ID);

		sendBroadcast(new Intent(ACTION_SERVICE_STOPPED));
		stopSelf();
	}

	private void setPressureAGL() {
		final List<Float> samples = new ArrayList<Float>(30);

		mSensorManager.registerListener(new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				if (samples.size() < 30) {
					samples.add(event.values[0]);
				} else {
					mSensorManager.unregisterListener(this);
					float sum = 0f;
					for (float value : samples) {
						sum += value;
						mPressureAGL = sum / samples.size();
					}

//					Log.d(TAG, String.format("mPressureAGL = %f", mPressureAGL));
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		}, mPressure, SensorManager.SENSOR_DELAY_FASTEST);
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

//		Log.d(TAG, String.format("%s: %s", event.sensor.getName(), Arrays.toString(event.values)));
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}