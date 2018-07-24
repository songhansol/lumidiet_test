package com.doubleh.lumidiet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class GuideActivity extends BaseActivity {

    String TAG = "GuideActivity";
    Button prev_Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        activityList.add(this);

        prev_Btn = (Button) findViewById(R.id.guide_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.guide_flayout, new GuideInfoFragment()).commit();
        }
        setDisplay(R.id.guide_activity);
    }

    public void setFragment(int index) {
        if (index == 0) {
            getFragmentManager().beginTransaction().replace(R.id.guide_flayout, new GuideInfoFragment()).commit();
        }
        else if (index == 1) {
            getFragmentManager().beginTransaction().replace(R.id.guide_flayout, new GuideContentFragment()).commit();
        }
    }

    public void startMainActivity() {
        // notice 및 기타 정보를 받아올 수 없으므로 login activity 를 이용
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
                if (BuildConfig.DEBUG) Log.d(TAG, "activity finish");
				activityList.remove(this);
				finish();
			}
		}, 500);
        if (BuildConfig.DEBUG) Log.d(TAG, "basic login start");
		((LoginActivity) activityList.get(0)).basicLogin();
    }
}
