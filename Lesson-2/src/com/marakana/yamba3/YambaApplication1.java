package com.marakana.yamba3;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApplication1 extends Application{ // <1>
  private static final String TAG = YambaApplication1.class.getSimpleName();
  private SharedPreferences prefs;

  @Override
  public void onCreate() { // <3>
    super.onCreate();
    this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
    Log.i(TAG, "onCreated");
  }

  @Override
  public void onTerminate() { // <4>
    super.onTerminate();
    Log.i(TAG, "onTerminated");
  }

}
