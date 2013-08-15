package com.marakana.yamba3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.plter.weibo.sdk.extend.CBGetStatusesAPI;
import com.plter.weibo.sdk.extend.PWeibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

public class UpdaterService extends Service {
	private static final String TAG = "UpdaterService";

	static final int DELAY = 60000; // wait a minute
	private boolean runFlag = false;
	private Updater updater;
	private YambaApplication yamba; // <1>
	private PWeibo pWeibo;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		pWeibo = new PWeibo(this, Config.API_KEY, Config.API_SEC, Config.REDIRECT_URL, Config.SCOPE);
		
		this.yamba = (YambaApplication) getApplication(); // <2>
		this.updater = new Updater();

		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		this.runFlag = true;
		this.updater.start();
		this.yamba.setServiceRunning(true); // <3>

		Log.d(TAG, "onStarted");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		this.yamba.setServiceRunning(false); // <4>

		Log.d(TAG, "onDestroyed");
	}

	/**
	 * Thread that performs the actual update from the online service
	 */
	private class Updater extends Thread {
		JSONArray timeline; // <5>

		public Updater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;
			while (updaterService.runFlag) {
				Log.d(TAG, "Updater running");
				try {
					// Get the timeline from the cloud
					pWeibo.getStatusesAPI(new CBGetStatusesAPI() {

						@Override
						public void suc(StatusesAPI arg0) {
							arg0.friendsTimeline(0, 0, 50, 1, false, WeiboAPI.FEATURE.ALL, false, new RequestListener() {

								@Override
								public void onIOException(IOException arg0) {
									Log.e(TAG, "io error");
								}

								@Override
								public void onError(WeiboException arg0) {
									Log.e(TAG, "error");
								}

								@Override
								public void onComplete4binary(ByteArrayOutputStream arg0) {
								}

								@Override
								public void onComplete(String arg0) {
									try {
										timeline = new JSONObject(arg0).getJSONArray("statuses");
										JSONObject status;
										for (int i = 0; i < timeline.length(); i++) {
											status = timeline.getJSONObject(i);
											Log.d(TAG, String.format("%s: %s", status.getJSONObject("user").getString("name"), status.getString("text")));
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
						}

						@Override
						public void fail() {
							Log.e(TAG, "fail");
						}
					});

					Log.d(TAG, "Updater ran");
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	} // Updater
}
