package com.marakana.yamba6;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.plter.weibo.sdk.extend.CBGetStatusesAPI;
import com.plter.weibo.sdk.extend.PWeibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

public class StatusActivity extends Activity implements OnClickListener,
TextWatcher {
	private static final String TAG = "StatusActivity";
	EditText editText;
	Button updateButton;
	TextView textCount;
	private PWeibo pWeibo;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);

		pWeibo=new PWeibo(this, Config.API_KEY, Config.API_SEC, Config.REDIRECT_URL, Config.SCOPE);

		// Find views
		editText = (EditText) findViewById(R.id.editText);
		updateButton = (Button) findViewById(R.id.buttonUpdate);
		updateButton.setOnClickListener(this);

		textCount = (TextView) findViewById(R.id.textCount);
		textCount.setText(Integer.toString(140));
		textCount.setTextColor(Color.GREEN);
		editText.addTextChangedListener(this);
		
//		startService(new Intent(this, UpdaterService.class));
	}

	// Called when button is clicked
	public void onClick(View v) {
		pWeibo.getStatusesAPI(new CBGetStatusesAPI() {

			@Override
			public void suc(StatusesAPI arg0) {
				String status = editText.getText().toString();
				arg0.update(status, "0.0", "0.0", new RequestListener() {
					
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
						Log.e(TAG, "complete");
					}
				});
			}

			public void fail() {
				Log.e(TAG, "fail");
			}
		});
	}

	// Called first time user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}


	// TextWatcher methods
	public void afterTextChanged(Editable statusText) {
		int count = 140 - statusText.length();
		textCount.setText(Integer.toString(count));
		textCount.setTextColor(Color.GREEN);
		if (count < 10)
			textCount.setTextColor(Color.YELLOW);
		if (count < 0)
			textCount.setTextColor(Color.RED);
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

}