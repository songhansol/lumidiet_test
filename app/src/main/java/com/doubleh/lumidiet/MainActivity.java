package com.doubleh.lumidiet;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.doubleh.lumidiet.ble.BluetoothLeService;
import com.doubleh.lumidiet.ble.DeviceConnectActivity;
import com.doubleh.lumidiet.ble.DfuService;
import com.doubleh.lumidiet.ble.MessageEvent;
import com.doubleh.lumidiet.common.Constants;
import com.doubleh.lumidiet.data.BeltHistory;
import com.doubleh.lumidiet.data.BeltStatus;
import com.doubleh.lumidiet.data.HelpData;
import com.doubleh.lumidiet.data.LDIValue;
import com.doubleh.lumidiet.data.NoticeData;
import com.doubleh.lumidiet.data.WalkLog;
import com.doubleh.lumidiet.pedometer.Database;
import com.doubleh.lumidiet.pedometer.util.Util;
import com.doubleh.lumidiet.utils.BackPressCloseHandler;
import com.doubleh.lumidiet.utils.BigEndianByteHandler;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.doubleh.lumidiet.utils.HexEditor;
import com.doubleh.lumidiet.utils.HoloCircularProgressBar;
import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.LumiListView;
import com.doubleh.lumidiet.utils.MyCalendarUtil;
import com.doubleh.lumidiet.utils.NumberFormatUtil;
import com.doubleh.lumidiet.utils.OnSingleClickListener;
import com.doubleh.lumidiet.utils.SlidingImage_Adapter;
import com.google.gson.Gson;
import com.viewpagerindicator.CirclePageIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static com.doubleh.lumidiet.service.NotificationService.KEY_SET_NOTI;
import static com.doubleh.lumidiet.service.NotificationService.NAME_NOTIFICATION;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_NAME;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_VERSION;


public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, SensorEventListener {
    String TAG = "MainActivity";

    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    private BackPressCloseHandler backPressCloseHandler;
    DrawerLayout drawerLayout;
    Button /*menu_Btn, */aboutldi_Btn, detail_Btn, connect_Btn;
    RelativeLayout measureLDI_Btn, menu_Btn;

    protected boolean mAnimationHasEnded = true;
    private HoloCircularProgressBar mHoloCircularProgressBar;
    private ObjectAnimator mProgressBarAnimator;

    Fragment mainLDIUsageStepsRecordFragment, nowContentFragment, prevContentFragment;

    public static final String FACEBOOK_PHOTO_PREFIX    = "https://graph.facebook.com/";
    public static final String FACEBOOK_PHOTO_POSTFIX   = "/picture?width=250&height=250";

    public static final int CONTENT_HOME						= 0;
    public static final int CONTENT_MEASURE_LDI          		= 1;
    public static final int CONTENT_USAGE_STATISTICS    		= 2;
    public static final int CONTENT_NOTICE                		= 3;
    public static final int CONTENT_DIET_INFO            		= 4;
    public static final int CONTENT_GUIDE                		= 5;
	@Deprecated
    public static final int CONTENT_ALARM                		= 6;
    public static final int CONTENT_MY_INFO              		= 7;
    public static final int CONTENT_MY_INFO_ACCOUNT     		= 71;
    public static final int CONTENT_MY_INFO_INFO        		= 72;
    public static final int CONTENT_MY_INFO_PW_CHANGE   		= 73;
    public static final int CONTENT_MY_INFO_MEMBER_LEAVE	= 74;
    public static final int CONTENT_SUPPORT               		= 8;
    public static final int CONTENT_APP_INFO                	= 9;
	public static final int CONTENT_SET						= 10;
	public static final int CONTENT_SET_MAGNIFIER           	= 11;
	public static final int CONTENT_SET_ALARM					= 12;
	public static final int CONTENT_SET_BLUETOOTH				= 13;

    private final int BLE_TIMEOUT = 5000;

    public int MAX_USING_TIME = 30 * 60 * 1000;
    private int MIN_USING_TIME = 2 * 60 * 1000;
    private int contentID = -1;
    //private String deviceNamePrefix = "Lumi-S-";

	// pop-up state
	public static final int ABOUT				= 1;
	public static final int NOTUSE				= 2;
	public static final int COMPLETE			= 3;
	public static final int HISTORY				= 4;
	@Deprecated
	public static final int BATTERYLOW			= 5;
	public static final int FIRMWARE			= 6;

	int popupState = NONE;

    PopupWindow popup = null;
    RelativeLayout popup_back_Layer;
    ProgressBar mProgressBar;
    TextView progress_TextView, body_TextView;
    RelativeLayout firmwareOK_Btn;

    // popup
    private ViewPager mPager;
    //private int currentPage = 0;
    //private int NUM_PAGES = 0;
    private final Integer[] IMAGES= {R.drawable.popup_guide_1, R.drawable.popup_guide_2, R.drawable.popup_guide_3, R.drawable.popup_guide_4, R.drawable.popup_guide_5};
    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();

    //belt
    private ImageView battery_ImageView;
    private DatabaseManager mDatabaseManager;
    ArrayList<BeltHistory> mBeltHistories = new ArrayList<>();
    private LumiListView listView;
    HistoryListAdapter listAdapter;
    BeltStatus mBeltStatus = null;
    LDIValue mLDIValue = null;

    Object beltResponse;
    public static Context mainContext;
    private View mainUsageOffView = null, mainUsageOnView = null;
    String mDeviceName = "";
    String mDeviceAddress = "";

    private float timerTime = 0.0f;
    private Timer mTimer = null, mStatusTimer = null, mBatteryTimer = null;
    private Handler connectHandler1 = null, connectHandler2 = null;
    private Runnable connectRunnable1 = new Runnable() {
        @Override
        public void run() {
            if (BuildConfig.DEBUG) Log.d(TAG, "connectHandler1, connectRunnable1, cannot response belt status(51)");
            if (mBeltStatus == null) {
                if (BuildConfig.DEBUG) Log.d(TAG, "getStatusFromBelt(), mBeltStatus is null");
                connectHandler2.removeCallbacks(connectRunnable2);

				if (isFirstConnect) {
					if (BuildConfig.DEBUG) Log.d(TAG, "getStatusFromBelt(), connect retry");
					connectStart();
					isFirstConnect = false;
					return;
				}

                if (mConnected) {
                    HHFU = true;
                    mConnected = false;
                    mBluetoothLeService.disconnect();
                    unbindService(mServiceConnection);
                    mServiceConnection.onServiceDisconnected(null);
                    mDeviceAddress = null;
                    mDeviceName = null;
                }
                connect_Btn.setEnabled(true);

                if (mStatusTimer != null) {
                    mStatusTimer.cancel();
                    mStatusTimer.purge();
                    mStatusTimer = null;
                }
			}
            if (connectActivity != null) {
				isFirstConnect = true;
                connectActivity.finish();
                connectActivity = null;
            }
        }
    };
    private Runnable connectRunnable2 = new Runnable() {
        @Override
        public void run() {
            if (BuildConfig.DEBUG) Log.d(TAG, "connectHandler2, connectRunnable2, was not send getStatusFromBelt message yet");
            if (!isSendStatus) {
                if (BuildConfig.DEBUG) Log.d(TAG, "connectStart(), isSendStatus: "+isSendStatus);
                connectHandler1.removeCallbacks(connectRunnable1);

				if (isFirstConnect) {
					if (BuildConfig.DEBUG) Log.d(TAG, "connectStart(), connect retry");
					connectStart();
					isFirstConnect = false;
					return;
				}

				if (mConnected) {
                    mConnected = false;
                    mBluetoothLeService.disconnect();
                    unbindService(mServiceConnection);
                    mServiceConnection.onServiceDisconnected(null);
                    mDeviceAddress = null;
                    mDeviceName = null;
                }

				if (mStatusTimer != null) {
					mStatusTimer.cancel();
					mStatusTimer.purge();
					mStatusTimer = null;
				}

                if (connectActivity != null) {
					isFirstConnect = true;
                    connectActivity.finish();
                    connectActivity = null;
                }
                connect_Btn.setEnabled(true);
            }
        }
    };

    private BluetoothLeService mBluetoothLeService;
    protected EventBus eventBus = null;
    private boolean mConnected = false;
    private boolean isTimer = false;
    private boolean isShowNotUsePopup = true;
    private boolean isFirmwarePopup = false, isCompletePopup = false;
    protected boolean isSendStatus = false, isStart = true;
    private boolean HHFU = true, isStartHistory = true, isFirstConnect = true;

    private int todayOffset, total_start, goal, since_boot, total_days;
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

    // Code to manage Service lifecycle.
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            if (BuildConfig.DEBUG) Log.d(TAG, "ServiceConnection() onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (mDeviceAddress != "") {
                mBluetoothLeService.connect(mDeviceAddress);
                mConnected = true;
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (BuildConfig.DEBUG) Log.d(TAG, "ServiceConnection() onServiceDisconnected");
            mBluetoothLeService = null;
            mConnected = false;
        }
    };

    private Gson gson;
    private boolean isRunUnpostedPutWalkLogs = false;
    private ArrayList<WalkLog> cumulativeStepsAndRankWalkLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 로그인을 제외한 모든 액티비티 삭제, remove all prev activity without login activity
        for (int i = 1;;) {
			if (activityList == null)
				break;

            if (activityList.size() <= 1)
                break;

            activityList.get(i).finish();
            activityList.remove(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		magMode = getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).getBoolean(PREFERENCES_MAGNIFIER, false);

        activityList.add(this);
        nowActivity = "MainActivity";

        setDisplayDrawerLayout(R.id.main_activity);

        //
        if (is3min) {
            MAX_USING_TIME = 3 * 60 * 1000;
            MIN_USING_TIME = 30 * 1000;
        }

        mDeviceAddress = "";
        mDeviceName = "";
        mainContext = this;
        backPressCloseHandler = new BackPressCloseHandler(this);

        for (int i = 0; i < IMAGES.length; i++)
            ImagesArray.add(IMAGES[i]);

        mDatabaseManager = DatabaseManager.getInstance(getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);

        drawerLayout = (DrawerLayout) findViewById(R.id.main_activity);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                menu_Btn.setEnabled(false);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (!getDrawerLayoutLocked())
                    menu_Btn.setEnabled(true);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // 우선은 사용 페이지 기본
        // 내용 변경은 로직 필요
        mainLDIUsageStepsRecordFragment = new MainLDIUsageStepsRecordFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_flayout_ldi_usage, mainLDIUsageStepsRecordFragment);
        fragmentTransaction.commit();

        menu_Btn = (RelativeLayout) findViewById(R.id.main_btn_menu);
        menu_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(findViewById(R.id.nav_view))) {
                    return;
                }
                else {
                    drawerLayout.openDrawer(findViewById(R.id.nav_view));
                }
            }
        });

        popup_back_Layer = (RelativeLayout) findViewById(R.id.main_popup_layer);
        mProgressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        aboutldi_Btn = (Button) findViewById(R.id.main_btn_about_ldi);
        aboutldi_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLDIAboutPopup();
            }
        });

        detail_Btn = (Button) findViewById(R.id.main_btn_detail);
        detail_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentFrameLayout(CONTENT_USAGE_STATISTICS);
            }
        });

        //connectHandler1 = new Handler(Looper.getMainLooper());
        connectHandler1 = new Handler();
        connectHandler2 = new Handler();

        measureLDI_Btn = (RelativeLayout) findViewById(R.id.main_btn_measure_ldi);
        measureLDI_Btn.setOnClickListener(this);
        setMeasureLDIButtonEnabled(false);

		setButtonOnClickListener();

		setUserProfile();

        // belt info setting
        setBeltInfoLayer();

        eventBus = EventBus.getDefault();
        eventBus.register(this);
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);

		findViewById(R.id.main_activity).getRootView().setDrawingCacheEnabled(true);
		captureView = (ImageView) findViewById(R.id.main_capture);

		if (isTest) {
			findViewById(R.id.isTest).setVisibility(View.VISIBLE);
		}

        gson = new Gson();

        updateCumulativeStepsAndRanking(0, 0);
        requestGetWalkSummary();
		requestCumulativeStepsAndRanking();
    }

    void showLDIAboutPopup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "showLDIAboutPopup()");

		popupState = ABOUT;
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type1, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type1_btn_ok);

		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup_back_Layer.setVisibility(View.INVISIBLE);
				popup = null;
				popupState = NONE;
			}
		});

		mPager = (ViewPager) layout.findViewById(R.id.popup_type1_pager);
		mPager.setAdapter(new SlidingImage_Adapter(mContext, ImagesArray));
		CirclePageIndicator indicator = (CirclePageIndicator) layout.findViewById(R.id.popup_type1_indicator);

		indicator.setViewPager(mPager);

		indicator.setRadius(5 * getDensity());

		// Pager listener over indicator
		indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				//currentPage = position;
			}

			@Override
			public void onPageScrolled(int pos, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int pos) {

			}
		});
	}

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume() " + mDeviceAddress);

        SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        boolean isPedometerEnabled = prefs.getBoolean("isEnabled", true);

        if (isPedometerEnabled) {
            runPutWalkLogs();

            startLivePedometerTracking();
        }

        setPedometerOffUI(isPedometerEnabled);

        // 맨 처음 자동으로 단 한번만 들어오도록
        if (isStart) {
            //isStart = false;
            /*new Handler().post(new Runnable() {
                @Override
                public void run() {
                    connectBelt();
                }
            });*/
            return;
        }

        if (HHFU) {
            HHFU = false;
            return;
        }
        // 벨트 연결 시작
        connectStart();
    }

    private void startLivePedometerTracking() {
        Database db = Database.getInstance(this);

        if (BuildConfig.DEBUG) db.logState();
        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        goal = prefs.getInt("goal", 10000);
        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        if (!prefs.contains("pauseCount")) {
            SensorManager sm =
                    (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (sensor == null) {
                new AlertDialog.Builder(this).setTitle(R.string.no_sensor)
                        .setMessage(R.string.no_sensor_explain)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(final DialogInterface dialogInterface) {
                                SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
                                prefs.edit().putBoolean("isEnabled", false).apply();
                                setPedometerOffUI(false);
                            }
                        }).setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            } else {
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
            }
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();

        updateTodaySteps();
    }

    private void updateTodaySteps() {
        if (BuildConfig.DEBUG) Log.d(TAG, "UI - since boot: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);
        Log.d(TAG, "steps_today:" + steps_today);

        Database database = Database.getInstance(this);
        database.saveCurrentSteps(since_boot);
        int today_offset = database.getSteps(Util.getToday());
        int steps = database.getCurrentSteps();
        database.close();
        if (steps > 0) {
            if (today_offset == Integer.MIN_VALUE) today_offset = -steps;
            steps = (today_offset + steps);
        }
        Log.d(TAG, "today steps from db:" + steps);
        if (steps_today < steps) {
            Answers.getInstance().logCustom(new CustomEvent(String.format(Locale.US, "sb:%d < db:%d", steps_today, steps)));
            steps_today = steps;
        } else if (steps_today > steps) {
            Answers.getInstance().logCustom(new CustomEvent(String.format(Locale.US, "sb:%d > db:%d", steps_today, steps)));
        }

        if (steps_today >= 0) {
            if (null != mainUsageOnView) {
                TextView tvOnTodaySteps = mainUsageOnView.findViewById(R.id.tvTodaySteps);
                tvOnTodaySteps.setText(formatter.format(steps_today));
                TextView tvMaxSteps = mainUsageOnView.findViewById(R.id.tvMaxSteps);
                if (null != cumulativeStepsAndRankWalkLogs) {
                    if (cumulativeStepsAndRankWalkLogs.size() > 0) {
                        WalkLog walkLog = cumulativeStepsAndRankWalkLogs.get(0);
                        int cumulative_steps = Integer.valueOf(walkLog.getSumscope());
                        tvMaxSteps.setText(NumberFormatUtil.commaedNumber(cumulative_steps + steps_today));
                    }
                } else {
                    tvMaxSteps.setText(NumberFormatUtil.commaedNumber(steps_today));
                }
            }
            if (null != mainUsageOffView) {
                TextView tvOffTodaySteps = mainUsageOffView.findViewById(R.id.tvTodaySteps);
                tvOffTodaySteps.setText(formatter.format(steps_today));
                TextView txMaxSteps = mainUsageOffView.findViewById(R.id.tvMaxSteps);
                if(null != cumulativeStepsAndRankWalkLogs) {
                    if (cumulativeStepsAndRankWalkLogs.size() > 0) {
                        WalkLog walkLog = cumulativeStepsAndRankWalkLogs.get(0);
                        int cumulative_steps = Integer.valueOf(walkLog.getSumscope());
                        txMaxSteps.setText(NumberFormatUtil.commaedNumber(cumulative_steps + steps_today));
                    }
                } else {
                    txMaxSteps.setText(NumberFormatUtil.commaedNumber(steps_today));
                }
            }

            updateOnlyTodayStepsAtStepsRecordFragment();
        }
    }

    private void updateOnlyTodayStepsAtStepsRecordFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_flayout_ldi_usage);
        if (null != fragment) {
            if (fragment instanceof MainLDIUsageStepsRecordFragment) {
                ((MainLDIUsageStepsRecordFragment)fragment).updateOnlyTodaySteps();
            }
        }
    }

    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus && isStart) {
			isStart = false;
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					connectBelt(false);
				}
			});
			return;
		}
	}

	public void connectBelt(boolean isButtonTouched) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (BuildConfig.DEBUG) Log.d(TAG, ""+bluetoothAdapter.isEnabled());
		if (getLanguage().equalsIgnoreCase("ko")) {
			if (bluetoothAdapter.isEnabled()) {
				if (!isButtonTouched && !getSharedPreferences(Preferences_BLE, MODE_PRIVATE).getBoolean(KEY_AUTO, true)) {
					// auto connect X
					return;
				}
			} else {
				if (getSharedPreferences(Preferences_BLE, MODE_PRIVATE).getBoolean(KEY_AUTO, true)) {
					bluetoothAdapter.enable();
				} else if (!getSharedPreferences(Preferences_BLE, MODE_PRIVATE).getBoolean(KEY_AUTO, true)) {
					showBLEPopup();
					return;
				}
			}
		} else {
			if (!bluetoothAdapter.isEnabled())
				bluetoothAdapter.enable();
		}

		connect_Btn.setEnabled(false);
		isStartHistory = true;
		isUserTerminated = false;

		if (mConnected) {
			mConnected = false;
			mBluetoothLeService.disconnect();
			mServiceConnection.onServiceDisconnected(null);
			unbindService(mServiceConnection);
		}

		Intent deviceConnectIntent = new Intent(this, DeviceConnectActivity.class);
		startActivity(deviceConnectIntent);
	}

	boolean isChk = true;

	void showBLEPopup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "showBLEPopup()");

		isChk = true;

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_bluetooth_connect, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
		layout.findViewById(R.id.chk_btn).setActivated(isChk);
		layout.findViewById(R.id.chk_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isChk = !isChk;
				v.setActivated(isChk);
			}
		});

		// ok - main activity go
		layout.findViewById(R.id.popup_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getSharedPreferences(Preferences_BLE, MODE_PRIVATE).edit().putBoolean(KEY_AUTO, isChk).commit();
				v.setEnabled(false);
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				bluetoothAdapter.enable();

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						popup.dismiss();
						popup = null;
						popup_back_Layer.setVisibility(View.INVISIBLE);
						connectBelt(true);
					}
				}, 500);
			}
		});
		// cancel
		layout.findViewById(R.id.popup_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				getSharedPreferences(Preferences_BLE, MODE_PRIVATE).edit().putBoolean(KEY_AUTO, isChk).commit();
			}
		});
	}

    public void connectStart() {
        isSendStatus = false;

        if (!mConnected) {
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            if (BuildConfig.DEBUG) Log.d(TAG, "Connect request result=" + result);

            if (result) {
                getStatusFromBelt();
                connectHandler2.postDelayed(connectRunnable2, BLE_TIMEOUT + 1000);
            } else {
                setBeltInfoLayer();
                setMeasureLDIButtonEnabled(mConnected);
            }
        } else {
            setBeltInfoLayer();
            setMeasureLDIButtonEnabled(mConnected);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause()");

        try {
            SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Database db = Database.getInstance(this);
        db.saveCurrentSteps(since_boot);
        db.close();


        //eventBus.unregister(this);
        //DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy()");
        if (mConnected) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mStatusTimer != null) {
            mStatusTimer.cancel();
            mStatusTimer = null;
        }
        eventBus.unregister(this);
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    public void setDevice(String deviceName, String deviceAddress) {
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;

        if (mDeviceAddress == "") {
            if (BuildConfig.DEBUG) Log.d(TAG, "not found device");
            disconnectBelt(true);
            return;
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "device name: " + mDeviceName + "  device address: " + mDeviceAddress);
		getFirmwareData().setCodeName(mDeviceName.substring(mDeviceName.indexOf("-") + 1, mDeviceName.lastIndexOf("-")));
        HHFU = true;
        connectStart();
    }

    public void setUserProfile() {
        TextView tv = (TextView) findViewById(R.id.main_left_menu_txt_id);
        if (getUserData().getFacebook()) {
            tv.setText(getUserData().getName());
        }
        else {
            tv.setText(getUserData().getUserID());
        }

        ImageView iv = (ImageView) findViewById(R.id.main_left_menu_img_profile);
        String path;
        if (getUserData().getFacebook()) {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(FACEBOOK_PHOTO_PREFIX);
            urlBuilder.append(getUserData().getFacebookID());
            urlBuilder.append(FACEBOOK_PHOTO_POSTFIX);

            if (BuildConfig.DEBUG) Log.d(TAG, "profile image url: " + urlBuilder.toString());
            new DownloadImageTask(iv).execute(urlBuilder.toString());
        } else {
            if ((path = getSharedPreferences(getUserData().getMasterKey(),
                    MODE_PRIVATE).getString(Preferences_PROFILE_IMAGE_PATH, null)) != null) {
                File file = new File(path);

                if (file.exists()) {
                    Bitmap thumbnail = BitmapFactory.decodeFile(file.getAbsolutePath());
                    iv.setImageBitmap(thumbnail);
                }
            }
        }
    }

    public void setMeasureLDIButtonEnabled(boolean isEnabled) {
        measureLDI_Btn.setEnabled(isEnabled);
        findViewById(R.id.main_img_measure_ldi).setEnabled(isEnabled);
        if (isEnabled) {
            ((TextView) findViewById(R.id.main_txt_measure_ldi)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFFFFFFFF));
            ((Button) findViewById(R.id.main_left_menu_btn_measure_ldi)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFF2A2A2A));
        }
        else {
            ((TextView) findViewById(R.id.main_txt_measure_ldi)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFFBCBCBC));
            ((Button) findViewById(R.id.main_left_menu_btn_measure_ldi)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFFBCBCBC));
        }
    }

	protected void setBeltInfoLayer() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setBeltInfoLayer()");

        if (mConnected) {
            initCircularProgressBar(mBeltStatus.getUsingTime() * 1000);
        }
        else {
            if (mainUsageOffView == null) {
                RelativeLayout useLayout = (RelativeLayout) View.inflate(this, R.layout.activity_main_disconnect, null);
                useLayout.setGravity(Gravity.CENTER | Gravity.TOP);

                mainUsageOffView = useLayout;

                connect_Btn = (Button) useLayout.findViewById(R.id.main_btn_connect);
                connect_Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connectBelt(true);
                    }
                });
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(Gravity.CENTER_HORIZONTAL);

            ((RelativeLayout) findViewById(R.id.main_rlayout)).removeView(mainUsageOffView);
            ((RelativeLayout) findViewById(R.id.main_rlayout)).removeView(mainUsageOnView);
            ((RelativeLayout) findViewById(R.id.main_rlayout)).addView(mainUsageOffView, params);
        }
    }

    public void initCircularProgressBar(long time) {
        // 벨트 사용 정보 레이아웃 추가 (벨트 연결시)

        long duration = MAX_USING_TIME - time;
        double progress = (double)time / (double)MAX_USING_TIME;
        if (BuildConfig.DEBUG) Log.d(TAG, "duration: " + duration);
        if (BuildConfig.DEBUG) Log.d(TAG, "time: "     + time);
        if (BuildConfig.DEBUG) Log.d(TAG, "progress: " + progress);

        if (duration <= 0)
            duration = 0;

        if (mainUsageOnView == null) {
            RelativeLayout useLayout = (RelativeLayout) View.inflate(this, R.layout.activity_main_use, null);
            useLayout.setGravity(Gravity.CENTER | Gravity.TOP);
            mainUsageOnView = useLayout;

            battery_ImageView = (ImageView) mainUsageOnView.findViewById(R.id.main_img_battery);

            mHoloCircularProgressBar = (HoloCircularProgressBar) mainUsageOnView.findViewById(R.id.main_holoCircularProgressBar);

            mHoloCircularProgressBar.setVisibility(View.VISIBLE);
            mHoloCircularProgressBar.setBackgroundColor(getResources().getColor(R.color.color00FFFFFF));
            mHoloCircularProgressBar.setProgressBackgroundColor(getResources().getColor(R.color.color00FFFFFF));
            mHoloCircularProgressBar.setDrawingCacheBackgroundColor(getResources().getColor(R.color.color00FFFFFF));
            mHoloCircularProgressBar.setProgressColor(getResources().getColor(R.color.colorFF0CCB54));
            mHoloCircularProgressBar.setMarkerEnabled(false);
            mHoloCircularProgressBar.setThumbImageView((ImageView) mainUsageOnView.findViewById(R.id.main_img_circle));
            mHoloCircularProgressBar.showImageView();
            mHoloCircularProgressBar.setWheelSize((int)(4.0f * getDensity()));
            mHoloCircularProgressBar.setCircleStrokeWidth((int)(2.5f * getDensity()));
            mHoloCircularProgressBar.setThumbImageLayout((RelativeLayout) mainUsageOnView.findViewById(R.id.main_img_circle_layout));
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(Gravity.CENTER_HORIZONTAL);

        ((RelativeLayout) findViewById(R.id.main_rlayout)).removeView(mainUsageOffView);
        ((RelativeLayout) findViewById(R.id.main_rlayout)).removeView(mainUsageOnView);
        ((RelativeLayout) findViewById(R.id.main_rlayout)).addView(mainUsageOnView, params);

		updateBatteryView();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((int)Math.floor(duration / 1000 / 60) + 1);
        //stringBuilder.append(getString(R.string.minute_ko));
        ((TextView) mainUsageOnView.findViewById(R.id.main_txt_remain_time)).setText(stringBuilder);

        if (null != cumulativeStepsAndRankWalkLogs) {
            updateWalkLogs(cumulativeStepsAndRankWalkLogs);
        }

        mHoloCircularProgressBar.setStartProgress((float)progress);

        animate(mHoloCircularProgressBar, new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationEnd");
                mAnimationHasEnded = true;
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationStart");
                mAnimationHasEnded = false;
            }
        }, 1.0f, duration);//MAX_USING_TIME * 1000);
    }

    // 1분 마다 배터리 정보 갱신을 위해 분리
    void updateBatteryView() {
		if (getFirmwareData().getCodeName().equals("S")) {
			if (mBeltStatus.getBattery() >= 70) {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step1);
			} else if (mBeltStatus.getBattery() >= 50) {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step2);
			} else if (mBeltStatus.getBattery() >= 30) {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step3);
			} else {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step4);
			}
		} else {
			if (mBeltStatus.getBattery() > 80) {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step1);
			} else if (mBeltStatus.getBattery() > 50) {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step2);
			} else if (mBeltStatus.getBattery() > 30) {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step3);
			} else {
				battery_ImageView.setBackgroundResource(R.drawable.main_battery_step4);
			}
		}
	}

    private void animate(final HoloCircularProgressBar progressBar, final Animator.AnimatorListener listener,
                         final float progress, final long duration) {

        mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
        mProgressBarAnimator.setDuration(duration);

        if (listener != null) {
            mProgressBarAnimator.addListener(listener);
        } else {
            mProgressBarAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationCancel(final Animator animation) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationCancel");
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationEnd");
                    progressBar.setProgress(progress);
                    progressBar.setStartProgress(0.0f);
                }

                @Override
                public void onAnimationRepeat(final Animator animation) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationRepeat");
                }

                @Override
                public void onAnimationStart(final Animator animation) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationStart");
                }
            });
        }
        mProgressBarAnimator.reverse();
        mProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //Log.d(TAG, "onAnimationUpdate(final ValueAnimator animation)");
                progressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });
        progressBar.setMarkerProgress(progress);
        mProgressBarAnimator.start();
    }

    public void usingCalculator() {
        // 누적 사용시간 표시
        long totalUsingTime = 0;
        ArrayList<BeltHistory> histories = mDatabaseManager.selectBeltHistory(Integer.parseInt(getUserData().getMasterKey()));
        for (BeltHistory history : histories) {
            totalUsingTime += history.getUsingTime();
        }

        totalUsingTime /= 60;

        TextView tvCumulativeUsageTime = mainUsageOnView.findViewById(R.id.main_txt_accum_usage_time);
        tvCumulativeUsageTime.setText(getText(R.string.accum_usage_time));
        tvCumulativeUsageTime.append("(");
        tvCumulativeUsageTime.append(getString(R.string.minute));
        tvCumulativeUsageTime.append(") : ");
        SpannableString spannableString = new SpannableString(NumberFormatUtil.commaedNumber(totalUsingTime));
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvCumulativeUsageTime.append(spannableString);

    }

    public void setButtonOnClickListener() {
		findViewById(R.id.main_left_menu_btn_home).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_measure_ldi).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_usage_statistics).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_notice).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_diet_info).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_guide).setOnClickListener(this);
		//findViewById(R.id.main_left_menu_btn_alarm).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_my_info).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_support).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_app_info).setOnClickListener(this);
		//findViewById(R.id.main_left_menu_btn_mag).setOnClickListener(this);
		findViewById(R.id.main_left_menu_btn_setting).setOnClickListener(this);

        setNoticeButton();
        setAlarmButton();
    }

    public void setAlarmButton() {
        Button main_Alarm = (Button) findViewById(R.id.main_btn_alarm);
        main_Alarm.setOnClickListener(this);

        if (getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_SET_NOTI, false)) {
            main_Alarm.setVisibility(View.VISIBLE);
        } else {
            main_Alarm.setVisibility(View.INVISIBLE);
        }
    }

    public void setNoticeButton() {
        Button main_Notice = (Button) findViewById(R.id.main_btn_noti);
        main_Notice.setOnClickListener(this);

        if (BuildConfig.DEBUG) Log.d(TAG, "not read notice count check: " + getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).getInt(KEY_BADGE_COUNT, 0));
        if (getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).getInt(KEY_BADGE_COUNT, 0) <= 0)
            main_Notice.setVisibility(View.INVISIBLE);
        else
            main_Notice.setVisibility(View.VISIBLE);

        // 기존에 존재하는 공지는 count 되지 않음, 직접 확인
        if (main_Notice.getVisibility() == View.INVISIBLE) {
            for (NoticeData d : mDatabaseManager.selectNoticeHistory(getUserData().getMasterKey(), getLanguage())) {
                if (!d.getRead()) {
                    main_Notice.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
        if (main_Notice.getVisibility() == View.INVISIBLE) {
            for (HelpData d : mDatabaseManager.selectHelpHistory(getUserData().getMasterKey())) {
                if (!d.getRead()) {
                    main_Notice.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }

    /**
     * Set DrawerLayout LOCK_MODE_LOCKED_CLOSED or LOCK_MODE_UNLOCKED
     * @param locked    true is LOCK_MODE_LOCKED_CLOSED, false is LOCK_MODE_UNLOCKED
     */
    public void setDrawerLayoutLocked(boolean locked) {
        if (locked) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        menu_Btn.setEnabled(!locked);
        detail_Btn.setEnabled(!locked);
    }

    /**
     * Get DrawerLayout LOCK_MODE_LOCKED_CLOSED or LOCK_MODE_UNLOCKED
     * @return  true is LOCK_MODE_LOCKED_CLOSED, false is LOCK_MODE_UNLOCKED
     */
    public boolean getDrawerLayoutLocked() {
        if (drawerLayout.getDrawerLockMode(findViewById(R.id.nav_view)) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            return true;
        }
        else {
            return false;
        }
    }

    public void setContentFrameLayout(int contentID) {
        this.contentID = contentID;
        if (contentID == CONTENT_HOME) {
            if (nowContentFragment != null) {
                getFragmentManager().beginTransaction().remove(nowContentFragment).commit();
                nowContentFragment = null;
            }
            setDrawerLayoutLocked(false);
        }
        else if (contentID == CONTENT_MEASURE_LDI) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new MeasureLDIFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_USAGE_STATISTICS) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new StatisticsFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_NOTICE) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new NoticeFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_DIET_INFO) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new DietInfoFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_GUIDE) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new GuideFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_ALARM) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new AlarmFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_MY_INFO) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new MyInfoFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_SUPPORT) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new SupportFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_APP_INFO) {
            setDrawerLayoutLocked(true);
            if (nowContentFragment == null) {
                nowContentFragment = new AppInfoFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_MY_INFO_ACCOUNT) {
            if (nowContentFragment != null) {
                prevContentFragment = nowContentFragment;
                nowContentFragment = new AccountManagementFragment();
                getFragmentManager().beginTransaction().add(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_MY_INFO_INFO) {
            if (nowContentFragment != null) {
                prevContentFragment = nowContentFragment;
                nowContentFragment = new InfoManagementFragment();
                getFragmentManager().beginTransaction().add(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_MY_INFO_PW_CHANGE) {
            if (nowContentFragment != null) {
                prevContentFragment = nowContentFragment;
                nowContentFragment = new ChangePWFragment();
                getFragmentManager().beginTransaction().add(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_MY_INFO_MEMBER_LEAVE) {
            if (nowContentFragment != null) {
                prevContentFragment = nowContentFragment;
                nowContentFragment = new MemberLeaveFragment();
                getFragmentManager().beginTransaction().add(R.id.main_flayout, nowContentFragment).commit();
            }
        }
        else if (contentID == CONTENT_SET) {
			setDrawerLayoutLocked(true);
			if (nowContentFragment == null) {
				nowContentFragment = new SettingsFragment();
				getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
			}
		}
		else if (contentID == CONTENT_SET_MAGNIFIER) {
			/*setDrawerLayoutLocked(true);
			if (nowContentFragment == null) {
				nowContentFragment = new SetMagnifierFragment();
				getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
			}*/

			if (nowContentFragment != null) {
				prevContentFragment = nowContentFragment;
				nowContentFragment = new SetMagnifierFragment();
				getFragmentManager().beginTransaction().add(R.id.main_flayout, nowContentFragment).commit();
			}
		}
		else if (contentID == CONTENT_SET_ALARM) {
			if (nowContentFragment != null) {
				prevContentFragment = nowContentFragment;
				nowContentFragment = new AlarmFragment();
				getFragmentManager().beginTransaction().add(R.id.main_flayout, nowContentFragment).commit();
			} else {
				setDrawerLayoutLocked(true);
				nowContentFragment = new AlarmFragment();
				getFragmentManager().beginTransaction().replace(R.id.main_flayout, nowContentFragment).commit();
			}
		}
		else if (contentID == CONTENT_SET_BLUETOOTH) {
			if (nowContentFragment != null) {
				prevContentFragment = nowContentFragment;
				nowContentFragment = new SetBluetoothFragment();
				getFragmentManager().beginTransaction().add(R.id.main_flayout, nowContentFragment).commit();
			}
		}
    }

    public void setPrevContentFragmentLayout() {
		if (prevContentFragment == null) {
			setContentFrameLayout(CONTENT_HOME);
			return;
		}

        getFragmentManager().beginTransaction().remove(nowContentFragment).commit();
        nowContentFragment = prevContentFragment;
        prevContentFragment = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.main_left_menu_btn_home:
                if (BuildConfig.DEBUG) Log.d(TAG, "home");
                setContentFrameLayout(CONTENT_HOME);
                break;
            case R.id.main_left_menu_btn_measure_ldi:
            case R.id.main_btn_measure_ldi:
                if (!mConnected)
                    return;
                if (BuildConfig.DEBUG) Log.d(TAG, "measure");
                setContentFrameLayout(CONTENT_MEASURE_LDI);
                break;
            case R.id.main_left_menu_btn_usage_statistics:
                if (BuildConfig.DEBUG) Log.d(TAG, "usage statictics");
                setContentFrameLayout(CONTENT_USAGE_STATISTICS);
                break;
            case R.id.main_left_menu_btn_notice:
            case R.id.main_btn_noti:
                if (BuildConfig.DEBUG) Log.d(TAG, "notice");
                setContentFrameLayout(CONTENT_NOTICE);
                break;
            case R.id.main_left_menu_btn_diet_info:
                if (BuildConfig.DEBUG) Log.d(TAG, "diet infomation");
                setContentFrameLayout(CONTENT_DIET_INFO);
                break;
            case R.id.main_left_menu_btn_guide:
                if (BuildConfig.DEBUG) Log.d(TAG, "product guide");
                setContentFrameLayout(CONTENT_GUIDE);
                break;
            case R.id.btn_set_alarm:
				if (BuildConfig.DEBUG) Log.d(TAG, "alarm left");
				setContentFrameLayout(CONTENT_SET_ALARM);
				break;
            case R.id.main_btn_alarm:
                if (BuildConfig.DEBUG) Log.d(TAG, "alarm home");
                setContentFrameLayout(CONTENT_ALARM);
                break;
            case R.id.main_left_menu_btn_my_info:
                if (BuildConfig.DEBUG) Log.d(TAG, "my info");
                setContentFrameLayout(CONTENT_MY_INFO);
                break;
            case R.id.main_left_menu_btn_support:
                if (BuildConfig.DEBUG) Log.d(TAG, "customer support");
                setContentFrameLayout(CONTENT_SUPPORT);
                break;
            case R.id.main_left_menu_btn_app_info:
                if (BuildConfig.DEBUG) Log.d(TAG, "app info");
                setContentFrameLayout(CONTENT_APP_INFO);
                break;
			case R.id.main_left_menu_btn_setting:
				if (BuildConfig.DEBUG) Log.d(TAG, "setting");
				setContentFrameLayout(CONTENT_SET);
				break;
			/*case R.id.main_left_menu_btn_mag:
				setContentFrameLayout(CONTENT_SET_MAGNIFIER);
				break;*/
        }
        if (drawerLayout.isDrawerOpen(findViewById(R.id.nav_view))) {
            drawerLayout.closeDrawer(findViewById(R.id.nav_view));
        }
    }

    @Subscribe
    public void onEvent(MessageEvent event) {
        switch (event.event){
            case BLE_STATE_CONNECTED:
            {
                mConnected = true;
                if (BuildConfig.DEBUG) Log.d(TAG, "belt connected");
                break;
            }
            case BLE_STATE_DISCONNECTED:
            {
                if (BuildConfig.DEBUG) Log.d(TAG, "belt disconnected");
                isClearAll = false;
                connectHandler1.removeCallbacks(connectRunnable1);
                connectHandler2.removeCallbacks(connectRunnable2);
                HHFU = true;
                if (mConnected) {
                    mConnected = false;
                    mBluetoothLeService.disconnect();
                    unbindService(mServiceConnection);
                    mServiceConnection.onServiceDisconnected(null);
                    mDeviceAddress = null;
                    mDeviceName = null;
                }
                mBeltStatus = null;
                getFirmwareData().setRealVersion("");
                if (mStatusTimer != null) {
                    mStatusTimer.cancel();
                    mStatusTimer.purge();
                    mStatusTimer = null;
                }
                if (mBatteryTimer != null) {
					mBatteryTimer.cancel();
					mBatteryTimer.purge();
					mBatteryTimer = null;
				}

                mBeltHistories.clear();
                sendClearHistories.clear();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (BuildConfig.DEBUG) Log.d(TAG, "complete popup? " + isCompletePopup + ", firmware popup? " + isFirmwarePopup);
                        if (!isCompletePopup && !isFirmwarePopup) {
                            if (popup != null) {
                                popup.dismiss();
                                popup = null;
                                popupState = NONE;
                            }
                            popup_back_Layer.setVisibility(View.INVISIBLE);
                        }

                        if (contentID == CONTENT_MEASURE_LDI) {
                            getFragmentManager().beginTransaction().remove(nowContentFragment).commit();
                            nowContentFragment = null;
                        }

                        connect_Btn.setEnabled(true);
                        setBeltInfoLayer();
                        setMeasureLDIButtonEnabled(mConnected);

                        if (connectActivity != null) {
                            connectActivity.finish();
                            connectActivity = null;
                        }
                    }
                });
                break;
            }
            case BLE_STATE_OTHER:
            {
                mConnected = false;
                mBeltStatus = null;
                unbindService(mServiceConnection);
                mServiceConnection.onServiceDisconnected(null);
                if (BuildConfig.DEBUG) Log.d(TAG, "belt state_other");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connect_Btn.setEnabled(true);
                        setBeltInfoLayer();
                        setMeasureLDIButtonEnabled(mConnected);
                    }
                });
                break;
            }
            case GATT_SERVICES_DISCOVERED:
            {
                if (BuildConfig.DEBUG) Log.d(TAG, "gatt services discovered");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mBluetoothLeService == null || mBluetoothLeService./*getLumiCharacteristic()*/getLumiCharacteristicNCheckAllServices() == null) {
								if (tryCount < 3) {
									// disconnect belt and connect retry (3 times)
									disconnectBelt(false);

									tryCount++;
									connectStart();
								} else {
									disconnectBelt(true);
								}
								return;
							}

							if (isTest) {
								try {
									File logDir = new File(Environment.getExternalStorageDirectory() +"/" + Environment.DIRECTORY_DOCUMENTS+"/lumidiet");
									Log.d(TAG, logDir.getAbsolutePath());
									if (!logDir.exists()) {
										logDir.mkdirs();
									}
									File logFile = new File(logDir.getAbsolutePath(), "lumidiet_connect_log.txt");
									if (!logFile.exists()) {
										logFile.createNewFile();
									}
									StringBuilder logStr = new StringBuilder();
									logStr.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
									logStr.append(" lumidiet belt connect try count: ");
									logStr.append(tryCount + 1);
									logStr.append("\n");

									FileOutputStream fOut = new FileOutputStream(logFile, true);
									fOut.write(logStr.toString().getBytes());
									fOut.flush();
									fOut.close();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

                            mBluetoothLeService.setCharacteristicNotification(mBluetoothLeService.getLumiCharacteristic(), true);
                            Thread.sleep(1000);

                            if (mBluetoothLeService != null) {
                                getStatusFromBelt();
                                //getHistoryFromBelt();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                sendPairingData();
                break;
            }
            case DATA_AVAILABLE:
            {
                if (BuildConfig.DEBUG) Log.d(TAG, "belt data available");
                beltResponse = event.object;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (beltResponse != null) {
                            beltResponseHandling(beltResponse);
                        }
                    }
                }).start();
                break;
            }
            case GATT_CAN_READ:
            {
                BluetoothGattCharacteristic characteristic = mBluetoothLeService.getLumiCharacteristic();
                mBluetoothLeService.readCharacteristic(characteristic);

                break;
            }
			case BATTERY:
			{
				if (BuildConfig.DEBUG) Log.d(TAG, "receive battery data");
				beltResponse = event.object;
				new Thread(new Runnable() {
					@Override
					public void run() {
						if (beltResponse != null) {
							beltBatteryReceiveHandling(beltResponse);
						}
					}
				}).start();
				break;
			}
        }
    }

    int tryCount = 0;

    void sendPairingData() {
        try {
            JSONObject json = new JSONObject();
            StringBuilder display = new StringBuilder();
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getRealSize(size);
            display.append(size.x);
            display.append("x");
            display.append(size.y);
            json.put("masterkey", getUserData().getMasterKey());
            json.put("s_phonename", Build.MODEL);
            json.put("s_phoneos", "Android"+Build.VERSION.RELEASE);
            json.put("s_display", display);
            //StringBuilder deviceid = new StringBuilder(deviceNamePrefix);
            StringBuilder deviceid = new StringBuilder(mDeviceName.substring(0, mDeviceName.length() - 4));
            deviceid.append(mDeviceAddress.replaceAll(":", ""));
            json.put("lumi_device", deviceid);

            new JSONNetworkManager(JSONNetworkManager.LUMIDIET, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    disconnectBelt(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener).setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    try {
                        int result = responseJson.getInt("result");

                        if (result == 0) {
                            if (BuildConfig.DEBUG) Log.d(TAG, "sendPairingData failed");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void getFirmwareFromBelt() {
        if (mBluetoothLeService == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "getFirmwareFromBelt, mBluetoothLeService is null");
            return;
        }
        mBluetoothLeService.sendGetFirmwareVersion();
    }

    void getHistoryFromBelt() {
		if (BuildConfig.DEBUG) Log.d(TAG, "getHistoryFromBelt()");
        if (mBluetoothLeService == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "getHistoryFromBelt, mBluetoothLeService is null");
            return;
        }

        //mBluetoothLeService.sendGetUseHistoryData(Integer.parseInt(getUserData().getMasterKey()));
		mBluetoothLeService.sendGetAtomicUseHistoryData(Integer.parseInt(getUserData().getMasterKey()));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popup_back_Layer.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isTimer) {
                        isTimer = true;
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popup_back_Layer.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.VISIBLE);
                            }
                        });*/
                    }

                    timerTime += 0.1f;

                    if (timerTime >= 2.0f) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "timerTime over 2.0f");
                        isStartHistory = false;
                        timerTime = 0.0f;
                        isTimer = false;

                        if (!mConnected) {
                            if (mTimer != null) {
                                mTimer.cancel();
                                mTimer.purge();
                                mTimer = null;
                            }
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.INVISIBLE);

                                if (mBeltHistories.size() <= 0) {
                                    popup_back_Layer.setVisibility(View.INVISIBLE);
                                    //getUsingDate();
                                    if (!mAnimationHasEnded)
                                        getUsingDate();
                                    if (!isClearAll) {
                                        clearAllHistoryFromBelt();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                getFirmwareFromBelt();
                                            }
                                        }, 1000);
                                    }
                                    return;
                                }
								showBeltHistoryPopup();
                            }
                        });

                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer.purge();
                            mTimer = null;
                        }
                    }
                }
            }, 1000, 100);
        }
    }

    private long mLastClickTime;
    private long MIN_CLICK_INTERVAL = 500;
	private boolean isDelete = true;

    void showBeltHistoryPopup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "showBeltHistoryPopup()");
		if (popup != null) {
			if (BuildConfig.DEBUG) Log.e(TAG, "popup is not null");
            if (popupState == HISTORY) {
                if (BuildConfig.DEBUG) Log.e(TAG, "history popup");
                return;
            } else {
                if (BuildConfig.DEBUG) Log.e(TAG, "not history popup, popupState:" + popupState);
                return;
            }
		}

		popupState = HISTORY;
		// popup
		LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type5, null);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		//popup.setFocusable(true);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
		//RelativeLayout applyBtn = (RelativeLayout) layout.findViewById(R.id.history_btn_apply);
		RelativeLayout applyBtn = (RelativeLayout) layout.findViewById(R.id.popup_btn_ok);
		RelativeLayout cancelBtn = (RelativeLayout) layout.findViewById(R.id.popup_btn_cancel);
		hideUI();

		final View delCheckView = layout.findViewById(R.id.history_del_chk);
		delCheckView.setActivated(isDelete);
		delCheckView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				long currentClickTime = SystemClock.uptimeMillis();
				long elapsedTime = currentClickTime - mLastClickTime;
				mLastClickTime = currentClickTime;

				// 중복 클릭인 경우
				if(elapsedTime <= MIN_CLICK_INTERVAL) {
					if (BuildConfig.DEBUG) Log.d(TAG, "중복 클릭: "+elapsedTime);
					return;
				}
				isDelete = !isDelete;
				delCheckView.setActivated(isDelete);
			}
		});

		listView = (LumiListView) layout.findViewById(R.id.history_listview);
		listAdapter = new HistoryListAdapter();
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (BuildConfig.DEBUG) Log.d(TAG, "click");
                long currentClickTime = SystemClock.uptimeMillis();
                long elapsedTime = currentClickTime - mLastClickTime;
                mLastClickTime = currentClickTime;

                // 중복 클릭인 경우
                if(elapsedTime <= MIN_CLICK_INTERVAL) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "중복 클릭: "+elapsedTime);
                    return;
                }
                listAdapter.setSelectedHolder(position);
			}
		});

		listAdapter.addItems(mBeltHistories);
		listAdapter.notifyDataSetChanged();

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dispatchTouchEvent(event);
				return false;
			}
		});

		applyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                long currentClickTime = SystemClock.uptimeMillis();
                long elapsedTime = currentClickTime - mLastClickTime;
                mLastClickTime = currentClickTime;

                // 중복 클릭인 경우
                if(elapsedTime <= MIN_CLICK_INTERVAL) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "중복 클릭: "+elapsedTime);
                    return;
                }

				//isStartHistory = false;
				popup.dismiss();
				popup_back_Layer.setVisibility(View.INVISIBLE);
				popup = null;
				popupState = NONE;
				// list view 에서 데이터 갖고 오기
				mBeltHistories = listAdapter.getSelectedHistories();
				if (isDelete) {
					deleteHistories.addAll(listAdapter.getUnselectedHistories());
				}
				if (mBeltHistories.size() > 0) {
					// server 전송 및 내부 db 저장
					requestBeltHistory();
				} else {
                    clearHistoryFromBelt();
                }
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						getFirmwareFromBelt();
					}
				}, 1000);
			}
		});

		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup_back_Layer.setVisibility(View.INVISIBLE);
				popup = null;
				popupState = NONE;

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						getFirmwareFromBelt();
					}
				}, 1000);
			}
		});
	}

    void insertBeltHistoryDB() {
        if (mDatabaseManager.insertBeltHistory(mBeltHistories)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "insert success");
            //mBeltHistories.clear();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainLDIUsageStepsRecordFragment) mainLDIUsageStepsRecordFragment).setFragment();
                    //if (!mAnimationHasEnded)
                        //getUsingDate();
                        //usingCalculator();
                }
            });
        }
        else {
            if (BuildConfig.DEBUG) Log.d(TAG, "insert failed");
            //insertBeltHistoryDB();
        }
    }

    public void requestBeltHistory() {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", "in");
            json.put("userid", getUserData().getMasterKey());
            json.put("kind", getUserData().getCountry());
            json.put("lumi_device", mBeltHistories.get(0).getDeviceId());
            JSONArray array = new JSONArray();
            for (BeltHistory history : mBeltHistories) {
                JSONObject data = new JSONObject();

                data.put("type", history.getType());
                data.put("ledmode", history.getMode());
                data.put("ldi", history.getLdiValue());
                data.put("ldi_row", history.getLDIRawData());
                data.put("use_s_time", history.getStartTime());
                data.put("use_e_time", history.getEndTime());

                array.put(data);
            }
            json.put("ldi_data", array);

            new JSONNetworkManager(JSONNetworkManager.LDI, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    disconnectBelt(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener).setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    responseBeltHistory(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void responseBeltHistory(JSONObject json) {
        try {
            int result = json.getInt("result");
            if (result == 0) {
                if (BuildConfig.DEBUG) Log.d(TAG, "failed, retry");
                requestBeltHistory();
            } else {
                if (BuildConfig.DEBUG) Log.d(TAG, "success");

                long beltHistoryVersion = json.getLong("version");

                getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putLong(Preferences_BELT_HISTORY_VERSION, beltHistoryVersion).commit();

                insertBeltHistoryDB();
                clearHistoryFromBelt();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    boolean isClearAll = false;
    void clearAllHistoryFromBelt() {
        // clear all
        isClearAll = true;
        if (BuildConfig.DEBUG) Log.d(TAG, "clearAllHistoryFromBelt()");
        mBluetoothLeService.sendClearUseHistory();
    }

    ArrayList<BeltHistory> sendClearHistories = new ArrayList<>();
    ArrayList<BeltHistory> deleteHistories = new ArrayList<>();
    // 데이터 선택 삭제
    void clearHistoryFromBelt() {
        if (!mConnected) {
            if (BuildConfig.DEBUG) Log.d(TAG, "belt disconnected");
            return;
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "clearHistoryFromBelt()");

        if (deleteHistories.size() > 0) {
            if (BuildConfig.DEBUG) Log.d(TAG, "deleteHistories.size(): "+deleteHistories.size());
            if (BuildConfig.DEBUG) Log.d(TAG, "mBeltHistories.size(): "+mBeltHistories.size());
            mBeltHistories.addAll(deleteHistories);
            deleteHistories.clear();
            if (BuildConfig.DEBUG) Log.d(TAG, "mBeltHistories addAll deleteHistories: "+mBeltHistories.size());
        }

        if (mBeltHistories.size() <= 0) {
            if (BuildConfig.DEBUG) Log.d(TAG, "보낼 데이터 없음");
            getUsingDate();
            return;
        }
        if (mBeltHistories.size() <= 4) {
            if (BuildConfig.DEBUG) Log.d(TAG, "in1 "+mBeltHistories.size());
            long startTime[] = new long[mBeltHistories.size()];
            for (int i = 0; i < mBeltHistories.size(); i++) {
                startTime[i] = mBeltHistories.get(i).getStartTime();
            }
            mBeltHistories.clear();
            mBluetoothLeService.sendClearUseHistory(startTime);
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "in2 "+mBeltHistories.size());
            int size = 4;// - sendClearHistories.size();
            for (int i = 0; i < size; i++) {
                sendClearHistories.add(mBeltHistories.get(0));
                mBeltHistories.remove(0);
            }

            long startTime[] = new long[sendClearHistories.size()];
            for (int i = 0; i < sendClearHistories.size(); i++) {
                startTime[i] = sendClearHistories.get(i).getStartTime();
            }
            mBluetoothLeService.sendClearUseHistory(startTime);
        }
    }

    void getStatusFromBelt() {
        if (mBluetoothLeService == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "getStatusFromBelt, mBluetoothLeService is null");
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "getStatusFromBelt()");
        // 여기서 시간
        mBluetoothLeService.sendGetDeviceStatus(Integer.parseInt(getUserData().getMasterKey()));
        isSendStatus = true;

        connectHandler1.postDelayed(connectRunnable1, BLE_TIMEOUT);
    }

    public void getMeasureLDIFromBelt() {
        if (mBluetoothLeService == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "getMeasureLDIFromBelt, mBluetoothLeService is null");
            return;
        }
        mBluetoothLeService.sendGetLDIValue();
    }

	void requestFirmwareVersion() {
		try {
			if (BuildConfig.DEBUG) Log.d(TAG, "requestFirmwareVersion, code name: " + getFirmwareData().getCodeName());
			JSONObject request = new JSONObject();
			//request.put("firmware_kind", getFirmwareData().getCodeName());
			//request.put("firmware_kind", "Lumi-B");
            request.put("firmware_kind", mDeviceName.substring(0, mDeviceName.lastIndexOf("-")));
			new JSONNetworkManager(JSONNetworkManager.FIRMWARE, request) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    disconnectBelt(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener).setCancelable(false)
                                    .show();
                        }
                    });
                }

				@Override
				public void responseCallback(JSONObject responseJson) {
					try {
						if (responseJson.getInt("result") == 1) {
							if (BuildConfig.DEBUG) Log.d(TAG, "requestFirmwareVersion, success");
							getFirmwareData().setVersion(responseJson.getString("firmware_ver"));
							getFirmwareData().setUrl(responseJson.getString("firmwarefile"));
							checkFirmware();
						} else {
							if (BuildConfig.DEBUG) Log.d(TAG, "requestFirmwareVersion, failed");
							requestFirmwareVersion();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}.sendJson();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

    void checkFirmware() {
		if (!isFirmwareForceUpdate) {
			if (getFirmwareData().getVersion() == null || getFirmwareData().getVersion().equalsIgnoreCase("")) {
				showNotUsePopupCheck();
				return;
			}
			String[] versionCodeN = getFirmwareData().getRealVersion().split("\\.");
			String[] versionCodeL = getFirmwareData().getVersion().split("\\.");

			for (int i = 0; i < 3;) {
				if (Integer.parseInt(versionCodeN[i]) < Integer.parseInt(versionCodeL[i])) {
					// update
					if (BuildConfig.DEBUG) Log.d(TAG, "needed device firmware update");
					// firmware download & update
					new DownloadFileFromURL().execute(getFirmwareData().getUrl());
					break;
				} else {
					if (Integer.parseInt(versionCodeN[i]) > Integer.parseInt(versionCodeL[i]) || i == 2) {
						// latest version
						if (BuildConfig.DEBUG) Log.d(TAG, "device firmware is latest or test version");
						if (BuildConfig.DEBUG) Log.d(TAG, "now device firmware version: " + versionCodeN[0] + "." + versionCodeN[1] + "." + versionCodeN[2]);
						if (BuildConfig.DEBUG) Log.d(TAG, "latest device firmware version: " + versionCodeL[0] + "." + versionCodeL[1] + "." + versionCodeL[2]);
						// 최종인 경우 일주일 확인 팝업
						showNotUsePopupCheck();
						break;
					}
					i++;
				}
			}
		} else {
			if (BuildConfig.DEBUG) Log.d(TAG, "firmware force update");
			new DownloadFileFromURL().execute(getFirmwareData().getUrl());
		}
	}

	void showNotUsePopupCheck() {
		if (mDatabaseManager.selectLDIHistory(getUserData().getMasterKey()).size() > 0) {
			// LDI 측정이 하나라도 데이터가 있는 경우
			Calendar c = Calendar.getInstance();
			if (mDatabaseManager.selectLDIHistory(getUserData().getMasterKey(), (c.getTimeInMillis() / 1000) - (7 * 24 * 60 * 60), c.getTimeInMillis() / 1000).size() <= 0) {
				// 일주일 내에 사용한 데이터가 없다면
				if (isShowNotUsePopup) {
					// 해당 팝업이 단 한번도 나오지 않은 경우에만
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showNotUsePopup();
							isShowNotUsePopup = false;
						}
					});
				} else {
					if (popup_back_Layer.getVisibility() == View.VISIBLE)
						popup_back_Layer.setVisibility(View.INVISIBLE);
				}
			} else {
				if (popup_back_Layer.getVisibility() == View.VISIBLE)
					popup_back_Layer.setVisibility(View.INVISIBLE);
			}
		} else {
			if (popup_back_Layer.getVisibility() == View.VISIBLE)
				popup_back_Layer.setVisibility(View.INVISIBLE);
		}
	}

	void showCompletePopup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "showCompletePopup()");

		popupState = COMPLETE;
		isCompletePopup = true;
		LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_2l, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_2l_btn_ok);

		String body = getString(R.string.belt_use_complete_msg2);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_2l_body);
		tv.setText(body);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dispatchTouchEvent(event);
				return false;
			}
		});

		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isCompletePopup = false;
				popup.dismiss();
				popup_back_Layer.setVisibility(View.INVISIBLE);
				popup = null;
				popupState = NONE;
			}
		});
		hideUI();
	}

	@Deprecated
	void showBatteryLowPopup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "showBatteryLowPopup()");

		popupState = BATTERYLOW;
		isCompletePopup = true;
		LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_2l, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dispatchTouchEvent(event);
				return false;
			}
		});

		((TextView) layout.findViewById(R.id.popup_type2_2l_body)).setText(getString(R.string.alert_low_battery));
		layout.findViewById(R.id.popup_type2_2l_btn_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isCompletePopup = false;
				popup.dismiss();
				popup_back_Layer.setVisibility(View.INVISIBLE);
				popup = null;
				popupState = NONE;
			}
		});
		hideUI();
		connect_Btn.setEnabled(true);
	}

	void beltBatteryReceiveHandling(Object eventObject) {
		//String data = (String) eventObject;
		if (BuildConfig.DEBUG) Log.d(TAG, "battery receive data: " + eventObject);

		byte [] battery = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray((String) eventObject));
		Short batteryLevel = Short.parseShort(HexEditor.byteArrayToHex(battery), 16);

		if (mBeltStatus.getBattery() == batteryLevel) {
			if (BuildConfig.DEBUG) Log.d(TAG, "same battery level");
		} else {
			if (BuildConfig.DEBUG) Log.d(TAG, "changed battery level");
			mBeltStatus.setBattery(batteryLevel);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateBatteryView();
				}
			});
		}
	}

	int lastHistoryIdx = 0;
	boolean isUserTerminated = false;

    void beltResponseHandling(Object eventObject) {
        String data = (String) eventObject;
        if (BuildConfig.DEBUG) Log.d(TAG, "response data: " + data);

        String type = data.substring(0, 2);

        if (type.equals("01")) {
            // get firmware version
            StringBuilder version = new StringBuilder();

            for (int i = 2; i < data.length(); i+=2) {
                version.append(Short.parseShort(HexEditor.byteArrayToHex(BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(i, i + 2))))));
                if (i != data.length() - 2)
                    version.append(".");
            }

            getFirmwareData().setRealVersion(version.toString());

			// get latest firmware version from server
			requestFirmwareVersion();
        } else if (type.equals("31")) {
            // get usage history
            timerTime = 0.0f;

            BeltHistory history = new BeltHistory();

            String historyEndFlag = data.substring(2, 10);
            if (BuildConfig.DEBUG) Log.d(TAG, "history end?(F1F2F3F4 is end) : " + historyEndFlag);
            if (historyEndFlag.equals("F1F2F3F4")) {
                if (BuildConfig.DEBUG) Log.d(TAG, "history end");

                if (isUserTerminated) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "user terminated: "+isUserTerminated);

                    //mBeltHistories.add(history);
                    //requestBeltHistory();

                    return;
                }

                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                    mTimer = null;
                }

                // 마지막 데이터
                if (popup == null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "popup is null");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
							isStartHistory = false;
                            mProgressBar.setVisibility(View.INVISIBLE);

                            if (mBeltHistories.size() <= 0) {
                                if (BuildConfig.DEBUG) Log.d(TAG, "mBeltHistories size 0");

                                popup_back_Layer.setVisibility(View.INVISIBLE);

                                if (!mAnimationHasEnded)
                                    getUsingDate();

                                if (!isClearAll) {
                                    clearAllHistoryFromBelt();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            getFirmwareFromBelt();
                                        }
                                    }, 1000);
                                }
                                return;
                            } else {
                                // sorting, start time 오름차순으로
                                Comparator<BeltHistory> cmp = new Comparator<BeltHistory>() {
                                    @Override
                                    public int compare(BeltHistory lhs, BeltHistory rhs) {
                                        if (BuildConfig.DEBUG) Log.d(TAG, "compare: " + (lhs.getStartTime() > rhs.getStartTime() ? 1 : -1));
                                        return lhs.getStartTime() > rhs.getStartTime() ? 1 : -1;
                                    }
                                };

                                Collections.sort(mBeltHistories, cmp);
                            }
							showBeltHistoryPopup();
                        }
                    });
                }
                return;
            }

            if (data.length() <= 8) {
                if (BuildConfig.DEBUG) Log.e(TAG, "data error: "+data.length());
                return;
            }

            // belt response data without type(first 2byte)
            //byte [] ldi_low = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(2, data.length())));
            history.setLDIRawData(data.substring(2, data.length()));
            // user id - master key 인데 전부 현재 사용자로 전환, 벨트에서 넘어오는 정보는 다른 사용자일 수 있음
            //byte [] user = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(2, 10)));
            //history.setUserId(Long.toString(Long.parseLong(HexEditor.byteArrayToHex(user), 16)));
            history.setUserId(getUserData().getMasterKey());
            // LDI type (hex)    ((auto: 01, manual: 02,  - now unused) user terminated&auto: 81, user terminated&manual: 82)
			// LDI type (dec)    ((auto: 01, manual: 02,  - now unused) user terminated&auto: 129, user terminated&manual: 130)
            byte [] ldi_type = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(10, 12)));
			try {	// NumberFormatException 발생시 10진수로 변환 (구버전 펌웨어의 경우 데이터 없음을 알리는 메시지로 FF가 넘어옴), 어차피 FF 10진수로 바꾼거 안씀
				history.setType(Short.parseShort(data.substring(10, 12)));					// hexa
			} catch (NumberFormatException e) {
				history.setType(Short.parseShort(HexEditor.byteArrayToHex(ldi_type), 16));	// decimal
			}
            // LDI mode     (led on/motor off: 00, led on/motor on and off: 01, led on/motor always: 02)
            byte [] ldi_mode = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(12, 14)));
            history.setMode(Short.parseShort(HexEditor.byteArrayToHex(ldi_mode), 16));
            // LDI Value
            byte [] ldi_value = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(14, 18)));
            history.setLdiValue(Integer.parseInt(HexEditor.byteArrayToHex(ldi_value), 16));
            // Start time
            byte [] sTime = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(18, 26)));
            history.setStartTime(Long.parseLong(HexEditor.byteArrayToHex(sTime), 16));
            // end time
            byte [] eTime = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(26, data.length())));
            history.setEndTime(Long.parseLong(HexEditor.byteArrayToHex(eTime), 16));
            //StringBuilder deviceId = new StringBuilder(deviceNamePrefix);
			StringBuilder deviceid = new StringBuilder(mDeviceName.substring(0, mDeviceName.length() - 4));
            //StringBuilder deviceId = new StringBuilder(mDeviceName.substring(0, mDeviceName.length() - 4));
            deviceid.append(mDeviceAddress.replaceAll(":", ""));
            history.setDeviceId(deviceid.toString());

            // 사용시간 2분 미만 컷
            if (history.getUsingTime() * 1000 < MIN_USING_TIME) {
                if (BuildConfig.DEBUG) Log.d(TAG, "2분 미만 데이터");
                deleteHistories.add(history);
                return;
            }

            // 2016년 10월 이전 데이터 컷
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(history.getStartTime() * 1000);

            if (c.get(Calendar.YEAR) <= 2016 && c.get(Calendar.MONTH) < 9) {
                if (BuildConfig.DEBUG) Log.d(TAG, "2016년 10월 이전 데이터");
                deleteHistories.add(history);
                return;
            }

            Calendar cn = Calendar.getInstance();
            cn.getTimeInMillis();

            if ((cn.getTimeInMillis() / 1000) - history.getStartTime() >= (30 * 24 * 60 * 60)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "30일 이상 지난 데이터");
                deleteHistories.add(history);
                return;
            }

            // 동일한 시작시간 있는거 컷
            if (mDatabaseManager.selectCountBeltHistory(Integer.parseInt(history.getUserId()), history.getStartTime()) > 0) {
                if (BuildConfig.DEBUG) Log.d(TAG, "동일한 시작시간 존재");
                deleteHistories.add(history);
                return;
            }

            // 이전과 동일한 데이터 들어온 경우 컷 (연속으로 데이터가 들어오는 상황이 존재)
            for (BeltHistory history1 : mBeltHistories) {
                if (history1.getStartTime() == history.getStartTime()) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "이미 동일한 데이터 수신");
                    return;
                }
            }

			//if (BuildConfig.DEBUG) Log.d(TAG, "history type: " + history.getType());
            // 사용자 강제 종료 상황
			if (!isStartHistory && history.getType() == 82) {
				if (BuildConfig.DEBUG) Log.d(TAG, "user terminated manual");
                isUserTerminated = true;
				mBeltHistories.add(history);
				requestBeltHistory();

				return;
			}

            long endTime = MAX_USING_TIME - 10000;

            if (is3min) {
                endTime = MAX_USING_TIME - 3000;
            }

            if (mBeltStatus.getUsingTime() * 1000 > endTime) {
                // 정확히 0초에 들어오는 것이 아니므로 10초 미만으로 남았다면 팝업을 띄워줌
                // + 10초 단위로 시간 업데이트
                if (BuildConfig.DEBUG) Log.d(TAG, "사용 완료");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
						showCompletePopup();
                    }
                });

                mBeltHistories.add(history);
                requestBeltHistory();

                return;
            }

            mBeltHistories.add(history);
        } else if (type.equals("33")) {
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// check
			// get usage history, atomic communication
			timerTime = 0.0f;

			BeltHistory history = new BeltHistory();

			String historyEndFlag = data.substring(2, 10);
			if (BuildConfig.DEBUG) Log.d(TAG, "history end?(F1F2F3F4 is end) : " + historyEndFlag);
			if (historyEndFlag.equals("F1F2F3F4")) {
				if (BuildConfig.DEBUG) Log.d(TAG, "history end");

				if (isUserTerminated) {
					if (BuildConfig.DEBUG) Log.d(TAG, "user terminated: "+isUserTerminated);
					return;
				}

				if (mTimer != null) {
					mTimer.cancel();
					mTimer.purge();
					mTimer = null;
				}

				// 마지막 데이터
				if (popup == null) {
					if (BuildConfig.DEBUG) Log.d(TAG, "Belt use memory: "+mBeltStatus.getUseMemory()+", history last index: "+lastHistoryIdx);

					if (mBeltStatus.getUseMemory() != lastHistoryIdx) {
						// restart
						mBeltHistories.clear();
						deleteHistories.clear();
						getStatusFromBelt();
						return;
					}

					if (BuildConfig.DEBUG) Log.d(TAG, "popup is null");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							isStartHistory = false;
							mProgressBar.setVisibility(View.INVISIBLE);

							if (mBeltHistories.size() <= 0) {
								if (BuildConfig.DEBUG) Log.d(TAG, "mBeltHistories size 0");

								popup_back_Layer.setVisibility(View.INVISIBLE);

								if (!mAnimationHasEnded)
									getUsingDate();

								if (!isClearAll) {
									clearAllHistoryFromBelt();

									new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											getFirmwareFromBelt();
										}
									}, 1000);
								}
								return;
							} else {
								// sorting, start time 오름차순으로
								Comparator<BeltHistory> cmp = new Comparator<BeltHistory>() {
									@Override
									public int compare(BeltHistory lhs, BeltHistory rhs) {
										if (BuildConfig.DEBUG) Log.d(TAG, "compare: " + (lhs.getStartTime() > rhs.getStartTime() ? 1 : -1));
										return lhs.getStartTime() > rhs.getStartTime() ? 1 : -1;
									}
								};

								Collections.sort(mBeltHistories, cmp);
							}
							showBeltHistoryPopup();
						}
					});
				}
				return;
			}

			if (data.length() <= 8) {
				if (BuildConfig.DEBUG) Log.e(TAG, "data error: "+data.length());
				return;
			}

			// belt response data without type(first 2byte)
			//byte [] ldi_low = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(2, data.length())));
			history.setLDIRawData(data.substring(2, data.length()));
			// user id - master key 인데 전부 현재 사용자로 전환, 벨트에서 넘어오는 정보는 다른 사용자일 수 있음
			//byte [] user = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(2, 10)));
			//history.setUserId(Long.toString(Long.parseLong(HexEditor.byteArrayToHex(user), 16)));
			history.setUserId(getUserData().getMasterKey());
			// LDI type (hex)    ((auto: 01, manual: 02,  - now unused) user terminated&auto: 81, user terminated&manual: 82)
			// LDI type (dec)    ((auto: 01, manual: 02,  - now unused) user terminated&auto: 129, user terminated&manual: 130)
			byte [] ldi_type = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(10, 12)));
			try {	// NumberFormatException 발생시 10진수로 변환 (구버전 펌웨어의 경우 데이터 없음을 알리는 메시지로 FF가 넘어옴), 어차피 FF 10진수로 바꾼거 안씀
				history.setType(Short.parseShort(data.substring(10, 12)));					// hexa
			} catch (NumberFormatException e) {
				history.setType(Short.parseShort(HexEditor.byteArrayToHex(ldi_type), 16));	// decimal
			}
			// LDI mode     (led on/motor off: 00, led on/motor on and off: 01, led on/motor always: 02)
			byte [] ldi_mode = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(12, 14)));
			history.setMode(Short.parseShort(HexEditor.byteArrayToHex(ldi_mode), 16));
			// LDI Value
			byte [] ldi_value = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(14, 18)));
			history.setLdiValue(Integer.parseInt(HexEditor.byteArrayToHex(ldi_value), 16));
			// Start time
			byte [] sTime = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(18, 26)));
			history.setStartTime(Long.parseLong(HexEditor.byteArrayToHex(sTime), 16));
			// end time
			byte [] eTime = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(26, 34)));
			history.setEndTime(Long.parseLong(HexEditor.byteArrayToHex(eTime), 16));
			// current index
			byte [] cIdx = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(34, data.length())));
			if (Integer.parseInt(HexEditor.byteArrayToHex(cIdx), 16) > lastHistoryIdx)
				lastHistoryIdx = Integer.parseInt(HexEditor.byteArrayToHex(cIdx), 16);

			StringBuilder deviceid = new StringBuilder(mDeviceName.substring(0, mDeviceName.length() - 4));
			deviceid.append(mDeviceAddress.replaceAll(":", ""));
			history.setDeviceId(deviceid.toString());

			// 사용시간 2분 미만 컷
			if (history.getUsingTime() * 1000 < MIN_USING_TIME) {
				if (BuildConfig.DEBUG) Log.d(TAG, "2분 미만 데이터");
				deleteHistories.add(history);
				mBluetoothLeService.sendGetAtomicUseHistoryEcho(data);		// send echo
				return;
			}

			// 2016년 10월 이전 데이터 컷
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(history.getStartTime() * 1000);

			if (c.get(Calendar.YEAR) <= 2016 && c.get(Calendar.MONTH) < 9) {
				if (BuildConfig.DEBUG) Log.d(TAG, "2016년 10월 이전 데이터");
				deleteHistories.add(history);
				mBluetoothLeService.sendGetAtomicUseHistoryEcho(data);		// send echo
				return;
			}

			Calendar cn = Calendar.getInstance();
			cn.getTimeInMillis();

			if ((cn.getTimeInMillis() / 1000) - history.getStartTime() >= (30 * 24 * 60 * 60)) {
				if (BuildConfig.DEBUG) Log.d(TAG, "30일 이상 지난 데이터");
				deleteHistories.add(history);
				mBluetoothLeService.sendGetAtomicUseHistoryEcho(data);		// send echo
				return;
			}

			// 동일한 시작시간 있는거 컷
			if (mDatabaseManager.selectCountBeltHistory(Integer.parseInt(history.getUserId()), history.getStartTime()) > 0) {
				if (BuildConfig.DEBUG) Log.d(TAG, "동일한 시작시간 존재");
				deleteHistories.add(history);
				mBluetoothLeService.sendGetAtomicUseHistoryEcho(data);		// send echo
				return;
			}

			// 이전과 동일한 데이터 들어온 경우 컷 (연속으로 데이터가 들어오는 상황이 존재)
			for (BeltHistory history1 : mBeltHistories) {
				if (history1.getStartTime() == history.getStartTime()) {
					if (BuildConfig.DEBUG) Log.d(TAG, "이미 동일한 데이터 수신");
					return;
				}
			}

			mBeltHistories.add(history);
			mBluetoothLeService.sendGetAtomicUseHistoryEcho(data);		// send echo
		} else if (type.equals("41")) {
            // erase all usage history
            String result = data.substring(2, 4);
            if (result.equals("01")) {
                // success
                mBluetoothLeService.sendGetUseHistoryData(Integer.parseInt(getUserData().getMasterKey()));
            } else {

                if (data.length() >= 8) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "data error: "+data.length());
                    return;
                }

                // fail
                clearAllHistoryFromBelt();
            }
        } else if (type.equals("42")) {
            // 선택 삭제
        } else if (type.equals("43")) {
            if (data.length() >= 8) {
                if (BuildConfig.DEBUG) Log.e(TAG, "data error");
                return;
            }
            // 선택 삭제 완료
            if (BuildConfig.DEBUG) Log.d(TAG, "선택삭제 완료");
            sendClearHistories.clear();
            clearHistoryFromBelt();
        } else if (type.equals("51")) {

            if (data.length() <= 8) {
                if (BuildConfig.DEBUG) Log.e(TAG, "data error: "+data.length());
                return;
            }

            // device status request
            boolean isFirst = false;
            // initialize variable
            if (mBeltStatus == null) {
                mBeltStatus = new BeltStatus();
                isFirst = true;
            }
            else {
                mBeltStatus.setUserId("");
                mBeltStatus.setUsingStatus((short)0);
                mBeltStatus.setUsingTime(0);
                mBeltStatus.setUseMemory(0);
                mBeltStatus.setTotalMemory(0);
                mBeltStatus.setMode((short)0);
                mBeltStatus.setBattery((short)0);
            }

            // user id - 위와 마찬가지로 현재 로그인한 유저의 master key 값으로 set
            //byte [] user = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(2, 10)));
            //mBeltStatus.setUserId(Long.toString(Long.parseLong(HexEditor.byteArrayToHex(user), 16)));
            mBeltStatus.setUserId(getUserData().getMasterKey());
            // current rtc (time stamp)
            byte [] curRTC = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(10, 18)));

            // using status (on: 01, off: 02)
            byte [] using_status = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(18, 20)));
            mBeltStatus.setUsingStatus(Short.parseShort(HexEditor.byteArrayToHex(using_status), 16));
            // using time (0~1200s)
            byte [] using_time = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(20, 24)));
            mBeltStatus.setUsingTime(Long.parseLong(HexEditor.byteArrayToHex(using_time), 16));
            // mode (led on/motor off: 00, led on/motor on and off: 01, led on/motor always: 02)
            byte [] mode = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(24, 26)));
            mBeltStatus.setMode(Short.parseShort(HexEditor.byteArrayToHex(mode), 16));
            // battery (0~100)
            byte [] battery = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(26, 28)));
            mBeltStatus.setBattery(Short.parseShort(HexEditor.byteArrayToHex(battery), 16));
            // memory status : usage
            byte [] memory_usage = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(28, 32)));
            mBeltStatus.setUseMemory(Integer.parseInt(HexEditor.byteArrayToHex(memory_usage), 16));
            // memory status : total
            byte [] memory_total = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(32, data.length())));
            mBeltStatus.setTotalMemory(Integer.parseInt(HexEditor.byteArrayToHex(memory_total), 16));

            // battery 20% 미만일 때에는? 시기는 벨트와 연결된 직후 - 내용 삭제 06.22
            /*if (isFirst && ((getFirmwareData().getCodeName().equals("S") && mBeltStatus.getBattery() < 20) || (!getFirmwareData().getCodeName().equals("S") && mBeltStatus.getBattery() <= 10))) {
                // 강제 연결 종료
                disconnectBelt();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
						showBatteryLowPopup();
                    }
                });
                return;
            }*/

            // battery timer (interval : one minute)
			if (mBatteryTimer == null) {
				mBatteryTimer = new Timer();
				mBatteryTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (BuildConfig.DEBUG) Log.d(TAG, "battery timer");
						mBluetoothLeService.sendGetPowerLevel();
					}
				}, 60000, 60000);
			}

            if (mStatusTimer == null) {
                mStatusTimer = new Timer();
                mStatusTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (BuildConfig.DEBUG) Log.d(TAG, "status timer");
                        // milli second 단위로 변경
                        long prevTime = mBeltStatus.getUsingTime() * 1000;
                        long nowTime = prevTime + 10000;
                        mBeltStatus.setUsingTime(nowTime / 1000);

                        if ((Math.floor((MAX_USING_TIME - nowTime) / 60000) + 1) != (Math.floor((MAX_USING_TIME - prevTime) / 60000) + 1))
                        {
                            if (BuildConfig.DEBUG) Log.d(TAG, "time changed");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append((int)(Math.floor((MAX_USING_TIME - (mBeltStatus.getUsingTime() * 1000)) / 60000) + 1));
                                    //stringBuilder.append(getString(R.string.minute_ko));
                                    ((TextView) mainUsageOnView.findViewById(R.id.main_txt_remain_time)).setText(stringBuilder);
                                }
                            });
                        }

                        if (nowTime >= MAX_USING_TIME) {
                            if (mStatusTimer != null) {
                                mStatusTimer.cancel();
                                mStatusTimer.purge();
                                mStatusTimer = null;
                            }
                        }
                    }
                }, 0, 10000);
            }

            // set belt usage layout
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setBeltInfoLayer();
                    setMeasureLDIButtonEnabled(mConnected);
                }
            });

            // history 를 갖고 오는 것은 벨트 연결 후 맨 처음에만
            if (BuildConfig.DEBUG) Log.d(TAG, "isFirst: " + isFirst);
            if (isFirst)
                getHistoryFromBelt();

        } else if (type.equals("61")) {
            // measure LDI value
            // request minimum between 1s
            if (mLDIValue == null)
                mLDIValue = new LDIValue();
            else
                mLDIValue.setLdiValue((short)0);

            byte [] ldi_value = BigEndianByteHandler.byteTobyte(HexEditor.hexToByteArray(data.substring(2, data.length())));
            mLDIValue.setLdiValue(Short.parseShort(HexEditor.byteArrayToHex(ldi_value), 16));
            mLDIValue.setMeasureTime(Calendar.getInstance().getTimeInMillis() / 1000);
            //StringBuilder deviceid = new StringBuilder(deviceNamePrefix);
			StringBuilder deviceid = new StringBuilder(mDeviceName.substring(0, mDeviceName.length() - 4));
            deviceid.append(mDeviceAddress.replaceAll(":", ""));
            mLDIValue.setDeviceId(deviceid.toString());
            mLDIValue.setUserId(getUserData().getMasterKey());
            mLDIValue.setMode(mBeltStatus.getMode());
            mLDIValue.setType((short)2);
            mLDIValue.setRawLDIValue(data.substring(2, data.length()));

            if (nowContentFragment != null)
                ((MeasureLDIFragment) nowContentFragment).checkLDIValue(mLDIValue);
        } else {
            // invalid data
        }
    }

    public void disconnectBelt(boolean isRemove) {
        isClearAll = false;
        connectHandler1.removeCallbacks(connectRunnable1);
        connectHandler2.removeCallbacks(connectRunnable2);
        HHFU = true;
        if (mConnected) {
            mConnected = false;
            mBluetoothLeService.disconnect();
            unbindService(mServiceConnection);
            mServiceConnection.onServiceDisconnected(null);
            mDeviceAddress = null;
            mDeviceName = null;
        }
        mBeltStatus = null;
        getFirmwareData().setRealVersion("");
        if (mStatusTimer != null) {
            mStatusTimer.cancel();
            mStatusTimer.purge();
            mStatusTimer = null;
        }
		if (mBatteryTimer != null) {
			mBatteryTimer.cancel();
			mBatteryTimer.purge();
			mBatteryTimer = null;
		}

        if (isRemove) {
			if (connectActivity != null) {
				connectActivity.finish();
				connectActivity = null;
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!isCompletePopup && !isFirmwarePopup) {
						if (popup != null) {
							if (BuildConfig.DEBUG) Log.d(TAG, "disconnectBelt() popup is not null");
							popup.dismiss();
							popup = null;
							popupState = NONE;
						}
						popup_back_Layer.setVisibility(View.INVISIBLE);
					}
					connect_Btn.setEnabled(true);
					setBeltInfoLayer();
					setMeasureLDIButtonEnabled(mConnected);
				}
			});
		}
    }

    void showNotUsePopup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "showNotUsePopup()");

		popupState = NOTUSE;
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type6, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type6_btn_ok);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dispatchTouchEvent(event);
				return false;
			}
		});

		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup_back_Layer.setVisibility(View.INVISIBLE);
				popup = null;
				popupState = NONE;
			}
		});
    }

    void showUpdateFirmwarePopup() {
		if (BuildConfig.DEBUG) Log.d(TAG, "showUpdateFirmwarePopup()");

		popupState = FIRMWARE;
        isFirmwarePopup = true;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_type4, null);
        popup_back_Layer.setVisibility(View.VISIBLE);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
        RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type4_btn_ok);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dispatchTouchEvent(event);
				return false;
			}
		});

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                popup = null;
				popupState = NONE;
                updateFirmware();
                //popup_back_Layer.setVisibility(View.INVISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.layout_popup_type7, null);
                        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
                        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

                        progress_TextView = (TextView) layout.findViewById(R.id.popup_type7_percent);
                        body_TextView = (TextView) layout.findViewById(R.id.popup_type7_body);

                        firmwareOK_Btn = (RelativeLayout) layout.findViewById(R.id.popup_type7_btn_ok);
                        firmwareOK_Btn.setEnabled(false);
                        firmwareOK_Btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popup.dismiss();
                                popup_back_Layer.setVisibility(View.INVISIBLE);
                                popup = null;
                                isFirmwarePopup = false;
                            }
                        });
                    }
                });
            }
        });
    }
    /* */
    void updateFirmware(){
        if (BuildConfig.DEBUG) Log.d(TAG, "updateFirmware()");
        final boolean keepBond = false;

        final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceAddress)
                .setDeviceName(mDeviceName)
                .setKeepBond(keepBond);
        starter.setZip(null, getFirmwareData().getFilePath());
        starter.start(this, DfuService.class);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = args.getParcelable("uri");
		/*
		 * Some apps, f.e. Google Drive allow to select file that is not on the device. There is no "_data" column handled by that provider. Let's try to obtain
		 * all columns and than check which columns are present.
		 */
        // final String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
        return new CursorLoader(this, uri, null /* all columns, instead of projection */, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "FINISHED");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void onTransferCompleted() {
        // firmware update completed
        progress_TextView.setVisibility(View.INVISIBLE);
        firmwareOK_Btn.setEnabled(true);
        body_TextView.setText(getString(R.string.firmware_update_success_msg));

        File file = new File(getFirmwareData().getFilePath());
        if (file.exists()) {
            file.delete();
        }
    }

    public void onUploadCanceled() {
        // firmware update canceled
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (popup != null) {
                    popup.dismiss();
                    popup_back_Layer.setVisibility(View.INVISIBLE);
                    popup = null;
                    isFirmwarePopup = false;
                }
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.firmware_update_err_msg), Toast.LENGTH_LONG);
                ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
                toast.show();
            }
        });
    }

    int order = 0;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onDeviceConnecting === " + deviceAddress);
            // first
            // forth
            if (BuildConfig.DEBUG) Log.i(TAG, "onDeviceConnecting === order: " + ++order);
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onDfuProcessStarting === " + deviceAddress);
            // second
            // fifth
            if (BuildConfig.DEBUG) Log.i(TAG, "onDfuProcessStarting === order: " + ++order);
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onEnablingDfuMode === " + deviceAddress);
            // third
            if (BuildConfig.DEBUG) Log.i(TAG, "onEnablingDfuMode === order: " + ++order);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onFirmwareValidating === " + deviceAddress);
            if (BuildConfig.DEBUG) Log.i(TAG, "onFirmwareValidating === order: " + ++order);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onDeviceDisconnecting === " + deviceAddress);
            // sixth
            if (BuildConfig.DEBUG) Log.i(TAG, "onDeviceDisconnecting === order: " + ++order);
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onDfuCompleted === " + deviceAddress);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTransferCompleted();

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onDfuAborted === " + deviceAddress);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onUploadCanceled();

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onProgressChanged === " + deviceAddress);
            if (BuildConfig.DEBUG) Log.i(TAG, "onProgressChanged === percent: " + percent);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress_TextView.setText(Integer.toString(percent) + "%");
                }
            });
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "onError === " + deviceAddress + "  : error === : " + message);
            if (BuildConfig.DEBUG) Log.d(TAG, "onError === " + deviceAddress + "  : error === : " + message);

            // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (popup != null) {
                        popup.dismiss();
                        popup_back_Layer.setVisibility(View.INVISIBLE);
                        popup = null;
                        isFirmwarePopup = false;
                    }
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.firmware_update_err_msg), Toast.LENGTH_LONG);
                    ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
                    toast.show();
                }
            });
        }
    };

    /**
     * 지난 날짜 계산기
     * @param present
     * @param past
     * @return  index 0: 0이면 중간에 공백인 날짜가 있단 의미, 0이 아닌 수는 무조건 공백이 없단 의미, index 1: 지난 날짜를 반환
     */
    public int[] getGapDays(Calendar present, Calendar past) {
        if (BuildConfig.DEBUG) Log.d(TAG, "getGapDays");

        int gap[] = {0, 1};

        for (int i = 1; ;i++) {
            long times[] = MyCalendarUtil.getDayInMillis(i);

            Calendar temp = Calendar.getInstance();
            temp.setTimeInMillis(times[0]);
            if (past.get(Calendar.DAY_OF_YEAR) == temp.get(Calendar.DAY_OF_YEAR)) {
                // 동일한 날짜가 되었으므로 break;, 즉 연속 사용중이라는 뜻
                gap[0]++;
                break;
            }

            ArrayList<BeltHistory> datas = mDatabaseManager.selectBeltHistory(Integer.parseInt(getUserData().getMasterKey()), times[0], times[1]);
            if (datas.size() > 0) {
                gap[1]++;
            } else {
                // 여기 들어왔따면 gap[0] == 0
                break;
            }
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "0이 아니면 연속 0이면 연속 아님: " + gap[0]);
        if (BuildConfig.DEBUG) Log.d(TAG, "연속 사용일 수: " + gap[1]);

        return gap;
    }

    public void getUsingDate() {
        if (BuildConfig.DEBUG) Log.d(TAG, "getUsingDate()");
        try {
            JSONObject json = new JSONObject();
            json.put("masterkey", getUserData().getMasterKey());
            //StringBuilder deviceid = new StringBuilder(deviceNamePrefix);
            StringBuilder deviceid = new StringBuilder(mDeviceName.substring(0, mDeviceName.length() - 4));
            deviceid.append(mDeviceAddress.replaceAll(":", ""));
            json.put("device", deviceid);

            new JSONNetworkManager(JSONNetworkManager.USE_DATE, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    disconnectBelt(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener).setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    try {
                        getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_CUR, responseJson.getInt("use_day")).commit();
                        getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_MAX, responseJson.getInt("use_day_max")).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    usingCalculator();
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

	// 드래그 모드인지 핀치줌 모드인지 돋보기 모드인지 구분
	static final int NONE	= 0;
	static final int DRAG	= 1;
	static final int ZOOM	= 2;
	static final int MAG		= 3;
	static final int ZOOM_IN	= 4;
	int mode = NONE;

	// 드래그시 좌표 저장
	int posX1=0, posX2=0, posY1=0, posY2=0;

	// 핀치시 두좌표간의 거리 저장
	float oldDist = 1f;
	float newDist = 1f;

	//PhotoViewAttacher photoViewAttacher;
	ImageView captureView;

	// set maximum scroll amount (based on center of image)
	int maxX, maxY;
	static String PREFERENCES_MAGNIFIER		= "MAGNIFIER";

	boolean magMode = false;

	public void setMagMode(boolean enabled) {
		getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putBoolean(PREFERENCES_MAGNIFIER, enabled).commit();
		magMode = enabled;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (!magMode) {
			return super.dispatchTouchEvent(event);
		}
		int act = event.getAction();
		if (mode != MAG) {
			switch (act & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN: //첫번째 손가락 터치(드래그 용도)
					posX1 = (int) event.getX();
					posY1 = (int) event.getY();

					Log.d("zoom", "mode=DRAG");
					mode = DRAG;
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) { // 드래그 중
						posX2 = (int) event.getX();
						posY2 = (int) event.getY();

						if (Math.abs(posX2 - posX1) > 20 || Math.abs(posY2 - posY1) > 20) {
							posX1 = posX2;
							posY1 = posY2;
						}
					} else if (mode == ZOOM) { // 핀치 중
						newDist = spacing(event);
						Log.d("zoom", "newDist=" + newDist);
						Log.d("zoom", "oldDist=" + oldDist);
						if (newDist - oldDist > 20) { // zoom in
							oldDist = newDist;
							mode = ZOOM_IN;
						} else if (oldDist - newDist > 20) { // zoom out
							oldDist = newDist;
						}
					}
					break;
				case MotionEvent.ACTION_UP: // 첫번째 손가락을 떼었을 경우
				case MotionEvent.ACTION_POINTER_UP: // 두번째 손가락을 떼었을 경우
					mode = NONE;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					//두번째 손가락 터치(손가락 2개를 인식하였기 때문에 핀치 줌으로 판별)
					mode = ZOOM;

					newDist = spacing(event);
					oldDist = spacing(event);

					Log.d("zoom", "newDist=" + newDist);
					Log.d("zoom", "oldDist=" + oldDist);
					Log.d("zoom", "mode=ZOOM");
					break;
				case MotionEvent.ACTION_CANCEL:
				default:
					break;
			}
		}

		if (mode == ZOOM_IN) {
			mode = MAG;
            View root = null;

			if (popup != null) {
				root = popup.getContentView().getRootView();
			} else if (nowContentFragment != null) {
				Log.d(TAG, nowContentFragment.getClass().getSimpleName());
				if (nowContentFragment.getClass().getSimpleName().equals("MeasureLDIFragment")) {
					root = ((MeasureLDIFragment) nowContentFragment).getMagView();
					((MeasureLDIFragment) nowContentFragment).hidePopup();
				} else if (nowContentFragment.getClass().getSimpleName().equals("AccountManagementFragment")) {
					root = ((AccountManagementFragment) nowContentFragment).getMagView();
					((AccountManagementFragment) nowContentFragment).hidePopup();
				} else if (nowContentFragment.getClass().getSimpleName().equals("AlarmFragment")) {
					root = ((AlarmFragment) nowContentFragment).getMagView();
					((AlarmFragment) nowContentFragment).hidePopup();
				} else if (nowContentFragment.getClass().getSimpleName().equals("ChangePWFragment")) {
					root = ((ChangePWFragment) nowContentFragment).getMagView();
					((ChangePWFragment) nowContentFragment).hidePopup();
				} else if (nowContentFragment.getClass().getSimpleName().equals("InfoManagementFragment")) {
					root = ((InfoManagementFragment) nowContentFragment).getMagView();
					((InfoManagementFragment) nowContentFragment).hidePopup();
				} else if (nowContentFragment.getClass().getSimpleName().equals("StatisticsFragment")) {
					root = ((StatisticsFragment) nowContentFragment).getMagView();
					((StatisticsFragment) nowContentFragment).hidePopup();
				} else if (nowContentFragment.getClass().getSimpleName().equals("MemberLeaveFragment")) {
					root = ((MemberLeaveFragment) nowContentFragment).getMagView();
					((MemberLeaveFragment) nowContentFragment).hidePopup();
				} else if (nowContentFragment.getClass().getSimpleName().equals("MyInfoFragment")) {
					root = ((MyInfoFragment) nowContentFragment).getMagView();
					((MyInfoFragment) nowContentFragment).hidePopup();
				} else {
					root = findViewById(R.id.main_activity).getRootView();
				}
			} else {
				root = findViewById(R.id.main_activity).getRootView();
			}
			root.clearAnimation();
			root.refreshDrawableState();
			root.invalidate();
			root.buildDrawingCache();
			Bitmap bmp = Bitmap.createBitmap(root.getDrawingCache());

			findViewById(R.id.capture_layer).setVisibility(View.VISIBLE);

			if (popup != null) {
				popup.dismiss();
				popup = null;
			}

			if (bmp != null) {
				if (drawerLayout.isDrawerOpen(findViewById(R.id.nav_view)))
					drawerLayout.closeDrawer(findViewById(R.id.nav_view));
				setDrawerLayoutLocked(true);
				captureView.setImageBitmap(bmp);
				root.destroyDrawingCache();

				captureView.setScaleX(2.0f);
				captureView.setScaleY(2.0f);
				captureView.setScaleType(ImageView.ScaleType.CENTER);

				Display display = getWindowManager().getDefaultDisplay();

				Point size = new Point();
				display.getRealSize(size);
				int screen_width = size.x;
				int screen_height = size.y;

				maxX = screen_width / 4;
				maxY = screen_height / 4;

				// set scroll limits
				final int maxLeft = (maxX * -1);
				final int maxRight = maxX;
				final int maxTop = (maxY * -1);
				final int maxBottom = maxY;

				captureView.setOnTouchListener(new View.OnTouchListener() {

					float downX, downY;
					int totalX, totalY;
					int scrollByX, scrollByY;

					@Override
					public boolean onTouch(View view, MotionEvent event)
					{
						float currentX, currentY;
						switch (event.getAction())
						{
							case MotionEvent.ACTION_DOWN:
								downX = event.getX();
								downY = event.getY();
								break;

							case MotionEvent.ACTION_MOVE:
								currentX = event.getX();
								currentY = event.getY();
								scrollByX = (int)(downX - currentX);
								scrollByY = (int)(downY - currentY);

								// scrolling to left side of image (pic moving to the right)
								if (currentX > downX) {
									if (totalX == maxLeft) {
										scrollByX = 0;
									}
									if (totalX > maxLeft) {
										totalX = totalX + scrollByX;
									}
									if (totalX < maxLeft) {
										scrollByX = maxLeft - (totalX - scrollByX);
										totalX = maxLeft;
									}
								}

								// scrolling to right side of image (pic moving to the left)
								if (currentX < downX) {
									if (totalX == maxRight) {
										scrollByX = 0;
									}
									if (totalX < maxRight) {
										totalX = totalX + scrollByX;
									}
									if (totalX > maxRight) {
										scrollByX = maxRight - (totalX - scrollByX);
										totalX = maxRight;
									}
								}

								// scrolling to top of image (pic moving to the bottom)
								if (currentY > downY) {
									if (totalY == maxTop) {
										scrollByY = 0;
									}
									if (totalY > maxTop) {
										totalY = totalY + scrollByY;
									}
									if (totalY < maxTop) {
										scrollByY = maxTop - (totalY - scrollByY);
										totalY = maxTop;
									}
								}

								// scrolling to bottom of image (pic moving to the top)
								if (currentY < downY) {
									if (totalY == maxBottom) {
										scrollByY = 0;
									}
									if (totalY < maxBottom) {
										totalY = totalY + scrollByY;
									}
									if (totalY > maxBottom) {
										scrollByY = maxBottom - (totalY - scrollByY);
										totalY = maxBottom;
									}
								}

								captureView.scrollBy(scrollByX, scrollByY);
								downX = currentX;
								downY = currentY;
								break;
						}

						return true;
					}
				});

				findViewById(R.id.close_btn).setOnClickListener(new OnSingleClickListener() {
					@Override
					public void onSingleClick(View v) {
						// reset
						captureView.setScaleX(1.0f);
						captureView.setScaleY(1.0f);
						captureView.setImageResource(R.color.transparent);
						captureView.setScrollX(0);
						captureView.setScrollY(0);
						captureView.clearAnimation();
						captureView.refreshDrawableState();
						captureView.invalidate();
						findViewById(R.id.capture_layer).setVisibility(View.GONE);
						setDrawerLayoutLocked(false);
						mode = NONE;
						if (popupState != NONE) {
							switch (popupState) {
								case ABOUT:
									showLDIAboutPopup();
									break;
								case NOTUSE:
									showNotUsePopup();
									break;
								case COMPLETE:
									showCompletePopup();
									break;
								case HISTORY:
									showBeltHistoryPopup();
									break;
								case BATTERYLOW:
									showBatteryLowPopup();
									break;
								case FIRMWARE:
									showUpdateFirmwarePopup();
									break;
							}
						} else if (nowContentFragment != null) {
							if (nowContentFragment.getClass().getSimpleName().equals("MeasureLDIFragment")) {
								((MeasureLDIFragment) nowContentFragment).reshowPopup();
							} else if (nowContentFragment.getClass().getSimpleName().equals("AccountManagementFragment")) {
								((AccountManagementFragment) nowContentFragment).reshowPopup();
							} else if (nowContentFragment.getClass().getSimpleName().equals("AlarmFragment")) {
								((AlarmFragment) nowContentFragment).reshowPopup();
							} else if (nowContentFragment.getClass().getSimpleName().equals("ChangePWFragment")) {
								((ChangePWFragment) nowContentFragment).reshowPopup();
							} else if (nowContentFragment.getClass().getSimpleName().equals("InfoManagementFragment")) {
								((InfoManagementFragment) nowContentFragment).reshowPopup();
							} else if (nowContentFragment.getClass().getSimpleName().equals("StatisticsFragment")) {
								((StatisticsFragment) nowContentFragment).reshowPopup();
							} else if (nowContentFragment.getClass().getSimpleName().equals("MemberLeaveFragment")) {
								((MemberLeaveFragment) nowContentFragment).reshowPopup();
							} else if (nowContentFragment.getClass().getSimpleName().equals("MyInfoFragment")) {
								((MyInfoFragment) nowContentFragment).reshowPopup();
							} else {
								//NO POPUP
							}
						}
					}
				});
			}
			else {
				Log.d(TAG, "bmp is null");
				mode = NONE;
			}
		}
		return super.dispatchTouchEvent(event);
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                    sensorEvent.values[0]);
        if (sensorEvent.values[0] > Integer.MAX_VALUE || sensorEvent.values[0] == 0) {
            return;
        }

        since_boot = (int) sensorEvent.values[0];

        Database db = Database.getInstance(this);
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -since_boot;
            db.insertNewDay(Util.getToday(), since_boot);
        } else {
            db.saveCurrentSteps(since_boot);
        }
        db.close();

        updateTodaySteps();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // Adapter for holding devices found through scanning.
    private class HistoryListAdapter extends BaseAdapter {
        private ArrayList<BeltHistory> mHistories;
        private ArrayList<ViewItem> mItems = new ArrayList<>();

        public HistoryListAdapter() {
            super();
            mHistories = new ArrayList<>();
        }

        public void addItem(BeltHistory history) {
            mHistories.add(history);
            ViewItem item = new ViewItem();
            item.isSelected = true;
            mItems.add(item);
        }
        public void addItems(ArrayList<BeltHistory> histories) {
            for (BeltHistory history : histories) {
                addItem(history);
            }
        }
        public void setSelectedHolder(int position) {
            ViewItem selectedHolder = mItems.get(position);

            selectedHolder.isSelected = !selectedHolder.isSelected;

            notifyDataSetChanged();
        }
        public ArrayList<BeltHistory> getSelectedHistories() {
            ArrayList<BeltHistory> returnData = new ArrayList<>();

            for (int i = 0; i < mItems.size(); i++) {
                if (mItems.get(i).isSelected) {
                    returnData.add(mHistories.get(i));
                }
            }

            return returnData;
        }
		public ArrayList<BeltHistory> getUnselectedHistories() {
			ArrayList<BeltHistory> returnData = new ArrayList<>();

			for (int i = 0; i < mItems.size(); i++) {
				if (!mItems.get(i).isSelected) {
					returnData.add(mHistories.get(i));
				}
			}

			return returnData;
		}
        public void clear() {
            mHistories.clear();
            mItems.clear();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }
        @Override
        public Object getItem(int i) {
            return mItems.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewItem viewHolder = mItems.get(i);
            // General ListView optimization code.
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_listitem_device, null);
            }

            viewHolder.historyBody = (TextView) view.findViewById(R.id.list_item_device_name);
            viewHolder.checkImageView = (ImageView) view.findViewById(R.id.list_item_check);

            StringBuilder body = new StringBuilder();
            body.append(new SimpleDateFormat("yyyy.MM.dd").format(new Date(mHistories.get(i).getStartTime() * 1000)));
            body.append(" ");
			body.append(new SimpleDateFormat("HH:mm").format(new Date(mHistories.get(i).getStartTime() * 1000)));
            body.append(" ");
            body.append(mHistories.get(i).getUsingTime() / 60);
            body.append(getString(R.string.minute_en));

            viewHolder.historyBody.setText(body);
            viewHolder.checkImageView.setActivated(viewHolder.isSelected);
            //viewHolder.checkImageView.setVisibility(viewHolder.isSelected ? View.VISIBLE : View.INVISIBLE);

            return view;
        }
    }
    class ViewItem {
        TextView historyBody;
        ImageView checkImageView;
        boolean isSelected;
    }

    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/firmware.zip");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                if (BuildConfig.DEBUG) Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //Log.d(TAG, progress[0]);
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // Displaying downloaded image into image view
            // Reading image path from sdcard
            getFirmwareData().setFilePath(Environment.getExternalStorageDirectory().toString() + "/firmware.zip");
            //updateFirmware();
            showUpdateFirmwarePopup();
        }

    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null)
                bmImage.setImageBitmap(result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            }
        }
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private void runPutWalkLogs() {

        try {
            Database database = Database.getInstance(this);
            int today_offset = database.getSteps(Util.getToday());
            // read todays offset
            todayOffset = today_offset;
            int steps = database.getCurrentSteps();
            if (steps > 0) {
                if (today_offset == Integer.MIN_VALUE) today_offset = -steps;
                steps = (today_offset + steps);
            }
            DateFormat dateFormat = DateFormat.getDateInstance();
            String day = dateFormat.format(Util.getToday());
            Log.e(TAG, "" + day + ", count:" + steps);
            if(steps >= 0) { // TODO 음수일 경우 처리 필요
                if (null != mainUsageOnView) {
                    TextView tvOnTodaySteps = mainUsageOnView.findViewById(R.id.tvTodaySteps);
                    tvOnTodaySteps.setText("" + steps);
                }
                if (null != mainUsageOffView) {
                    TextView tvOffTodaySteps = mainUsageOffView.findViewById(R.id.tvTodaySteps);
                    tvOffTodaySteps.setText("" + steps);
                }
                requestTodayPutWalkLogIfNeeded(steps);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!isRunUnpostedPutWalkLogs) {
                    isRunUnpostedPutWalkLogs = true;
                    requestUnpostedPutWalkLogs();
                    isRunUnpostedPutWalkLogs = false;
                }
            }
        }).start();
    }

    private void requestUnpostedPutWalkLogs() {
        SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        long lastPutWalkLogDate = prefs.getLong("lastPutWalkLogDate", 0);
        Date lastDate = new Date(lastPutWalkLogDate);
        Log.d(TAG, "lastPutWalkLogDate : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(lastDate));
        DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String lastPostDay = dateformat.format(lastDate);
        String today = dateformat.format(new Date());
        if(lastPostDay.equals(today)) {
            return;
        }

        Database database = Database.getInstance(this);

        final long limitDays = TimeUnit.DAYS.toMillis(30);
        long before30DayDate = System.currentTimeMillis() - limitDays;
        Log.d(TAG, "30 days before : " + dateformat.format(new Date(before30DayDate)));
        if (lastPutWalkLogDate < before30DayDate) {
            lastPutWalkLogDate = before30DayDate;
        }

        final long aDayMillis = TimeUnit.DAYS.toMillis(1);
        for(long aDay = lastPutWalkLogDate; aDay < Util.getToday(); aDay += aDayMillis) {
            Date aDate = new Date(aDay);
            Log.d(TAG, "check day : " + dateformat.format(aDate));
            final int steps = database.getSteps(Util.getStartOfDay(aDay));
            if (steps > 0) {
                final long finalADay = aDay;
                Log.d(TAG, "missing day : " + dateformat.format(new Date(finalADay)) + ", steps: " + steps);
                requestPutWalkLog(aDate, steps, new PutWalkLogRequestListener() {
                    @Override
                    public void run() {
                        if (isSuccess || walks >= steps ) {
                            SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
                            prefs.edit().putLong("lastPutWalkLogDate", finalADay).apply();
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        database.close();
    }

    abstract class PutWalkLogRequestListener implements Runnable {

        protected boolean isSuccess;

        public void setWalks(int walks) {
            this.walks = walks;
        }

        protected int walks;

        public void setSuccess(boolean success) {
            isSuccess = success;
        }
    }


    private void requestTodayPutWalkLogIfNeeded(int steps) {
        final SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        long todayPutWalkLogDate = prefs.getLong("todayPutWalkLogDate", 0);
        Log.d(TAG, "todayPutWalkLogDate : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(todayPutWalkLogDate)));
        if (System.currentTimeMillis() - todayPutWalkLogDate > TimeUnit.HOURS.toMillis(0)) {
            requestPutWalkLog(new Date(), steps, new PutWalkLogRequestListener() {
                @Override
                public void run() {
                    if (isSuccess) {
                        prefs.edit().putLong("todayPutWalkLogDate", System.currentTimeMillis()).apply();
                    }
                }
            });
        }
    }

    private void requestPutWalkLog(Date logDate, int steps, final PutWalkLogRequestListener listener) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            JSONObject json = new JSONObject();
            json.put("func", "put_walklog");
//            json.put("userid", "");
            String loggedDate = dateFormat.format(logDate);
            json.put("logged", loggedDate);
            json.put("masterkey", getUserData().getMasterKey());
            json.put("attr", "walks");
            json.put("measure", steps);
            json.put("unit", "step");

            // TODO 개발용 서버 접속 운영배포시에는 변경 필요
            new JSONNetworkManager(JSONNetworkManager.WALKS_INFO, json, Constants.IS_USE_DEV_SERVER) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);
                    Log.e(TAG, "error status = " + status);
                    if (null != listener)
                        listener.run();
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    Log.d(TAG, "response = " + responseJson.toString());
                    String status = responseJson.optString("status");
                    if ("success".equalsIgnoreCase(status)) {
                        JSONObject data = responseJson.optJSONObject("data");
                        if( null != data ) {
                            int inserted = data.optInt("inserted");
                            if (inserted > 0) {
                                Log.d(TAG, "put_walklog success:" + inserted);
                                if (null != listener)
                                    listener.setSuccess(true);
                            }
                            int walks = data.optInt("walks");
                            if (walks > 0 ) {
                                if (null != listener)
                                    listener.setWalks(walks);
                            }
                        }
                    }
                    if (null != listener)
                        listener.run();
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
            if (null != listener)
                listener.run();
        }
    }

    public void refreshLDIUsageFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_flayout_ldi_usage);
        if (null != fragment) {
            if (fragment instanceof MainLDIUsageStepsRecordFragment) {
                ((MainLDIUsageStepsRecordFragment)fragment).refresh();
            }
        }
    }

    private void requestGetWalkSummary() {
        try {
            JSONObject json = new JSONObject();
            json.put("func", "get_walk_summary");
//            json.put("userid", "");
            json.put("masterkey", getUserData().getMasterKey());
            json.put("attr", "walks");


            // TODO 개발용 서버 접속 운영배포시에는 변경 필요
            new JSONNetworkManager(JSONNetworkManager.WALKS_INFO, json, Constants.IS_USE_DEV_SERVER) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);
                    Log.e(TAG, "error status = " + status);
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    Log.d(TAG, "response = " + responseJson.toString());
                    String status = responseJson.optString("status");
                    if ("success".equalsIgnoreCase(status)) {
                        JSONObject data = responseJson.optJSONObject("data");
                        if( null != data ) {
                            ArrayList<WalkLog> walkLogs = new ArrayList<>();
                            WalkLog walkLog = gson.fromJson(data.toString(), WalkLog.class);
                            walkLogs.add(walkLog);

                            cumulativeStepsAndRankWalkLogs = walkLogs;

                            updateWalkLogs(walkLogs);
                        }
                    }
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestCumulativeStepsAndRanking() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            final JSONObject json = new JSONObject();
            json.put("func", "list_walk_summary");
//            json.put("userid", "");
            String date = dateFormat.format(new Date());
            json.put("base", date);
            json.put("masterkey", getUserData().getMasterKey());
            json.put("section", "walks");
            json.put("scale", "todate");


            new JSONNetworkManager(JSONNetworkManager.WALKS_INFO, json, Constants.IS_USE_DEV_SERVER) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);
                    Log.e(TAG, "error status = " + status);
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    Log.d(TAG, "response = " + responseJson.toString());
                    String status = responseJson.optString("status");
                    if ("success".equalsIgnoreCase(status)) {
                        JSONObject data = responseJson.optJSONObject("data");
                        if( null != data ) {
                            String baseDate = data.optString("base");
                            String scale = data.optString("scale");
                            JSONArray logs = data.optJSONArray("log");
                            if (null != logs) {
                                ArrayList<WalkLog> walkLogs = new ArrayList<>();
                                for(int i = 0; i < logs.length(); ++i) {
                                    JSONObject jsonObject = logs.optJSONObject(i);
                                    if (null != jsonObject) {
                                        WalkLog walkLog = gson.fromJson(jsonObject.toString(), WalkLog.class);

                                        walkLogs.add(walkLog);

                                    }
                                }

                                cumulativeStepsAndRankWalkLogs = walkLogs;

                                updateWalkLogs(walkLogs);
                            }
                        }
                    }
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateWalkLogs(ArrayList<WalkLog> walkLogs) {
        if (walkLogs.size() > 0) {
            final WalkLog walkLog = walkLogs.get(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String sumScope = walkLog.getSumscope();
                    int cumulative_steps = 0;
                    if (!TextUtils.isEmpty(sumScope)) {
                        cumulative_steps = Integer.valueOf(sumScope);
                    }
                    Database database = Database.getInstance(MainActivity.this);
                    int today_offset = database.getSteps(Util.getToday());
                    int steps = database.getCurrentSteps();
                    database.close();
                    if (steps > 0) {
                        if (today_offset == Integer.MIN_VALUE) today_offset = -steps;
                        steps = (today_offset + steps);
                    }
                    Log.d(TAG, "today steps from db:" + steps + ", cumulative_steps:" + cumulative_steps);
                    updateCumulativeStepsAndRanking(cumulative_steps + steps, walkLog.getRank());
                }
            });
        }
    }

    public void setPedometerOffUI(boolean isEnabled){

        if(mainUsageOnView != null) {
            if (isEnabled) {
                mainUsageOnView.findViewById(R.id.main_rl_pedometer_data).setVisibility(View.VISIBLE);
                mainUsageOnView.findViewById(R.id.main_txt_Pedometer_off).setVisibility(View.GONE);
            } else {
                mainUsageOnView.findViewById(R.id.main_rl_pedometer_data).setVisibility(View.GONE);
                mainUsageOnView.findViewById(R.id.main_txt_Pedometer_off).setVisibility(View.VISIBLE);
            }
        }

        if(mainUsageOffView != null) {
            if (isEnabled) {
                mainUsageOffView.findViewById(R.id.main_rl_pedometer_data).setVisibility(View.VISIBLE);
                mainUsageOffView.findViewById(R.id.main_txt_Pedometer_off).setVisibility(View.GONE);
            } else {
                mainUsageOffView.findViewById(R.id.main_rl_pedometer_data).setVisibility(View.GONE);
                mainUsageOffView.findViewById(R.id.main_txt_Pedometer_off).setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * @param cumulativeSteps 누적걸음
     * @param stepRanking 누적랭킹
     */
    private void updateCumulativeStepsAndRanking(int cumulativeSteps, int stepRanking) {
        if(mainUsageOnView != null) {
            ((TextView) mainUsageOnView.findViewById(R.id.tvMaxSteps)).setText(NumberFormatUtil.commaedNumber(cumulativeSteps));
            ((TextView) mainUsageOnView.findViewById(R.id.tvStepsRanking)).setText(NumberFormatUtil.commaedNumber(stepRanking));
        }

        if(mainUsageOffView != null) {
            ((TextView) mainUsageOffView.findViewById(R.id.tvMaxSteps)).setText(NumberFormatUtil.commaedNumber(cumulativeSteps));
            ((TextView) mainUsageOffView.findViewById(R.id.tvStepsRanking)).setText(NumberFormatUtil.commaedNumber(stepRanking));
        }
    }
}