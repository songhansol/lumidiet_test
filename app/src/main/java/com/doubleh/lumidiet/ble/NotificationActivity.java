package com.doubleh.lumidiet.ble;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.doubleh.lumidiet.BuildConfig;

/**
 * Created by steve on 2016. 8. 2..
 */
public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "--------------------");
        finish();
    }
}
