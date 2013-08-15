package com.marakana.yamba7;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.plter.weibo.sdk.extend.CBFetchStatusUpdates;
import com.plter.weibo.sdk.extend.CBGetStatusesAPI;
import com.plter.weibo.sdk.extend.PWeibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;

public class YambaApplication extends Application {
	private static final String TAG = YambaApplication.class.getSimpleName();
	public static final String LOCATION_PROVIDER_NONE = "NONE";
	public static final long INTERVAL_NEVER = 0;
	private static final int ON_ERROR = 2;
	private static final int ON_SUC =1;
	private static Context __context = null;
	private SharedPreferences prefs;
	private StatusData statusData;
	private boolean serviceRunning;
	private boolean inTimeline;

	public YambaApplication() {
		__context= this;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.statusData = new StatusData(this);
		Log.i(TAG, "Application started");
	}


	public PWeibo getNewPWeibo() {
		return new PWeibo(this, Config.API_KEY, Config.API_SEC, Config.REDIRECT_URL, Config.SCOPE);
	}


	public boolean startOnBoot() {
		return this.prefs.getBoolean("startOnBoot", false);
	}

	public StatusData getStatusData() {
		return statusData;
	}


	private static final Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ON_ERROR:
				Toast.makeText(__context, "失败", Toast.LENGTH_SHORT).show();
				break;
			case ON_SUC:
				Toast.makeText(__context, "成功", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};


	public void fetchStatusUpdates(final CBFetchStatusUpdates updates) {
		Log.d(TAG, "Fetching status updates");

		getNewPWeibo().getStatusesAPI(new CBGetStatusesAPI() {

			public void suc(StatusesAPI arg0) {
				arg0.friendsTimeline(0, 0, 50, 1, false, WeiboAPI.FEATURE.ALL, false, new RequestListener() {

					public void onIOException(IOException arg0) {
						handler.sendEmptyMessage(ON_ERROR);
					}

					public void onError(WeiboException arg0) {
						handler.sendEmptyMessage(ON_ERROR);
					}

					public void onComplete4binary(ByteArrayOutputStream arg0) {
					}

					public void onComplete(String arg0) {
						try {

							JSONObject jo = new JSONObject(arg0);
							JSONArray statusUpdates = jo.getJSONArray("statuses");
							long latestStatusCreatedAtTime = getStatusData()
									.getLatestStatusCreatedAtTime();
							int count = 0;
							ContentValues values = new ContentValues();
							JSONObject status=null;

							for (int i=0;i<statusUpdates.length();i++) {
								status = statusUpdates.getJSONObject(i);
								values.put(StatusData.C_ID, status.getString("id"));
								long createdAt = new Date(status.getString("created_at")).getTime();
								values.put(StatusData.C_CREATED_AT, createdAt);
								values.put(StatusData.C_TEXT, status.getString("text"));
								values.put(StatusData.C_USER, status.getJSONObject("user").getString("name"));
								Log.d(TAG, "Got update with id " + status.getString("id") + ". Saving");
								getStatusData().insertOrIgnore(values);
								if (latestStatusCreatedAtTime < createdAt) {
									count++;
								}
							}
							Log.d(TAG, count > 0 ? "Got " + count + " status updates"
									: "No new status updates");

							updates.suc(count);
							handler.sendEmptyMessage(ON_SUC);
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(ON_ERROR);
							updates.suc(0);
						}
					}
				});
			}

			public void fail() {
				handler.sendEmptyMessage(ON_ERROR);
			}
		});
	}


	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	public boolean isInTimeline() {
		return inTimeline;
	}

	public void setInTimeline(boolean inTimeline) {
		this.inTimeline = inTimeline;
	}

	public String getProvider() {
		return prefs.getString("provider", LOCATION_PROVIDER_NONE);
	}

	public long getInterval() {
		// For some reason storing interval as long doesn't work
		return Long.parseLong(prefs.getString("interval", "0"));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		this.statusData.close();
		Log.i(TAG, "Application terminated");
	}
}
