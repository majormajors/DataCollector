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
import com.mattmayers.android.datacollector.events.AltitudeChangedEvent;
import com.mattmayers.android.datacollector.events.ServiceStateChangeEvent;
import com.mattmayers.android.datacollector.utils.DbxAccountManagerUtil;
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

    private State mState = State.STOPPED;

    private NotificationManager mNotifcationManager;
    private ReadingProvider mProvider;

    private DbxAccountManager mDbxAccountManager;

    private Reading mLastReading;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotifcationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mDbxAccountManager = DbxAccountManagerUtil.getDbxAccountManager(this);

        if (mProvider == null) {
            mProvider = new SensorReadingProvider(this);
        }

        mProvider.setOnReadingListener(new ReadingProvider.OnReadingListener() {
            @Override
            public void onReading(Reading reading) {
                if (mLastReading != null) {
                    double change = Math.abs(mLastReading.altitude.meters() - reading.altitude.meters());
                    if (change > 1.0) {
                        AltitudeChangedEvent.post(reading);
                    }
                }

                mLastReading = reading;
            }

            @Override
            public void onStart() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(DataCollectionService.this)
                        .setSmallIcon(android.R.drawable.ic_menu_info_details)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.running))
                        .setOngoing(true);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        DataCollectionService.this, 0, new Intent(DataCollectionService.this, MainActivity.class), 0);
                builder.setContentIntent(pendingIntent);
                mNotifcationManager.notify(NOTIFICATION_ID, builder.getNotification());

                mState = State.RUNNING;
                ServiceStateChangeEvent.post(mState);
            }

            @Override
            public void onStop() {
                try {
                    dumpSensorData();
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                mNotifcationManager.cancel(NOTIFICATION_ID);

                mState = State.STOPPED;
                ServiceStateChangeEvent.post(mState);
                stopSelf();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getBooleanExtra(EXTRA_SENSOR_SWITCH, true)) {
            mProvider.start();
            return START_STICKY;
        } else {
            mProvider.stop();
            return START_NOT_STICKY;
        }
    }

    private void dumpSensorData() throws JSONException, IOException {
        JSONObject json = new JSONObject();
        json.put("start_time", DATE_FORMAT.format(mProvider.getStartTime()));
        json.put("end_time", DATE_FORMAT.format(mProvider.getStopTime()));
        json.put("start_millis", mProvider.getStartMillis());
        json.put("base_reading", mProvider.getBaseReading().pressure);
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

    public static enum State {
        RUNNING,
        STOPPED
    }
}