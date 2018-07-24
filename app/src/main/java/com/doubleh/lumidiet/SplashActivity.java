package com.doubleh.lumidiet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class SplashActivity extends BaseActivity {
	String key = "isFirst";
	PopupWindow sPopup;
	RelativeLayout progressLayer;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			finish();
		}
	};
	boolean isChk = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setDisplay(R.id.splash_activity);

		progressLayer = (RelativeLayout) findViewById(R.id.progress_layer);
    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (!hasFocus)
			return;

		//Log.d(TAG, "test: "+getLanguage());
		// 앱 실행 최초 1회만 실행, 한글판에서만 구동
		if (getLanguage().equalsIgnoreCase("ko") && getSharedPreferences(key, MODE_PRIVATE).getBoolean(key, true)) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					showPermissionInfoPopup();
				}
			}, 2000);
		}
		else {
            showUsePedometerPopupIfNeeded();
		}
	}

    private void showUsePedometerPopupIfNeeded() {
        final SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        if (prefs.contains("isEnabled")) {
            handler.sendEmptyMessageDelayed(0, 2000);
        } else {
            showUsePedometerPopup();
        }
    }

    void showPermissionInfoPopup() {
		// 권한 안내 팝업
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_permission, null);
		progressLayer.setVisibility(View.VISIBLE);
		sPopup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		sPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		// ok - Login activity go
		layout.findViewById(R.id.popup_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sPopup.dismiss();
				sPopup = null;

				showAutoConnectInfoPopup();
			}
		});
	}

	void showAutoConnectInfoPopup() {
		// 블루투스 자동 연결 설정 팝업
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_bluetooth_setting, null);
		sPopup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		sPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);
		layout.findViewById(R.id.chk_btn).setActivated(isChk);
		layout.findViewById(R.id.chk_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isChk = !isChk;
				v.setActivated(isChk);
			}
		});

		// ok - 만보기 사용 설정
		layout.findViewById(R.id.popup_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sPopup.dismiss();
				sPopup = null;
				progressLayer.setVisibility(View.INVISIBLE);
				getSharedPreferences(Preferences_BLE, MODE_PRIVATE).edit().putBoolean(KEY_AUTO, isChk).commit();
				getSharedPreferences(key, MODE_PRIVATE).edit().putBoolean(key, false).commit();

                showUsePedometerPopupIfNeeded();
			}
		});
	}

    void showUsePedometerPopup() {
        // 만보기 사용 설정 팝업
        isChk = true; // 기본은 사용

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_pedometer_setting, null);
        progressLayer.setVisibility(View.VISIBLE);
        sPopup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        sPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);
        layout.findViewById(R.id.chk_btn).setActivated(isChk);
        layout.findViewById(R.id.chk_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChk = !isChk;
                v.setActivated(isChk);
            }
        });

        // ok - Login activity go
        layout.findViewById(R.id.popup_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sPopup.dismiss();
                sPopup = null;
                progressLayer.setVisibility(View.INVISIBLE);

                final SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("isEnabled", isChk).apply();

                handler.sendEmptyMessageDelayed(0, 25);
            }
        });
    }
}
