package com.doubleh.lumidiet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.doubleh.lumidiet.BuildConfig;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class LumiFirebaseInstanceIDService extends FirebaseInstanceIdService {
	private final String TAG = "LumiFIIDService";

	public LumiFirebaseInstanceIDService() {
	}

	@Override
	public void onTokenRefresh() {
		//super.onTokenRefresh();
		String token = FirebaseInstanceId.getInstance().getToken();

		if (BuildConfig.DEBUG) Log.d(TAG, "Firebase Token: " + token);
	}
}
