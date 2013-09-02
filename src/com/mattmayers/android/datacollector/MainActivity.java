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

public class MainActivity extends Activity {
	private Button mStart;
	private Button mStop;
	private TextView mStatus;

	private long mStartTime;

	private BroadcastReceiver mServiceStartedReceiver;

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

	private void startCollection() {
		Intent intent = new Intent(this, DataCollectionService.class);
		intent.putExtra(DataCollectionService.EXTRA_SENSOR_SWITCH, true);
		startService(intent);

		mStartTime = System.currentTimeMillis();
	}

	private void stopCollection() {
		Intent intent = new Intent(this, DataCollectionService.class);
		intent.putExtra(DataCollectionService.EXTRA_SENSOR_SWITCH, false);
		startService(intent);

		mStartTime = 0L;
	}

	public void updateUI() {
		if (DataCollectionService.isRunning) {
			mStart.setEnabled(false);
			mStop.setEnabled(true);
			mStatus.setText(R.string.running);
			mStatus.setTextColor(Color.GREEN);
		} else {
			mStart.setEnabled(true);
			mStop.setEnabled(false);
			mStatus.setText(R.string.stopped);
			mStatus.setTextColor(Color.RED);
		}
	}
}
