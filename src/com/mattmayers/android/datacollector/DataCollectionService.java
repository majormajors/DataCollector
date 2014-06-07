package com.mattmayers.android.datacollector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.mattmayers.android.datacollector.events.ServiceStateChangeEvent;
import com.mattmayers.android.datacollector.model.Reading;
import com.mattmayers.android.datacollector.providers.ReadingProvider;
import com.mattmayers.android.datacollector.providers.SensorReadingProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

public class DataCollectionService extends Service {
    private static final String TAG = DataCollectionService.class.getName();
    private static final String BASE_DIR = "/sdcard/DataCollector";
    private static final int NOTIFICATION_ID = 1000;
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    public static final String EXTRA_SENSOR_SWITCH = "EXTRA_SENSOR_SWITCH";

    public static boolean isRunning = false;

    private NotificationManager mNotifcationManager;
    private ReadingProvider mProvider;

    private DbxAccountManager mDbxAccountManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mProvider == null) {
            mProvider = new SensorReadingProvider(this);
        }

        mProvider.setOnReadingListener(new ReadingProvider.OnReadingListener() {
            @Override
            public void onReading(Reading reading) {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {

            }
        });

        mNotifcationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String dropboxKey = getString(R.string.dropbox_key);
        String dropboxSecret = getString(R.string.dropbox_secret);
        mDbxAccountManager = DbxAccountManager.getInstance(getApplicationContext(), dropboxKey, dropboxSecret);
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
        json.put("startTime", DATE_FORMAT.format(mProvider.getStartTime()));
        json.put("endTime", DATE_FORMAT.format(mProvider.getStopTime()));
        json.put("barometer", buildJSON(mProvider.getReadings()));
        writeFile(json.toString(2));

        mProvider.clearData();
    }

    private JSONArray buildJSON(List<Reading> readings) throws JSONException {
        JSONArray entries = new JSONArray();

        JSONArray values;
        for (Reading reading : readings) {
            values = new JSONArray();
            values.put(reading.millis);
            values.put(reading.pressure);
            entries.put(values);
        }

        return entries;
    }

    private void writeFile(String data) throws IOException, JSONException {
        String fileName = String.format("%s.json", DATE_FORMAT.format(mProvider.getStartTime()));

        if (mDbxAccountManager.hasLinkedAccount()) {
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAccountManager.getLinkedAccount());
            DbxFile dbxFile = dbxFs.create(new DbxPath(fileName));
            try {
                dbxFile.writeString(data);
            } finally {
                dbxFile.close();
            }
        } else {
            Toast.makeText(this, R.string.storing_files_to_sd, Toast.LENGTH_SHORT).show();

            new File(BASE_DIR).mkdirs();
            FileWriter writer = new FileWriter(
                    String.format("%s/%s", BASE_DIR, fileName), false);
            writer.write(data);
            writer.close();
        }
    }

    private void startCollecting() {
        mProvider.start();

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
        mNotifcationManager.notify(NOTIFICATION_ID, builder.getNotification());

        ServiceStateChangeEvent.post(ServiceStateChangeEvent.State.STARTED);
    }

    private void stopCollecting() {
        mProvider.stop();

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
        ServiceStateChangeEvent.post(ServiceStateChangeEvent.State.STOPPED);
        stopSelf();
    }

//    private void setPressureAGL() {
//        final List<Float> samples = new ArrayList<Float>(10);
//
//        mSensorManager.registerListener(new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                if (samples.size() < 10) {
//                    samples.add(event.values[0]);
//                } else {
//                    mSensorManager.unregisterListener(this);
//                    float sum = 0f;
//                    for (float value : samples) {
//                        sum += value;
//                        mPressureAGL = sum / samples.size();
//                    }
//                }
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            }
//        }, mPressure, SensorManager.SENSOR_DELAY_FASTEST);
//    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        long millis = System.currentTimeMillis() - mStartMillis;
//
//        switch (event.sensor.getType()) {
//            case Sensor.TYPE_PRESSURE:
//                float pressure = event.values[0];
//                mReadings.add(new Reading(millis, pressure));
//                if (mPressureAGL > 0) {
//                    AltitudeChangedEvent.post(getAltitudeInFeetAGL(pressure));
//                }
//                break;
//        }
//    }

//    private float getAltitudeInFeetAGL(float value) {
//        return SensorManager.getAltitude(mPressureAGL, value) * 3.281f;
//    }
}