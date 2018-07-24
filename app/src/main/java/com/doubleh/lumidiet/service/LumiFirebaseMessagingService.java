package com.doubleh.lumidiet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;

import static com.doubleh.lumidiet.BaseActivity.Preferences_LOGIN;
import static com.doubleh.lumidiet.BaseActivity.Preferences_LOGIN_AUTO;
import static com.doubleh.lumidiet.BaseActivity.Preferences_MK;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_NAME;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_VERSION;

public class LumiFirebaseMessagingService extends FirebaseMessagingService {
	private final String TAG = "LumiFirebaseMSGService";

	public LumiFirebaseMessagingService() {
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);
		if (BuildConfig.DEBUG) Log.d(TAG, "Message Received: " + remoteMessage);
		//setBadge(remoteMessage.getData().get("message"));
		setBadge(remoteMessage.getData().get("badge"));
	}

	private void setBadge(String message) {
		if (BuildConfig.DEBUG) Log.d(TAG, "Received Message: " + message);

		/*if (getSharedPreferences(Preferences_LOGIN, MODE_PRIVATE).getBoolean(Preferences_LOGIN_AUTO, false)) {
			if (BuildConfig.DEBUG) Log.e(TAG, "Auto login false");
			return;
		}
		String masterkey = getSharedPreferences(Preferences_LOGIN, MODE_PRIVATE).getString(Preferences_MK, "");
		if (masterkey.equals("")) {
			if (BuildConfig.DEBUG) Log.e(TAG, "Master Key is null");
			return;
		}

		String language;
		if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko")) {
			language = "KO";
		} else {
			language = "EN";
		}*/

		/*int badgeCount = DatabaseManager.getInstance(getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION)
				.countNoticeNotRead(masterkey, language);*/

		int badgeCount = Integer.parseInt(message);

		if (badgeCount < 0) {
			if (BuildConfig.DEBUG) Log.e(TAG, "Badge count error: " + badgeCount);
			return;
		}

		// 새로운 공지가 있다는 것으로 판단하고 badgeCount를 임의적으로 1 증가
		// 안읽은 개수를 보내는 것으로 변경
		//++badgeCount;
		Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
		intent.putExtra("badge_count", badgeCount);
		//앱의  패키지 명
		intent.putExtra("badge_count_package_name", getPackageName());
		// AndroidManifest.xml에 정의된 메인 activity 명
		intent.putExtra("badge_count_class_name", "com.doubleh.lumidiet.LoginActivity");
		sendBroadcast(intent);
	}
}
