package com.mattmayers.android.datacollector.providers;

import android.content.Context;

import java.util.Date;

/**
 * Created by matt on 6/6/14.
 */
public class DropboxReadingProvider extends ReadingProvider {
    public DropboxReadingProvider(Context context) {
        super(context);
    }

    @Override
    public Date getStartTime() {
        return null;
    }

    @Override
    public Date getStopTime() {
        return null;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onCalibrated() {
    }
}
