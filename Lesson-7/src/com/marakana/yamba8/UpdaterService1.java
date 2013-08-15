package com.marakana.yamba8;

import com.plter.weibo.sdk.extend.CBFetchStatusUpdates;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UpdaterService1 extends IntentService { // <1>
	private static final String TAG = "UpdaterService";

	public static final String NEW_STATUS_INTENT = "com.marakana.yamba.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	public static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.RECEIVE_TIMELINE_NOTIFICATIONS";

	public UpdaterService1() { // <2>
		super(TAG);

		Log.d(TAG, "UpdaterService constructed");
	}

	@Override
	protected void onHandleIntent(Intent inIntent) { // <3>
		Log.d(TAG, "onHandleIntent'ing");
		YambaApplication yamba = (YambaApplication) getApplication();
		yamba.fetchStatusUpdates(new CBFetchStatusUpdates() {

			public void suc(Integer arg0) {
				if (arg0 > 0) { // <4>
					Intent intent;
					Log.d(TAG, "We have a new status");
					intent = new Intent(NEW_STATUS_INTENT);
					intent.putExtra(NEW_STATUS_EXTRA_COUNT, arg0);
					sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
				}
			}
		});
	}
}
