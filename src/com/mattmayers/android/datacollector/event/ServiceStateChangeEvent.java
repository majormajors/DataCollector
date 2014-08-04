package com.mattmayers.android.datacollector.event;

import com.mattmayers.android.datacollector.BusDriver;
import com.mattmayers.android.datacollector.DataCollectionService;

public class ServiceStateChangeEvent {
    final private DataCollectionService.State mState;

    public ServiceStateChangeEvent(DataCollectionService.State state) {
        mState = state;
    }

    public DataCollectionService.State getState() {
        return mState;
    }

    public static void post(DataCollectionService.State state) {
        BusDriver.getBus().post(new ServiceStateChangeEvent(state));
    }
}
