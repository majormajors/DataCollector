package com.mattmayers.android.datacollector.events;

import com.mattmayers.android.datacollector.BusDriver;

public class ServiceStateChangeEvent {
	final private State mState;

	public ServiceStateChangeEvent(State state) {
		mState = state;
	}

	public State getState() {
		return mState;
	}

	public static void post(State state) {
		BusDriver.getBus().post(new ServiceStateChangeEvent(state));
	}

	public static enum State {
		STARTED,
		STOPPED
	}
}
