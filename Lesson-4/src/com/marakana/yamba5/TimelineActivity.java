package com.marakana.yamba5;

import com.plter.weibo.sdk.extend.CBGetAccessToken;
import com.plter.weibo.sdk.extend.PWeibo;
import com.weibo.sdk.android.Oauth2AccessToken;

import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class TimelineActivity extends BaseActivity { // <1>
	Cursor cursor;
	ListView listTimeline;
	SimpleCursorAdapter adapter;
	static final String[] FROM = { DbHelper.C_CREATED_AT, DbHelper.C_USER,
		DbHelper.C_TEXT };
	static final int[] TO = { R.id.textCreatedAt, R.id.textUser, R.id.textText };

	private PWeibo pWeibo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		
		pWeibo = new PWeibo(this, Config.API_KEY, Config.API_SEC, Config.REDIRECT_URL, Config.SCOPE);

		pWeibo.getAccessToken(new CBGetAccessToken() {

			@Override
			public void suc(Oauth2AccessToken arg0) {
				Log.i("", "Suc");
			}

			@Override
			public void fail() {
				Log.e("", "Fail");
			}

			@Override
			public void cancel() {
				Log.e("", "User cancel");
			}
		});

		// Find your views
		listTimeline = (ListView) findViewById(R.id.listTimeline);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setup List
		this.setupList(); // <3>
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Close the database
		yamba.getStatusData().close(); // <4>
	}

	// Responsible for fetching data and setting up the list and the adapter
	private void setupList() { // <5>
		// Get the data
		cursor = yamba.getStatusData().getStatusUpdates();
		startManagingCursor(cursor);

		// Setup Adapter
		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER); // <6>
		listTimeline.setAdapter(adapter);
	}

	// View binder constant to inject business logic for timestamp to relative
	// time conversion
	static final ViewBinder VIEW_BINDER = new ViewBinder() { // <7>

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() != R.id.textCreatedAt)
				return false;

			// Update the created at text to relative time
			long timestamp = cursor.getLong(columnIndex);
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(view
					.getContext(), timestamp);
			((TextView) view).setText(relTime);

			return true;
		}

	};
}
