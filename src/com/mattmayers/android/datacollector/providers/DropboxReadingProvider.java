package com.mattmayers.android.datacollector.providers;

import android.content.Context;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileStatus;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.mattmayers.android.datacollector.R;
import com.mattmayers.android.datacollector.model.Reading;
import com.mattmayers.android.datacollector.utils.DbxAccountManagerUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by matt on 6/6/14.
 */
public class DropboxReadingProvider extends ReadingProvider {
    private DbxAccountManager mDbxAccountManager;

    public DropboxReadingProvider(Context context) {
        super(context);
        mDbxAccountManager = DbxAccountManagerUtil.getDbxAccountManager(context);
    }

    private Date mStartTime;
    private Date mStopTime;
    private long mStartMillis;

    private List<Reading> mQueue;

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
        DbxFileSystem dbxFs;
        List<DbxFileInfo> fileList;

        try {
            dbxFs = DbxFileSystem.forAccount(mDbxAccountManager.getLinkedAccount());
            fileList = dbxFs.listFolder(DbxPath.ROOT);
        } catch (DbxException e) {
            handleDbxError(e);
            return;
        }

        List<String> paths = new ArrayList<>();
        for (DbxFileInfo fileInfo : fileList) {
            paths.add(fileInfo.path.toString());
        }
        // NEED TO DISPLAY A FILE LIST HERE
    }

    @Override
    protected void onStop() {
        mQueue.clear();
    }

    @Override
    protected void onCalibrated() {
    }

    private void handleDbxError(Throwable e) {
        e.printStackTrace();
        Toast.makeText(
                getContext(), getContext().getString(R.string.dropbox_error, e.getMessage()),Toast.LENGTH_SHORT).show();
    }
}
