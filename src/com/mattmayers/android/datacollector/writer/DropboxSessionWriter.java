package com.mattmayers.android.datacollector.writer;

import android.content.Context;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.mattmayers.android.datacollector.util.DbxAccountManagerUtil;

import java.io.IOException;

/**
 * Created by matt on 8/2/14.
 */
public class DropboxSessionWriter extends SessionWriter {
    public DropboxSessionWriter(Context context) {
        super(context);
    }

    @Override
    public void writeData() {
        DbxAccountManager dbxAccountManager = DbxAccountManagerUtil.getDbxAccountManager(getContext());
        DbxFile dbxFile = null;
        try {
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(dbxAccountManager.getLinkedAccount());
            dbxFile = dbxFs.create(new DbxPath(getFilePath()));
            dbxFile.writeString(getSessionJson());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dbxFile != null) {
                dbxFile.close();
            }
        }
    }
}
