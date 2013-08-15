package com.marakana.yamba1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.plter.weibo.sdk.extend.CBGetStatusesAPI;
import com.plter.weibo.sdk.extend.PWeibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

public class StatusActivity2 extends Activity implements OnClickListener {
	private static final String TAG = "StatusActivity";
	private static Context __context = null;
	EditText editText;
	Button updateButton;
	private PWeibo pWeibo;

	public StatusActivity2() {
		__context=this;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);

		// Find views
		editText = (EditText) findViewById(R.id.editText);
		updateButton = (Button) findViewById(R.id.buttonUpdate);
		updateButton.setOnClickListener(this);

		pWeibo = new PWeibo(this,Config.API_KEY,Config.API_SEC ,Config.REDIRECT_URL, Config.SCOPE);
	}


	private static final Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ON_COMPLETE:
				Toast.makeText(__context, "onComplete", Toast.LENGTH_SHORT).show();
				break;
			case ON_IO_EXCEPTION:
			case ON_ERROR:
				Toast.makeText(__context, "onError", Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};	
	
	// Called when button is clicked
	public void onClick(View v) {
		final String status = editText.getText().toString();
		
		pWeibo.getStatusesAPI(new CBGetStatusesAPI() {
			@Override
			public void suc(StatusesAPI arg0) {
				
				arg0.update(status, "0.0", "0.0", new RequestListener() {
					
					@Override
					public void onIOException(IOException arg0) {
						handler.sendEmptyMessage(ON_IO_EXCEPTION);
					}
					
					@Override
					public void onError(WeiboException arg0) {
						handler.sendEmptyMessage(ON_ERROR);
					}
					
					@Override
					public void onComplete4binary(ByteArrayOutputStream arg0) {
					}
					
					@Override
					public void onComplete(String arg0) {
						handler.sendEmptyMessage(ON_COMPLETE);
					}
				});
			}
			
			@Override
			public void fail() {
				handler.sendEmptyMessage(ON_ERROR);
			}
		});
	}
	
	private static final int ON_IO_EXCEPTION = 1;
	private static final int ON_ERROR = 2;
	private static final int ON_COMPLETE = 3;
	
}