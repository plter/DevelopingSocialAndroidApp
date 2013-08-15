package com.marakana.yamba4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import com.plter.weibo.sdk.extend.CBGetStatusesAPI;
import com.plter.weibo.sdk.extend.PWeibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

public class UpdaterService1 extends Service {
	private static final String TAG = "UpdaterService";

	static final int DELAY = 60000; // wait a minute
	private boolean runFlag = false;
	private Updater updater;
	private YambaApplication yamba;
	private PWeibo pWeibo;

	DbHelper1 dbHelper; // <1>
	SQLiteDatabase db;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		pWeibo = new PWeibo(this, Config.API_KEY, Config.API_SEC, Config.REDIRECT_URL, Config.SCOPE);
		
		this.yamba = (YambaApplication) getApplication();
		this.updater = new Updater();

		dbHelper = new DbHelper1(this); // <2>

		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flag, int startId) {
		if (!runFlag) {
			this.runFlag = true;
			this.updater.start();
			((YambaApplication) super.getApplication()).setServiceRunning(true);

			Log.d(TAG, "onStarted");
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.runFlag = false;
		this.updater.interrupt();
		this.updater = null;
		this.yamba.setServiceRunning(false);

		Log.d(TAG, "onDestroyed");
	}

	/**
	 * Thread that performs the actual update from the online service
	 */
	private class Updater extends Thread {

		public Updater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdaterService1 updaterService = UpdaterService1.this;
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
										JSONArray timeline= new JSONObject(arg0).getJSONArray("statuses");
										JSONObject status;

										// Open the database for writing
										db = dbHelper.getWritableDatabase(); // <4>
										ContentValues values = new ContentValues(); // <5>
										for (int i = 0; i < timeline.length(); i++) {
											values.clear(); // <7>
											status = timeline.getJSONObject(i);
											values.put(DbHelper1.C_ID, status.getString("id"));
											values.put(DbHelper1.C_CREATED_AT, new Time(status.getString("created_at")).gmtoff);
											values.put(DbHelper1.C_SOURCE, status.getString("source"));
											values.put(DbHelper1.C_TEXT, status.getString("text"));
											values.put(DbHelper1.C_USER, status.getJSONObject("user").getString("name"));
											db.insertOrThrow(DbHelper1.TABLE, null, values); // <8>
											Log.d(TAG, String.format("%s: %s", status.getJSONObject("user").getString("name"), status.getString("text")));
										}
										// Close the database
										db.close(); // <9>
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
