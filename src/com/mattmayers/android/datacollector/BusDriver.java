package com.mattmayers.android.datacollector;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusDriver {
	private static Bus BUS = new Bus(ThreadEnforcer.ANY);

	public static Bus getBus() {
		return BUS;
	}

	private BusDriver() {
	}
}
