package com.doubleh.lumidiet;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.data.LDIValue;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.doubleh.lumidiet.utils.HoloCircularProgressBar;
import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.OnSingleClickListener;
import com.doubleh.lumidiet.utils.SlidingImage_Adapter;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.doubleh.lumidiet.BaseActivity.hideSoftKeyboardFromFocusedView;
import static com.doubleh.lumidiet.BaseActivity.isLDIView;


/*import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Environment;*/
// 11111 test

/**
 * A simple {@link Fragment} subclass.
 */
public class MeasureLDIFragment extends Fragment implements View.OnClickListener {
    String TAG = "MeasureLDIFragment";

    private Button reset_Btn;
    private TextView result_TxtView, resultMsg_TxtView, unit_TxtView;
    private ImageView emoticon_ImageView, icon_ImageView;
    private View mView;
    private PopupWindow popup, popup2;
    private RelativeLayout popup_back_Layer, ok_Btn;

    private String measureMsg;

    private String KEY_AGAIN = "isAgain";

    private final int RESULT_MEASURE = 0;
    private final int RESULT_INCREASE = 1;
    private final int RESULT_DECREASE = 2;
    private final int RESULT_SAME = 3;
    private final int RESULT_FIRST = 4;

    private HoloCircularProgressBar mHoloCircularProgressBar;
    private ObjectAnimator mProgressBarAnimator;

    private int rawLDIValue;
    private final int measureDuration = 10000;
    private final int MEASURE_START_TIME = 3000;
    private final int LDI_BASEMENT = 150;
    private int progressTime = 11;
    private LDIValue ldiValue = null, standardLdiValue;

    private MainActivity mActivity;

    // popup
    private ViewPager mPager;
    private int currentPage = 0;
    private int NUM_PAGES = 0;
    private final Integer[] IMAGES= {R.drawable.popup_guide_1, R.drawable.popup_guide_2, R.drawable.popup_guide_3, R.drawable.popup_guide_4, R.drawable.popup_guide_5};
    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();

    final Handler startHandler = new Handler() {
        public void handleMessage(Message msg) {
            showInputPopup();
        }
    };
    final Handler endHandler = new Handler() {
        public void handleMessage(Message msg) {
            animationEnd();
        }
    };
    // test 100 count
    /*final Handler testHandler = new Handler() {
        public void handleMessage(Message msg) {
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.layout_popup_type2_3l, null);
            popup_back_Layer.setVisibility(View.VISIBLE);
            popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
            popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
            RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_3l_btn_ok);

            String body = "LDI value test success\nFile path is\n";
            body += path.toString();

            TextView tv = (TextView) layout.findViewById(R.id.popup_type2_3l_body);
            tv.setText(body);
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup.dismiss();
                    popup = null;
                    popup_back_Layer.setVisibility(View.INVISIBLE);
                }
            });
        }
    };*/
    final CountDownTimer countDownTimer = new CountDownTimer(measureDuration + 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            result_TxtView.setText(Integer.toString(--progressTime));
        }

        @Override
        public void onFinish() {
            result_TxtView.setText(Integer.toString(--progressTime));
            progressTime = 11;
        }
    };

    EditText age_EditText, height_EditText, weight_EditText;
    RadioButton male_Radio, female_Radio;

    Handler measureLDIHandler;
    Runnable measureLDIRunnable = new Runnable() {
        @Override
        public void run() {
            mActivity.getMeasureLDIFromBelt();
        }
    };

    enum PopupState {
		NONE, INFO, INPUT, RESET, EMPTY, RANGE
	};

	PopupState state = PopupState.NONE;

    protected boolean mAnimationHasEnded = false;
    protected boolean isBackKeyDownDismiss = true;

    //test
    /*int count = 0;
    File textFile;
    FileOutputStream fos;
    String writeData;
    StringBuilder path;*/

    public MeasureLDIFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_measure_ldi, container, false);
        mActivity = (MainActivity) getActivity();

        // test code
        /*path = new StringBuilder(Environment.getExternalStorageDirectory().getPath());
        path.append("/ldi_data/");
        textFile = new File(path.toString());
        if (!textFile.exists()) {
            textFile.mkdir();
        }
        path.append("measure_ldi_value_");
        path.append(Calendar.getInstance().getTimeInMillis());
        path.append(".txt");

        textFile = new File(path.toString());
        try {
            if (!textFile.exists()) {
                Log.d(TAG, "create new text file");
                textFile.createNewFile();
                fos = new FileOutputStream(textFile);
            }
            else {
                Log.d(TAG, "text file exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // add code
        Button prev_Btn = (Button) mView.findViewById(R.id.measure_ldi_btn_prev);
        prev_Btn.setOnClickListener(this);
        reset_Btn = (Button) mView.findViewById(R.id.measure_ldi_btn_reset);
        reset_Btn.setOnClickListener(this);

        result_TxtView = (TextView) mView.findViewById(R.id.measure_ldi_txt_result);
        resultMsg_TxtView = (TextView) mView.findViewById(R.id.measure_ldi_txt_result_msg);
        unit_TxtView = (TextView) mView.findViewById(R.id.measure_ldi_txt_unit);
        emoticon_ImageView = (ImageView) mView.findViewById(R.id.measure_ldi_img_emoticon);
        icon_ImageView = (ImageView) mView.findViewById(R.id.measure_ldi_img_icon);

        popup_back_Layer = (RelativeLayout) mView.findViewById(R.id.popup_layer);

        ok_Btn = (RelativeLayout) mView.findViewById(R.id.measure_ldi_btn_ok);
        ok_Btn.setOnClickListener(this);

        measureMsg = getString(R.string.measure_msg);

        mView.findViewById(R.id.measure_ldi_img_base_graph_bar).getLayoutParams().height = (int)(50.0f * mActivity.getDensity());

        mHoloCircularProgressBar = (HoloCircularProgressBar) mView.findViewById(R.id.measure_ldi_holoCircularProgressBar);

        mHoloCircularProgressBar.setVisibility(View.VISIBLE);
        mHoloCircularProgressBar.setBackgroundColor(getResources().getColor(R.color.color00FFFFFF));
        mHoloCircularProgressBar.setProgressBackgroundColor(getResources().getColor(R.color.color00FFFFFF));
        mHoloCircularProgressBar.setDrawingCacheBackgroundColor(getResources().getColor(R.color.color00FFFFFF));
        mHoloCircularProgressBar.setProgressColor(getResources().getColor(R.color.colorFF0CCB54));
        mHoloCircularProgressBar.setMarkerEnabled(false);
        mHoloCircularProgressBar.setThumbImageView((ImageView)mView.findViewById(R.id.measure_ldi_img_circle));
        mHoloCircularProgressBar.showImageView();
        mHoloCircularProgressBar.setWheelSize(Math.round(4.0f * mActivity.getDensity()));
        mHoloCircularProgressBar.setCircleStrokeWidth(Math.round(2.5f * mActivity.getDensity()));
        mHoloCircularProgressBar.setThumbImageLayout((RelativeLayout)mView.findViewById(R.id.measure_ldi_img_circle_layout));
        mHoloCircularProgressBar.setStartProgress(0.0f);

        animate(mHoloCircularProgressBar, new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationEnd!");
                mAnimationHasEnded = true;
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onAnimationStart!");
                mAnimationHasEnded = false;
            }
        }, 1.0f, measureDuration);

        for(int i=0;i<IMAGES.length;i++)
            ImagesArray.add(IMAGES[i]);

        setResultView(RESULT_MEASURE);
        //
        if (mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).getBoolean(KEY_AGAIN, true)) {
            showPopup();
        } else {
            showInputPopup();
        }

        if (isLDIView) {
            mView.findViewById(R.id.ldi_test).setVisibility(View.VISIBLE);
        }

        measureLDIHandler = new Handler();

        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (measureLDIHandler != null)
            measureLDIHandler.removeCallbacks(measureLDIRunnable);

        if (popup != null) {
            popup.dismiss();
            popup = null;
        }

        if (mProgressBarAnimator.isRunning())
            mProgressBarAnimator.end();
        countDownTimer.cancel();
    }

    void setResultView(int focus) {
        if (focus == RESULT_MEASURE) {
            result_TxtView.setText("10");
            unit_TxtView.setText(getString(R.string.second_en));
            emoticon_ImageView.setVisibility(View.INVISIBLE);
            icon_ImageView.setVisibility(View.INVISIBLE);

            resultMsg_TxtView.setText(measureMsg);
        }
        else if (focus == RESULT_FIRST) {
            result_TxtView.setText("0");
            unit_TxtView.setText("%");
            emoticon_ImageView.setVisibility(View.VISIBLE);
            icon_ImageView.setVisibility(View.VISIBLE);

            resultMsg_TxtView.setText(getString(R.string.measure_first_msg));

            mView.findViewById(R.id.measure_ldi_img_measure_graph_bar).getLayoutParams().height
                    = Math.round (50.0f * mActivity.getDensity());

            emoticon_ImageView.setActivated(true);
            icon_ImageView.setActivated(true);
            result_TxtView.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFF11A0E3));
            mView.findViewById(R.id.measure_ldi_img_measure_graph_dot).setActivated(true);
            mView.findViewById(R.id.measure_ldi_img_measure_graph_bar).setActivated(true);

            Calendar c = Calendar.getInstance();
            // standard data
            StringBuilder date = new StringBuilder();
            date.append(c.get(Calendar.MONTH) + 1);
            date.append("/");
            date.append(c.get(Calendar.DAY_OF_MONTH));
            ((TextView) mView.findViewById(R.id.measure_ldi_txt_base_days)).setText(date);

            // measure data
            ((TextView) mView.findViewById(R.id.measure_ldi_txt_measure_days)).setText(date);
            ((TextView) mView.findViewById(R.id.measure_ldi_txt_measure_ldi)).setText("100");
        }
        else {
            unit_TxtView.setText("%");
            emoticon_ImageView.setVisibility(View.VISIBLE);
            icon_ImageView.setVisibility(View.VISIBLE);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(standardLdiValue.getMeasureTime() * 1000);

            SpannableStringBuilder msg;

            if (focus == RESULT_DECREASE) {
                msg = new SpannableStringBuilder(getString(R.string.measure_msg_decrease, c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
            } else if (focus == RESULT_INCREASE) {
                msg = new SpannableStringBuilder(getString(R.string.measure_msg_increase, c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
            } else {
                msg = new SpannableStringBuilder(getString(R.string.measure_msg_same, c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
            }
            if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko")) {         // korean
                if (focus == RESULT_DECREASE) {
                    int idx = msg.toString().indexOf("감소");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focus == RESULT_INCREASE) {
                    int idx = msg.toString().indexOf("증가");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    int idx = msg.toString().indexOf("동일");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja")) {  // japanese
                if (focus == RESULT_DECREASE) {
                    int idx = msg.toString().indexOf("減少");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focus == RESULT_INCREASE) {
                    int idx = msg.toString().indexOf("増加");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    int idx = msg.toString().indexOf("同じ");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh")) {  // chinese
                if (focus == RESULT_DECREASE) {
                    int idx = msg.toString().indexOf("减少了");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focus == RESULT_INCREASE) {
                    int idx = msg.toString().indexOf("增加了");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    int idx = msg.toString().indexOf("一样");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("tr")) {  // turkey
                if (focus == RESULT_DECREASE) {
                    int idx = msg.toString().indexOf("azaldı");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focus == RESULT_INCREASE) {
                    int idx = msg.toString().indexOf("arttı");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    int idx = msg.toString().indexOf("aynı");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {                                                                   // default - english (usa)
                if (focus == RESULT_DECREASE) {
                    int idx = msg.toString().indexOf("decreased");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (focus == RESULT_INCREASE) {
                    int idx = msg.toString().indexOf("increased");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    int idx = msg.toString().indexOf("not");
                    msg.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.colorFF00BD47)), idx, idx+11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            resultMsg_TxtView.setText(msg);

            float percentage = (float) ldiValue.getLdiValue() / (float) standardLdiValue.getLdiValue();

            // 비율 계산을 위해 1보다 크면 1을 빼줘야 하고 1보다 작으면 1에서 빼줘야하고. 이후 곱하기 100 (정수로 만들기 위해)
            percentage = percentage > 1.0f ? percentage - 1.0f : 1.0f - percentage;
            percentage *= 100.0f;

            //반올림으로 변경 - 0612
            //result_TxtView.setText(Integer.toString((int)percentage));
            result_TxtView.setText(Integer.toString(Math.round(percentage)));

            // standard data
            StringBuilder date1 = new StringBuilder();
            date1.append(c.get(Calendar.MONTH) + 1);
            date1.append("/");
            date1.append(c.get(Calendar.DAY_OF_MONTH));
            ((TextView) mView.findViewById(R.id.measure_ldi_txt_base_days)).setText(date1);

            // measure data
            c.setTimeInMillis(ldiValue.getMeasureTime() * 1000);
            StringBuilder date2 = new StringBuilder();
            date2.append(c.get(Calendar.MONTH) + 1);
            date2.append("/");
            date2.append(c.get(Calendar.DAY_OF_MONTH));
            ((TextView) mView.findViewById(R.id.measure_ldi_txt_measure_days)).setText(date2);
            //((TextView) mView.findViewById(R.id.measure_ldi_txt_measure_ldi))
                    //.setText(Integer.toString((int)(100.0f * (float) ldiValue.getLdiValue() / (float) standardLdiValue.getLdiValue())));
            //반올림 변경에 따른 수식 변화 - 0612
            if (focus == RESULT_INCREASE) {
                ((TextView) mView.findViewById(R.id.measure_ldi_txt_measure_ldi))
                        .setText(Integer.toString(100 + Math.round(percentage)));
            } else {
                ((TextView) mView.findViewById(R.id.measure_ldi_txt_measure_ldi))
                        .setText(Integer.toString(100 - Math.round(percentage)));
            }

            float graphValue = (50 * (float) ldiValue.getLdiValue() / (float) standardLdiValue.getLdiValue());
            if (graphValue >= 100.0f) {
                graphValue = 100.0f;
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
            }
            mView.findViewById(R.id.measure_ldi_img_measure_graph_bar).getLayoutParams().height
                    = Math.round (graphValue * mActivity.getDensity());

            if (focus == RESULT_INCREASE) {
                emoticon_ImageView.setActivated(false);
                icon_ImageView.setActivated(false);
                result_TxtView.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFFFF0042));
                mView.findViewById(R.id.measure_ldi_img_measure_graph_dot).setActivated(false);
                mView.findViewById(R.id.measure_ldi_img_measure_graph_bar).setActivated(false);
            } else if (focus == RESULT_DECREASE || focus == RESULT_SAME) {
                emoticon_ImageView.setActivated(true);
                icon_ImageView.setActivated(true);
                result_TxtView.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFF11A0E3));
                mView.findViewById(R.id.measure_ldi_img_measure_graph_dot).setActivated(true);
                mView.findViewById(R.id.measure_ldi_img_measure_graph_bar).setActivated(true);
            }
        }
        ((MainLDIUsageStepsRecordFragment) mActivity.mainLDIUsageStepsRecordFragment).setFragment();
    }

    void showPopup() {
		state = PopupState.INFO;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_type1, null);
        popup_back_Layer.setVisibility(View.VISIBLE);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mActivity.dispatchTouchEvent(event);
				return false;
			}
		});

        RelativeLayout leftBtn = (RelativeLayout) layout.findViewById(R.id.popup_type1_btn_ok);

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                popup = null;
                popup_back_Layer.setVisibility(View.INVISIBLE);
                showInputPopup();
            }
        });

        RelativeLayout rightBtn = (RelativeLayout) layout.findViewById(R.id.popup_type1_btn_cancel);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                popup.dismiss();
				popup = null;
                popup_back_Layer.setVisibility(View.INVISIBLE);
                mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).edit().putBoolean(KEY_AGAIN, false).commit();
                showInputPopup();
            }
        });

        mPager = (ViewPager) layout.findViewById(R.id.popup_type1_pager);
        mPager.setAdapter(new SlidingImage_Adapter(getActivity(), ImagesArray));
        CirclePageIndicator indicator = (CirclePageIndicator) layout.findViewById(R.id.popup_type1_indicator);

        indicator.setViewPager(mPager);

        indicator.setRadius(5 * mActivity.getDensity());

        NUM_PAGES =IMAGES.length;

        // Pager listener over indicator
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrolled(int pos, float arg1, int arg2) { }

            @Override
            public void onPageScrollStateChanged(int pos) { }
        });
    }

    int age = -1, height = -1, weight = -1;
    boolean isFocusStart = true;

    void showInputPopup() {
        isFocusStart = true;
		state = PopupState.INPUT;
        isBackKeyDownDismiss = true;
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_type8, null);
        popup_back_Layer.setVisibility(View.VISIBLE);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);
        popup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mActivity.dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout leftBtn = (RelativeLayout) layout.findViewById(R.id.popup_type8_btn_left);
        RelativeLayout rightBtn = (RelativeLayout) layout.findViewById(R.id.popup_type8_btn_right);

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (isBackKeyDownDismiss) {
                    age_EditText.addTextChangedListener(null);
                    age_EditText.setOnFocusChangeListener(null);
                    height_EditText.addTextChangedListener(null);
                    height_EditText.setOnFocusChangeListener(null);
                    weight_EditText.addTextChangedListener(null);
                    weight_EditText.setOnFocusChangeListener(null);

                    popup_back_Layer.setVisibility(View.INVISIBLE);
					state = PopupState.NONE;

                    if (mProgressBarAnimator.isRunning())
                        mProgressBarAnimator.end();
                    countDownTimer.cancel();
                    mActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                }
            }
        });

        layout.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                age_EditText.clearFocus();
                height_EditText.clearFocus();
                weight_EditText.clearFocus();
                removeFocusAll();
            }
        });

        age_EditText = (EditText) layout.findViewById(R.id.input_age);
        height_EditText = (EditText) layout.findViewById(R.id.input_height);
        weight_EditText = (EditText) layout.findViewById(R.id.input_weight);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    return;
                }

                if (popup2 != null) {
                    return;
                }

                int temp = Integer.parseInt(s.toString());
                if (BuildConfig.DEBUG) Log.d(TAG, "final data: "+temp);

                if (temp >= 1000) {
                    age_EditText.addTextChangedListener(null);
                    age_EditText.setOnFocusChangeListener(null);
                    height_EditText.addTextChangedListener(null);
                    height_EditText.setOnFocusChangeListener(null);
                    weight_EditText.addTextChangedListener(null);
                    weight_EditText.setOnFocusChangeListener(null);

                    state = PopupState.RANGE;
                    removeFocusAll();
                    showValueCheckPopup();
                }
            }
        };

        age_EditText.addTextChangedListener(textWatcher);
        height_EditText.addTextChangedListener(textWatcher);
        weight_EditText.addTextChangedListener(textWatcher);

        male_Radio = (RadioButton) layout.findViewById(R.id.radio_male);
        female_Radio = (RadioButton) layout.findViewById(R.id.radio_female);

        if (mActivity.getUserData().getSex() == 0) {
            female_Radio.setChecked(true);
        } else {
            male_Radio.setChecked(true);
        }

        if (mActivity.getUserData().getAge() > 0) {
            age_EditText.setText(Integer.toString(mActivity.getUserData().getAge()));
        }
        if (age != -1) {
            age_EditText.setText(Integer.toString(age));
        }
        if (mActivity.getUserData().getHeight() > 0) {
            height_EditText.setText(Integer.toString(mActivity.getUserData().getHeight()));
        }
        if (height != -1) {
            height_EditText.setText(Integer.toString(height));
        }
        if (mActivity.getUserData().getWeight() > 0) {
            weight_EditText.setText(Integer.toString(mActivity.getUserData().getWeight()));
        }
        if (weight != -1) {
            weight_EditText.setText(Integer.toString(weight));
        }

        age_EditText.setSelection(age_EditText.length());
        height_EditText.setSelection(height_EditText.length());
        weight_EditText.setSelection(weight_EditText.length());

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                age_EditText.addTextChangedListener(null);
                age_EditText.setOnFocusChangeListener(null);
                height_EditText.addTextChangedListener(null);
                height_EditText.setOnFocusChangeListener(null);
                weight_EditText.addTextChangedListener(null);
                weight_EditText.setOnFocusChangeListener(null);

                isBackKeyDownDismiss = false;
                popup.dismiss();
				popup = null;
                popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;

                if (popup2 != null) {
                    popup2.dismiss();
                    popup2 = null;
                }

                if (mProgressBarAnimator.isRunning())
                    mProgressBarAnimator.end();
                countDownTimer.cancel();
                mActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBackKeyDownDismiss = false;

                if (age_EditText.getText().toString().equals("") || height_EditText.getText().toString().equals("") || weight_EditText.getText().toString().equals("")) {
                    state = PopupState.EMPTY;
                    showValueCheckPopup();
                    return;
                }

                if (Integer.parseInt(age_EditText.getText().toString()) < 1 || Integer.parseInt(age_EditText.getText().toString()) > 200 ||
                        Integer.parseInt(height_EditText.getText().toString()) < 50 || Integer.parseInt(height_EditText.getText().toString()) > 250 ||
                        Integer.parseInt(weight_EditText.getText().toString()) < 20 || Integer.parseInt(weight_EditText.getText().toString()) > 250) {
                    state = PopupState.RANGE;
                    showValueCheckPopup();
                    return;
                }

                // send server
                requestUpdateInfo();

                age = -1; height = -1; weight = -1;

                popup.dismiss();
				popup = null;
                popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;
                animationStart();
            }
        });
    }

    void removeFocusAll() {
        hideSoftKeyboardFromFocusedView(getActivity(), age_EditText);
        hideSoftKeyboardFromFocusedView(getActivity(), height_EditText);
        hideSoftKeyboardFromFocusedView(getActivity(), weight_EditText);
    }

    public boolean showValueCheckPopup() {
        if (BuildConfig.DEBUG) Log.d(TAG, "showValueCheckPopup()");

        if (popup2 != null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "already show");
            return false;
        }

        // input popup data save
        if (age_EditText.getText().length() > 0) {
            age = Integer.parseInt(age_EditText.getText().toString());
            // data verify check
            if (age < 1 || age > 200)            { age = -1; }
        }
        if (height_EditText.getText().length() > 0) {
            height = Integer.parseInt(height_EditText.getText().toString());
            if (height < 50 || height > 250)    { height = -1; }
        }
        if (weight_EditText.getText().length() > 0) {
            weight = Integer.parseInt(weight_EditText.getText().toString());
            if (weight < 20 || weight > 250)    { weight = -1; }
        }

        // input popup dismiss
        isBackKeyDownDismiss = false;
        popup.dismiss();
        popup = null;

        removeFocusAll();

        StringBuilder sb = new StringBuilder("");

        if (state == PopupState.EMPTY) {
            sb.append(getString(R.string.measure_input_err_msg));

            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.layout_popup_type2_3l, null);
            popup2 = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
            popup2.showAtLocation(layout, Gravity.CENTER, 0, 0);

            popup2.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mActivity.dispatchTouchEvent(event);
                    return false;
                }
            });

            ((MainActivity) getActivity()).hideUI();

            ((TextView) layout.findViewById(R.id.popup_type2_3l_body)).setText(sb);

            layout.findViewById(R.id.popup_type2_3l_btn_ok).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    popup2.dismiss();
                    popup2 = null;
                    popup_back_Layer.setVisibility(View.INVISIBLE);
                    state = PopupState.NONE;
                    showInputPopup();
                }
            });

            return true;
        } else {
            sb.append(getString(R.string.measure_input_err_msg2));

            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
            popup2 = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
            popup2.showAtLocation(layout, Gravity.CENTER, 0, 0);

            popup2.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mActivity.dispatchTouchEvent(event);
                    return false;
                }
            });

            ((MainActivity) getActivity()).hideUI();

            ((TextView) layout.findViewById(R.id.popup_type2_1l_body)).setText(sb);

            layout.findViewById(R.id.popup_type2_1l_btn_ok).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    popup2.dismiss();
                    popup2 = null;
                    popup_back_Layer.setVisibility(View.INVISIBLE);
                    state = PopupState.NONE;
                    showInputPopup();
                }
            });

            return true;
        }
    }


    public void hidePopup() {
		isBackKeyDownDismiss = false;
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
		if (popup2 != null) {
			popup2.dismiss();
			popup2 = null;
		}
	}

	public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case INFO:
				showPopup();
				break;
			case INPUT:
				showInputPopup();
				break;
            case EMPTY:
            case RANGE:
				showValueCheckPopup();
				break;
			case RESET:
				showResetPopup();
				break;
		}
	}

	public View getMagView() {
		if (popup2 != null) {
			return popup2.getContentView();
		} else if (popup != null) {
			return popup.getContentView();
		} else {
			return getView();
		}
	}

    public void requestUpdateInfo() {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", "up");
            json.put("masterkey", (mActivity.getUserData().getMasterKey()));
            json.put("sex", male_Radio.isChecked() ? 1 : 0);
            json.put("age", age_EditText.getText());
            json.put("height", height_EditText.getText());
            json.put("weight", weight_EditText.getText());
            if (mActivity.getUserData().getFacebook())
                json.put("tp", 2);
            else
                json.put("tp", 1);

            new JSONNetworkManager(JSONNetworkManager.MEMBER, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    ((MainActivity) getActivity()).disconnectBelt(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener)
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    responseUpdateInfo(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void responseUpdateInfo(JSONObject json) {
        try {
            int result = json.getInt("result");
            if (result == 0) {
                if (BuildConfig.DEBUG) Log.d(TAG, "user data update failed");
            }
            else {
                if (BuildConfig.DEBUG) Log.d(TAG, "user data update complete");

                mActivity.getUserData().setMasterKey(json.getString("masterkey"));
                mActivity.getUserData().setUserID(json.getString("userid"));
                mActivity.getUserData().setSex(json.getInt("sex"));
                mActivity.getUserData().setAge(json.getInt("age"));
                mActivity.getUserData().setHeight(json.getInt("height"));
                mActivity.getUserData().setWeight(json.getInt("weight"));
                mActivity.getUserData().setMailChk(json.getString("mail_ch") == "Y" ? true : false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.measure_ldi_btn_prev:
                if (mProgressBarAnimator.isRunning())
                    mProgressBarAnimator.end();
                countDownTimer.cancel();
            case R.id.measure_ldi_btn_ok:
                mActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                break;
            case R.id.measure_ldi_btn_reset:
                // ldi standard reset
                showResetPopup();
                break;
        }

        hideSoftKeyboardFromFocusedView(mActivity, age_EditText);
    }

    public void showResetPopup() {
		state = PopupState.RESET;
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_type3, null);
        popup_back_Layer.setVisibility(View.VISIBLE);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mActivity.dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout okBtn = (RelativeLayout) layout.findViewById(R.id.popup_type3_btn_ok);
        RelativeLayout cancelBtn = (RelativeLayout) layout.findViewById(R.id.popup_type3_btn_cancel);

        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(standardLdiValue.getMeasureTime() * 1000);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(ldiValue.getMeasureTime() * 1000);
        StringBuilder body = new StringBuilder(getString(R.string.change_standard_date));
        body.append(c1.get(Calendar.MONTH) + 1);
        body.append("/");
        body.append(c1.get(Calendar.DAY_OF_MONTH));
        body.append(" > ");
        body.append(c2.get(Calendar.MONTH) + 1);
        body.append("/");
        body.append(c2.get(Calendar.DAY_OF_MONTH));

        TextView tv = (TextView) layout.findViewById(R.id.popup_type3_body);
        tv.setText(body);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
				popup = null;
                popup_back_Layer.setVisibility(View.INVISIBLE);
				//mActivity.popupState = mActivity.NONE;
				state = PopupState.NONE;

                standardLdiValue = ldiValue;

                mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).
                        edit().putLong(MainActivity.Preferences_LDI_STANDARD_DATE, standardLdiValue.getMeasureTime()).commit();

                sendLDIStandardDate(true);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
				popup = null;
                popup_back_Layer.setVisibility(View.INVISIBLE);
				//mActivity.popupState = mActivity.NONE;
				state = PopupState.NONE;
            }
        });
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
        //mProgressBarAnimator.reverse();
        mProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                //Log.d(TAG, "onAnimationUpdate(final ValueAnimator animation)");
                progressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });
        progressBar.setMarkerProgress(progress);
        //mProgressBarAnimator.start();
    }

    void animationStart() {
		mActivity.mode = mActivity.MAG;
        if (mProgressBarAnimator.isStarted())
            mProgressBarAnimator.end();

        ok_Btn.setEnabled(false);
        ok_Btn.findViewById(R.id.measure_ldi_img_ok).setEnabled(false);
        reset_Btn.setEnabled(false);

        mProgressBarAnimator.start();
        countDownTimer.start();

        measureLDIHandler.postDelayed(measureLDIRunnable, MEASURE_START_TIME);
    }

    void animationEnd() {
		mActivity.mode = mActivity.NONE;
        if (mProgressBarAnimator.isRunning())
            mProgressBarAnimator.end();

        ok_Btn.setEnabled(true);
        ok_Btn.findViewById(R.id.measure_ldi_img_ok).setEnabled(true);
        reset_Btn.setEnabled(true);

        countDownTimer.cancel();

        long date = mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).
                getLong(MainActivity.Preferences_LDI_STANDARD_DATE, 0);

        if (BuildConfig.DEBUG) Log.d(TAG, "standard date: "+date);

        if (date == 0) {
            standardLdiValue = ldiValue;

            // standard ldi value 가 없다면 첫 데이터를 기준으로
            mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).
                    edit().putLong(MainActivity.Preferences_LDI_STANDARD_DATE, standardLdiValue.getMeasureTime()).commit();
            sendLDIStandardDate(false);
            setResultView(RESULT_FIRST);
            return;
        } else {
            standardLdiValue = DatabaseManager.getInstance().selectLDIHistory(
                    mActivity.getUserData().getMasterKey(), date);
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "base ldi: "+ standardLdiValue.getLdiValue());

        if (standardLdiValue.getLdiValue() < ldiValue.getLdiValue()){
            setResultView(RESULT_INCREASE);
        } else if (standardLdiValue.getLdiValue() == ldiValue.getLdiValue()) {
            setResultView(RESULT_SAME);
        }
        else {
            setResultView(RESULT_DECREASE);
        }
    }

    void checkLDIValue(LDIValue value) {
        if (BuildConfig.DEBUG) Log.d(TAG, "ldi value: " + value.getLdiValue());
        // test 100 count
        /*++count;
        WriteTextInFile(Integer.toString(value.getLdiValue()));
        if (count >= 100) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    testHandler.sendMessage(testHandler.obtainMessage());
                }
            }).start();
            return;
        }
        mActivity.getMeasureLDIFromBelt();*/

        ldiValue = value;
        rawLDIValue = value.getLdiValue();

		if (BuildConfig.DEBUG) Log.d(TAG, "LDI raw data: " + ldiValue.getRawLDIValue());

        if (isLDIView) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) mView.findViewById(R.id.ldi_test)).setText(Integer.toString(rawLDIValue));
                }
            });
        }

        // 정상 코드
        if (value.getLdiValue() > LDI_BASEMENT) {
            if (BuildConfig.DEBUG) Log.d(TAG, "is animation ended: " + mAnimationHasEnded);
            if (!mAnimationHasEnded) {
                mActivity.getMeasureLDIFromBelt();
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
						mActivity.mode = mActivity.NONE;
						startHandler.sendMessage(startHandler.obtainMessage());
                    }
                }).start();
            }
            return;
        }

        // bmi test log
		ldiValue.setHeight(Integer.parseInt(height_EditText.getText().toString()));
		ldiValue.setWeight(Integer.parseInt(weight_EditText.getText().toString()));
        float bmi = (float) ((float) ldiValue.getWeight() / Math.pow(ldiValue.getHeight() / 100.0f, 2));
        if (BuildConfig.DEBUG) Log.d(TAG, "weight(float): " + (float) ldiValue.getWeight());
        if (BuildConfig.DEBUG) Log.d(TAG, "height(float): " + (float) ldiValue.getHeight());
        if (BuildConfig.DEBUG) Log.d(TAG, "Math.pow(height / 100.0f, 2): " + Math.pow(ldiValue.getHeight() / 100.0f, 2));
        if (BuildConfig.DEBUG) Log.d(TAG, "bmi(float): " + bmi);
        if (BuildConfig.DEBUG) Log.d(TAG, "bmi(integer): " + (int) bmi);
        if (BuildConfig.DEBUG) Log.d(TAG, "bmi * 0.93: " + (bmi * 0.93f));
        if (BuildConfig.DEBUG) Log.d(TAG, "ldi value * 0.07: " + ((float)ldiValue.getLdiValue() * 0.07f));
        if (BuildConfig.DEBUG) Log.d(TAG, "bmi * 0.93 + ldi value * 0.07: " + (bmi * 4.0f * 0.93f + (float)ldiValue.getLdiValue() * 0.07f));
        if (BuildConfig.DEBUG) Log.d(TAG, "bmi * 0.93 + ldi value * 0.07(Math.round): " + Math.round(bmi * 4.0f * 0.93f + (float)ldiValue.getLdiValue() * 0.07f));

        ldiValue.setLdiValue((int)(bmi * 4.0f * 0.93f + (float)ldiValue.getLdiValue() * 0.07f));
		ldiValue.setGender(mActivity.getUserData().getSex());
		ldiValue.setAge(Integer.parseInt(age_EditText.getText().toString()));

		// ldi value send to server
        sendLDIValue();
    }

    void sendLDIValue() {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", "in_ldi");
            json.put("userid", mActivity.getUserData().getMasterKey());
            json.put("kind", mActivity.getUserData().getCountry());
			json.put("lumi_device", ldiValue.getDeviceId());

            json.put("type", ldiValue.getType());
            json.put("ledmode", ldiValue.getMode());
            json.put("ldi", ldiValue.getLdiValue());
            json.put("ldi_row", ldiValue.getRawLDIValue());
            json.put("use_s_time", ldiValue.getMeasureTime());
			json.put("sex", ldiValue.getGender());
			json.put("age", ldiValue.getAge());
			json.put("height", ldiValue.getHeight());
			json.put("weight", ldiValue.getWeight());

            new JSONNetworkManager(JSONNetworkManager.LDI, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    ((MainActivity) getActivity()).disconnectBelt(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener)
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    setLDIHistoryVersion(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setLDIHistoryVersion(JSONObject json) {
        try {
            if (json.getInt("result") == 0) {
                if (BuildConfig.DEBUG) Log.d(TAG, "sendPairingData failed");
            }
            else {
                //insert database
                DatabaseManager.getInstance().insertLDIHistory(ldiValue);

                mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE)
                        .edit().putLong(MainActivity.Preferences_LDI_HISTORY_VERSION, json.getLong("version")).commit();

				new Thread(new Runnable() {
					@Override
					public void run() {
						endHandler.sendMessage(endHandler.obtainMessage());
					}
				}).start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void sendLDIStandardDate(final boolean isReset) {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", "up");
            json.put("masterkey", mActivity.getUserData().getMasterKey());
            json.put("ldi_center", standardLdiValue.getMeasureTime());
			json.put("sex", mActivity.getUserData().getSex());
			json.put("age", mActivity.getUserData().getAge());
			json.put("weight", mActivity.getUserData().getWeight());
			json.put("height", mActivity.getUserData().getHeight());

            new JSONNetworkManager(JSONNetworkManager.MEMBER, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    ((MainActivity) getActivity()).disconnectBelt(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener)
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    /*try {
                        long standardDate = responseJson.getLong("ldi_center");
                        if (BuildConfig.DEBUG) Log.d(TAG, "standard ldi date: " + standardDate);
                        mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).
                                edit().putLong(MainActivity.Preferences_LDI_STANDARD_DATE, standardDate).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                    if (isReset) {
                        //setResultView(RESULT_SAME);
						((MainLDIUsageStepsRecordFragment) mActivity.mainLDIUsageStepsRecordFragment).setFragment();
						mActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                    }
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // test 100 count
    /*void WriteTextInFile (String data) {
        writeData = data;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    fos.write(writeData.getBytes());
                    fos.write('\n');
                    fos.flush();
                    if (count >= 100)
                        fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }*/
}
