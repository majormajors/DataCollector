package com.mattmayers.android.datacollector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.dropbox.sync.android.DbxAccountManager;
import com.mattmayers.android.datacollector.event.AltitudeChangedEvent;
import com.mattmayers.android.datacollector.event.ServiceStateChangeEvent;
import com.squareup.otto.Subscribe;

public class MainActivity extends Activity {
    private static final int REQUEST_LINK_TO_DROPBOX = 0;
    private static final int CHOOSE_A_FILE = 1;

    private Button mStart;
    private Button mStop;
    private Button mLinkDropbox;
    private TextView mStatus;

    private DataCollectionService.State mServiceState = DataCollectionService.State.STOPPED;
    private Long mAltitudeFeet;

    private DbxAccountManager mDbxAccountManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mStatus = (TextView) findViewById(R.id.status);
        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mLinkDropbox = (Button) findViewById(R.id.link_dropbox);

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleService();
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleService();
            }
        });
        mLinkDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbxAccountManager.startLink(MainActivity.this, REQUEST_LINK_TO_DROPBOX);
            }
        });

        String dropboxKey = getString(R.string.dropbox_key);
        String dropboxSecret = getString(R.string.dropbox_secret);
        mDbxAccountManager = DbxAccountManager.getInstance(getApplicationContext(), dropboxKey, dropboxSecret);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
        BusDriver.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            BusDriver.getBus().unregister(this);
        } catch (Exception e) {
            // Do everything Jon Snow knows
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LINK_TO_DROPBOX:
                updateUI();
                break;
            case CHOOSE_A_FILE:
                // DO A THING
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void toggleService() {
        Intent intent = new Intent(this, DataCollectionService.class);
        if (!isRunning()) {
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    @Subscribe
    public void onServiceStateChanged(ServiceStateChangeEvent event) {
        mServiceState = event.getState();
        updateUI();
    }

    @Subscribe
    public void onAltitudeChanged(AltitudeChangedEvent event) {
        mAltitudeFeet = Math.round(event.getReading().altitude.feet());
        updateUI();
    }

    private void updateUI() {
        if (isRunning()) {
            mStart.setEnabled(false);
            mStop.setEnabled(true);
            if (mAltitudeFeet == null) {
                mStatus.setText(R.string.running);
            } else {
                mStatus.setText(String.valueOf(mAltitudeFeet));
            }
            mStatus.setTextColor(Color.GREEN);
            mLinkDropbox.setVisibility(View.GONE);
        } else {
            mStart.setEnabled(true);
            mStop.setEnabled(false);
            mStatus.setText(R.string.stopped);
            mStatus.setTextColor(Color.RED);

            if (mDbxAccountManager.hasLinkedAccount()) {
                mLinkDropbox.setVisibility(View.GONE);
                // TODO: show load file button
            } else {
                mLinkDropbox.setVisibility(View.VISIBLE);
                // TODO: hide load file button
            }
        }
    }

    private boolean isRunning() {
        return mServiceState == DataCollectionService.State.RUNNING;
    }
}
