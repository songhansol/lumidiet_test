package com.doubleh.lumidiet.utils;

import android.app.Activity;
import android.widget.Toast;

import com.doubleh.lumidiet.MainActivity;
import com.doubleh.lumidiet.R;
import com.facebook.login.LoginManager;

import static com.doubleh.lumidiet.BaseActivity.nowActivity;
import static com.doubleh.lumidiet.MainActivity.mainContext;

/**
 * Created by byj05 on 2016-11-04.
 * Reference : http://dsnight.tistory.com/14
 */

public class BackPressCloseHandler {

    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            if (nowActivity.equalsIgnoreCase("MainActivity") && ((MainActivity) mainContext).getUserData().getFacebook()) {
                LoginManager.getInstance().logOut();
            }

            //activity.finish();
            activity.finishAffinity();
            System.runFinalization();
            System.exit(0);

            toast.cancel();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(activity, activity.getString(R.string.exit_msg), Toast.LENGTH_SHORT);
        toast.show();
    }
}
