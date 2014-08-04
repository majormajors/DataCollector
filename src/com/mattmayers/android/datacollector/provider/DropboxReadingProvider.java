package com.mattmayers.android.datacollector.provider;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.google.gson.Gson;
import com.mattmayers.android.datacollector.R;
import com.mattmayers.android.datacollector.model.Reading;
import com.mattmayers.android.datacollector.model.Session;
import com.mattmayers.android.datacollector.util.DbxAccountManagerUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by matt on 6/6/14.
 */
public class DropboxReadingProvider extends ReadingProvider {
    private DbxAccountManager mDbxAccountManager;
    private Handler mHandler = new Handler();

    public DropboxReadingProvider(Context context) {
        super(context);
        mDbxAccountManager = DbxAccountManagerUtil.getDbxAccountManager(context);
    }

    private Date mStartTime;
    private Date mStopTime;
    private long mStartMillis;
    private String mLoadFile;
    private List<Reading> mQueue;
    private long mLastMillis = 0;

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
        if (mLoadFile == null) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.cant_load_file, mLoadFile), Toast.LENGTH_SHORT).show();
            stop();
            return;
        }

        final DbxFileSystem dbxFs;
        final DbxFile file;
        final Gson gson = new Gson();

        try {
            dbxFs = DbxFileSystem.forAccount(mDbxAccountManager.getLinkedAccount());
            file = dbxFs.open(new DbxPath(mLoadFile));
            Session session = gson.fromJson(new InputStreamReader(file.getReadStream()), Session.class);
            setBaseReading(session.base_reading);
            mStartTime = session.start_time;
            mStopTime = session.stop_time;
            mQueue = Arrays.asList(session.readings);
            onCalibrated();
        } catch (DbxException e) {
            handleDbxError(e);
            stop();
        } catch (IOException e) {
            e.printStackTrace();
            stop();
        }
    }

    @Override
    protected void onStop() {
        mQueue.clear();
    }

    @Override
    protected void onCalibrated() {
        if (mQueue != null) {
            processQueue();
        }
    }

    private void processQueue() {
        long delay;
        for (int i = 0; i < mQueue.size(); i++) {
            final Reading reading = mQueue.remove(0);
            delay = reading.millis - mLastMillis;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onReading(reading);
                }
            }, delay);
        }
        stop();
    }

    private void handleDbxError(Throwable e) {
        e.printStackTrace();
        Toast.makeText(
                getContext(), getContext().getString(R.string.dropbox_error, e.getMessage()),Toast.LENGTH_SHORT).show();
    }

    public void setLoadFile(String loadFile) {
        mLoadFile = loadFile;
    }
}
