package com.mattmayers.android.datacollector.writer;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mattmayers.android.datacollector.model.Session;

/**
 * Created by matt on 8/2/14.
 */
public abstract class SessionWriter {
    private Context mContext;
    private String mFilePath;
    private Session mSession;
    private Gson mGson = new Gson();

    public SessionWriter(Context context) {
        mContext = context;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setSession(Session session) {
        mSession = session;
    }

    public Session getSession() {
        return mSession;
    }

    protected String getSessionJson() {
        return mGson.toJson(mSession);
    }

    public Context getContext() {
        return mContext;
    }

    public abstract void writeData();
}
