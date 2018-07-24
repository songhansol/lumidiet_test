package com.doubleh.lumidiet;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.doubleh.lumidiet.data.FirmwareData;
import com.doubleh.lumidiet.data.UserData;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "BaseActivity";

    public static ArrayList<AppCompatActivity> activityList = null;
    public static AppCompatActivity connectActivity = null;

    private View decorView;
    private int uiOption;
    private int currentApiVersion;
    private static float mDensity = 0.0f;
    protected static Context mContext;

    public static final String Preferences_LOGIN						= "login";
    public static final String Preferences_LOGIN_AUTO					= "auto_login";
    public static final String Preferences_MK							= "masterkey";
    public static final String Preferences_ID							= "id";
    public static final String Preferences_PW							= "pw";
    public static final String Preferences_NOTICE_HISTORY_VERSION	= "notice_history_version";
    public static final String Preferences_HELP_HISTORY_VERSION		= "help_history_version";
    public static final String Preferences_BELT_HISTORY_VERSION		= "belt_history_version";
    public static final String Preferences_LDI_HISTORY_VERSION		= "ldi_history_version";
    public static final String Preferences_PROFILE_IMAGE_PATH		= "profile_img_path";
    public static final String Preferences_LDI_STANDARD_DATE			= "ldi_standard_date";
    public static final String Preferences_LDI_WEEK_AVG				= "ldi_week_avg";
    public static final String Preferences_LDI_WEEK					= "ldi_week";
    public static final String KEY_CUR									= "current";
    public static final String KEY_DATE									= "last_continue_date";
    public static final String KEY_MAX									= "maximum";
    public static final String KEY_BADGE_COUNT							= "badge";
	public static final String Preferences_BLE							= "ble";
	public static final String KEY_AUTO									= "auto";
    public static final String Preferences_PERMISSION					= "permission";
    public static final String KEY_STORAGE								= "storage";
    public static final String KEY_CAMERA								= "camera";

    private static UserData     userData;
    private static FirmwareData firmwareData;
    static public float scale;

    // language 정보
    public static String language;

    // Notification 설정을 위한 코드
    public static String nowActivity = "";
    public static boolean isRun = false;

    // test version, test server and code
    public final static boolean isTest                             = true;
    // 벨트 사용 시간, 최소 측정 인증 시간, 종료 예상 시간의 변경시 사용, MainActivity 사용
    public final static boolean is3min							    = false;
    // LDI 측정 화면에서 보여줄 것인지에 대한 내용, MeasureLDIFragment 사용
    public final static boolean isLDIView						    = false;
    // firmware 업데이트를 막는 용도, true면 업데이트를 무조건 진행하지 않음, MainActivity 사용, 미사용
    //public final static boolean isFirmwareForceNotUpdate	= false;
    // firmware 강제 업데이트용, true 면 강제 업데이트, MainActivity 사용
    public final static boolean isFirmwareForceUpdate		    = false;

    public static void setLanguage(String language) {
        BaseActivity.language = language;
    }

    public static String getLanguage() {
        return language;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (activityList == null)
            activityList = new ArrayList<>();

        mContext = getApplicationContext();

        if (getDensity() == 0.0f) {
            setDensity();
        }

        /*if (userData == null)
            createUserData();*/

        if (firmwareData == null) {
            firmwareData = new FirmwareData();
        }

        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        uiOption = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(uiOption);
            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
            {
                @Override
                public void onSystemUiVisibilityChange(int visibility)
                {
                    if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                    {
                        hideUI();
                    }
                }
            });
        }
    }

    public static void setScale(float scale) {
        BaseActivity.scale = scale;
    }

    public static float getScale() {
        return scale;
    }

    public static void createUserData() {
        if (userData == null || !userData.getMasterKey().equals("null")) {
            if (BuildConfig.DEBUG) Log.d("BaseActivity", "create new user data");
            userData = new UserData();
        } else {
            if (BuildConfig.DEBUG) Log.d("BaseActivity", "user data is null");
        }
    }

    public void resetUserData() {
        createUserData();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onWindowFocusChanged() hasFocus=" + hasFocus);
        //super.onWindowFocusChanged(hasFocus);

        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            hideUI();
        }
    }

    final public void hideUI()
    {
        decorView.setSystemUiVisibility(uiOption);
    }

    final public void setDisplay(@IdRes int id) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setDisplay()");

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getRealSize(size);
        int screen_width = size.x;
        int screen_height = size.y;

        RelativeLayout mRLayout = (RelativeLayout) findViewById(id);

        int layout_width = mRLayout.getLayoutParams().width;
        int layout_height = mRLayout.getLayoutParams().height;

        float scale = 1.0f;

        if (layout_height != screen_height)
        {
            scale = (float)screen_height / (float)layout_height;

            if (layout_width * scale > screen_width)
            {
                scale = (float)screen_width / (float)layout_width;
                if (BuildConfig.DEBUG) Log.d(TAG, "match width scale: "+scale);
                mRLayout.setScaleX(scale);
                mRLayout.setScaleY(scale);
            }
            else
            {
                if (BuildConfig.DEBUG) Log.d(TAG, "match height scale: "+scale);
                mRLayout.setScaleX(scale);
                mRLayout.setScaleY(scale);
            }
        }
        //mWidthPixels = mRLayout.getLayoutParams().width;
        //mHeightPixels = mRLayout.getLayoutParams().height;
    }

    final public void setDisplayDrawerLayout(@IdRes int id)
    {
        if (BuildConfig.DEBUG) Log.d(TAG, "setDisplay()");

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getRealSize(size);
        int screen_width = size.x;
        int screen_height = size.y;

        DrawerLayout mRLayout = (DrawerLayout) findViewById(id);

        int layout_width = mRLayout.getLayoutParams().width;
        int layout_height = mRLayout.getLayoutParams().height;

        float scale = 1.0f;

        if (layout_height != screen_height)
        {
            scale = (float)screen_height / (float)layout_height;

            if (layout_width * scale > screen_width)
            {
                scale = (float)screen_width / (float)layout_width;
                if (BuildConfig.DEBUG) Log.d(TAG, "match width scale: "+scale);
                mRLayout.setScaleX(scale);
                mRLayout.setScaleY(scale);
            }
            else
            {
                if (BuildConfig.DEBUG) Log.d(TAG, "match height scale: "+scale);
                mRLayout.setScaleX(scale);
                mRLayout.setScaleY(scale);
            }
        }
    }

    public final static boolean isValidId(CharSequence target) {
        Pattern ID = Pattern.compile("[a-zA-Z0-9_\\-@.ÇçĞğIıİiÖöŞşÜü]{2,40}");
        Pattern EMAILID = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,20}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,11}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,7}" +
                        ")+");

        return !TextUtils.isEmpty(target) && ID.matcher(target).matches();
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public final static boolean isValidPassword(CharSequence target) {
        Pattern PASSWORD1 = Pattern.compile("^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{6,16}$");
        Pattern PASSWORD2 = Pattern.compile("^(?=.*[!@#$%^*+=-]+)(?=.*[a-zA-Z]|.*[0-9]+).{6,16}$");
        return !TextUtils.isEmpty(target) &&
                (PASSWORD1.matcher(target).matches() || PASSWORD2.matcher(target).matches());
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        hideUI();
    }

    protected static void setDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        mDensity = metrics.density;
    }

    public final float getDensity() {
        return mDensity;
    }

    public static void showSoftKeyboardFromFocusedView(final Context context, final View view) {
        //final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        //imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideSoftKeyboardFromFocusedView(final Context context, final View view) {
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public UserData getUserData() {
        return userData;
    }

    public FirmwareData getFirmwareData() {
        return firmwareData;
    }
}
