package com.mattmayers.android.datacollector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.dropbox.sync.android.DbxAccountManager;

public class MainActivity extends Activity {
	private static final int REQUEST_LINK_TO_DROPBOX = 0;

	private Button mStart;
	private Button mStop;
	private Button mLinkDropbox;
	private TextView mStatus;

	private BroadcastReceiver mServiceStartedReceiver;

	private DbxAccountManager mDbxAccountManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mServiceStartedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateUI();
			}
		};

		mStatus = (TextView) findViewById(R.id.status);
		mStart = (Button) findViewById(R.id.start);
		mStop = (Button) findViewById(R.id.stop);
		mLinkDropbox = (Button) findViewById(R.id.link_dropbox);

		mStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startCollection();
			}
		});
		mStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopCollection();
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
		registerReceiver(mServiceStartedReceiver,
				new IntentFilter(DataCollectionService.ACTION_SERVICE_STARTED));
		registerReceiver(mServiceStartedReceiver,
				new IntentFilter(DataCollectionService.ACTION_SERVICE_STOPPED));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mServiceStartedReceiver);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DROPBOX) {
			updateUI();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void startCollection() {
		Intent intent = new Intent(this, DataCollectionService.class);
		intent.putExtra(DataCollectionService.EXTRA_SENSOR_SWITCH, true);
		startService(intent);
	}

	private void stopCollection() {
		Intent intent = new Intent(this, DataCollectionService.class);
		intent.putExtra(DataCollectionService.EXTRA_SENSOR_SWITCH, false);
		startService(intent);
	}

	public void updateUI() {
		if (DataCollectionService.isRunning) {
			mStart.setEnabled(false);
			mStop.setEnabled(true);
			mStatus.setText(R.string.running);
			mStatus.setTextColor(Color.GREEN);
			mLinkDropbox.setVisibility(View.GONE);
		} else {
			mStart.setEnabled(true);
			mStop.setEnabled(false);
			mStatus.setText(R.string.stopped);
			mStatus.setTextColor(Color.RED);

			if (mDbxAccountManager.hasLinkedAccount()) {
				mLinkDropbox.setVisibility(View.GONE);
			} else {
				mLinkDropbox.setVisibility(View.VISIBLE);
			}
		}
	}
}
