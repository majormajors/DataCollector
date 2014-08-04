package com.mattmayers.android.datacollector.util;

import android.content.Context;

import com.dropbox.sync.android.DbxAccountManager;
import com.mattmayers.android.datacollector.R;

/**
 * Created by matt on 6/7/14.
 */
public class DbxAccountManagerUtil {
    public static DbxAccountManager getDbxAccountManager(Context context) {
        String dropboxKey = context.getString(R.string.dropbox_key);
        String dropboxSecret = context.getString(R.string.dropbox_secret);
        return DbxAccountManager.getInstance(context.getApplicationContext(), dropboxKey, dropboxSecret);
    }
}
