package com.mattmayers.android.datacollector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.dropbox.sync.android.DbxAccountManager;
import com.mattmayers.android.datacollector.event.AltitudeChangedEvent;
import com.mattmayers.android.datacollector.event.ServiceStateChangeEvent;
import com.mattmayers.android.datacollector.model.Session;
import com.mattmayers.android.datacollector.provider.DropboxReadingProvider;
import com.mattmayers.android.datacollector.util.DbxAccountManagerUtil;
import com.mattmayers.android.datacollector.model.Reading;
import com.mattmayers.android.datacollector.provider.ReadingProvider;
import com.mattmayers.android.datacollector.provider.SensorReadingProvider;
import com.mattmayers.android.datacollector.writer.DropboxSessionWriter;
import com.mattmayers.android.datacollector.writer.FileSessionWriter;
import com.mattmayers.android.datacollector.writer.SessionWriter;

import java.text.DateFormat;

public class DataCollectionService extends Service {
    private static final String TAG = DataCollectionService.class.getName();
    private static final int NOTIFICATION_ID = 1000;
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();
    public static final String EXTRA_LOAD_FILE = "EXTRA_LOAD_FILE";

    private NotificationManager mNotificationManager;
    private ReadingProvider mProvider;
    private DbxAccountManager mDbxAccountManager;
    private Reading mLastReading;
    private boolean mWriteData;

    private ReadingProvider.OnReadingListener mOnReadingListener = new ReadingProvider.OnReadingListener() {
        @Override
        public void onReading(Reading reading) {
            if (mLastReading != null) {
                double change = Math.abs(mLastReading.altitude.feet() - reading.altitude.feet());
                if (change > 10.0) {
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
            mNotificationManager.notify(NOTIFICATION_ID, builder.getNotification());

            ServiceStateChangeEvent.post(State.RUNNING);
        }

        @Override
        public void onStop() {
            if (mWriteData) {
                writeFile();
            }
            mNotificationManager.cancel(NOTIFICATION_ID);
            ServiceStateChangeEvent.post(State.STOPPED);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mDbxAccountManager = DbxAccountManagerUtil.getDbxAccountManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String loadFile = intent.getStringExtra(EXTRA_LOAD_FILE);
            if (loadFile != null) {
                mProvider = new DropboxReadingProvider(this);
                ((DropboxReadingProvider) mProvider).setLoadFile(loadFile);
                mWriteData = false;
            } else {
                mProvider = new SensorReadingProvider(this);
                mWriteData = true;
            }
            mProvider.setOnReadingListener(mOnReadingListener);
            mProvider.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mProvider != null) {
            mProvider.stop();
        }
    }

    private void writeFile() {
        final Session session = new Session(mProvider);
        final SessionWriter writer;
        final String fileName = String.format("%s.json", DATE_FORMAT.format(mProvider.getStartTime()));

        if (mDbxAccountManager.hasLinkedAccount()) {
            writer = new DropboxSessionWriter(this);
        } else {
            Toast.makeText(this, R.string.storing_files_to_sd, Toast.LENGTH_SHORT).show();
            writer = new FileSessionWriter(this);
        }

        writer.setFilePath(fileName);
        writer.setSession(session);
        writer.writeData();
        mProvider = null;
    }

    public static enum State {
        RUNNING,
        STOPPED
    }
}