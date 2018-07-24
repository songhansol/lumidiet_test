package com.doubleh.lumidiet;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.doubleh.lumidiet.service.NotificationService;
import com.doubleh.lumidiet.utils.LumiTimePicker;
import com.doubleh.lumidiet.utils.OnSingleClickListener;

import java.lang.reflect.Field;

import static android.content.Context.MODE_PRIVATE;
import static com.doubleh.lumidiet.service.NotificationService.KEY_FRI;
import static com.doubleh.lumidiet.service.NotificationService.KEY_HOUR;
import static com.doubleh.lumidiet.service.NotificationService.KEY_MINUTE;
import static com.doubleh.lumidiet.service.NotificationService.KEY_MON;
import static com.doubleh.lumidiet.service.NotificationService.KEY_SAT;
import static com.doubleh.lumidiet.service.NotificationService.KEY_SET_NOTI;
import static com.doubleh.lumidiet.service.NotificationService.KEY_SUN;
import static com.doubleh.lumidiet.service.NotificationService.KEY_THR;
import static com.doubleh.lumidiet.service.NotificationService.KEY_TUE;
import static com.doubleh.lumidiet.service.NotificationService.KEY_WED;
import static com.doubleh.lumidiet.service.NotificationService.NAME_NOTIFICATION;
import static com.doubleh.lumidiet.service.NotificationService.notiData;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmFragment extends Fragment {
    String TAG = "AlarmFragment";

    View mView;
    ImageButton alarmSetting_Btn;
    TextView time_TextView, meridiem_TextView;
    Button[] daysBtns;

    private PopupWindow popup;
    private RelativeLayout popup_back_Layer;
    private LumiTimePicker timePicker;

    enum PopupState {
		NONE, PICKER
	}

	PopupState state = PopupState.NONE;

    int hour, minute;

    int[] daysId = {
            R.id.sunday, R.id.monday, R.id.tuesday, R.id.wednesday, R.id.thursday, R.id.friday, R.id.saturday
    };
    String[] daysKey = {
            KEY_SUN, KEY_MON, KEY_TUE, KEY_WED, KEY_THR, KEY_FRI, KEY_SAT
    };
    boolean[] isDaysChk = {
            false, false, false, false, false, false, false
    };
    boolean isOnAlarm;

    public AlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_alarm, container, false);

        //add code
        popup_back_Layer = (RelativeLayout) mView.findViewById(R.id.popup_layer);

        ImageButton prev_Btn = (ImageButton) mView.findViewById(R.id.alarm_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                //mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                mainActivity.setPrevContentFragmentLayout();
                mainActivity.setAlarmButton();
            }
        });

        alarmSetting_Btn = (ImageButton) mView.findViewById(R.id.alarm_btn_alarm_setting);
        isOnAlarm = false;

        meridiem_TextView = (TextView) mView.findViewById(R.id.meridiem);

        time_TextView = (TextView) mView.findViewById(R.id.time_textview);
        time_TextView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showTimePicker();
            }
        });

        daysBtns = new Button[7];
        for (int i = 0; i < daysId.length; i++) {
            daysBtns[i] = (Button) mView.findViewById(daysId[i]);
            daysBtns[i].setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    setDaysButtonClick(v);
                }
            });
        }

        init();

        return mView;
    }

    private void init() {
        for (int i = 0; i < 7; i++) {
            isDaysChk[i] = getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(daysKey[i], false);
            setDaysButton(i);
        }

        hour = getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getInt(KEY_HOUR, -1);
        minute = getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getInt(KEY_MINUTE, -1);

        if (notiData == null) {
            notiData = new NotificationService.NotiData();

            notiData.setDays(isDaysChk);
            notiData.setHour(hour);
            notiData.setMinute(minute);
        }

        if (hour != -1 && minute != -1) {
            if (hour >= 12) {
                meridiem_TextView.setText(getString(R.string.pm));
            } else {
                meridiem_TextView.setText(getString(R.string.am));
            }

            StringBuilder text = new StringBuilder();

            if (hour > 12)
                hour -= 12;
            else if (hour == 0)
                hour = 12;
            if (hour < 10) {
                text.append("0");
            }
            text.append(hour);
            text.append(":");
            if (minute < 10)
                text.append("0");
            text.append(minute);

            time_TextView.setText(text);
        }

        if (getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_SET_NOTI, false)) {
            isOnAlarm = true;
            alarmSetting_Btn.setActivated(true);
        }

        alarmSetting_Btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                isOnAlarm = !isOnAlarm;
                alarmSetting_Btn.setActivated(isOnAlarm);

                getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).edit().putBoolean(KEY_SET_NOTI, isOnAlarm).commit();

                if (isOnAlarm) {
                    if (hour == -1 && minute == -1) {
                        showTimePicker();
                    }
                    getActivity().startService(new Intent(getActivity().getApplicationContext(), NotificationService.class));
                } else {
                    getActivity().stopService(new Intent(getActivity().getApplicationContext(), NotificationService.class));
                }
            }
        });
    }

    private void showTimePicker() {
		state = PopupState.PICKER;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_timepicker, null);
        popup_back_Layer.setVisibility(View.VISIBLE);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        timePicker = (LumiTimePicker) layout.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(false);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((MainActivity) getActivity()).dispatchTouchEvent(event);
				return false;
			}
		});

        RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_timepicker_btn_ok);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                popup = null;
				state = PopupState.NONE;

                popup_back_Layer.setVisibility(View.INVISIBLE);

                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    hour = timePicker.getHour();
                    minute = timePicker.getMinute();
                } else {
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                }

                notiData.setHour(hour);
                notiData.setMinute(minute);
                getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).edit().putInt(KEY_HOUR, hour).commit();
                getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).edit().putInt(KEY_MINUTE, minute).commit();

                if (hour >= 12) {
                    meridiem_TextView.setText(getString(R.string.pm));
                } else {
                    meridiem_TextView.setText(getString(R.string.am));
                }

                StringBuilder text = new StringBuilder();

                if (hour > 12)
                    hour -= 12;
                else if (hour == 0)
                    hour = 12;
                if (hour < 10) {
                    text.append("0");
                }
                text.append(hour);
                text.append(":");
                if (minute < 10)
                    text.append("0");
                text.append(minute);

                time_TextView.setText(text);
            }
        });
    }

    private void setDaysButtonClick(View v) {
        int idx = -1;
        switch (v.getId()) {
            case R.id.sunday:
                idx = 0;
                break;
            case R.id.monday:
                idx = 1;
                break;
            case R.id.tuesday:
                idx = 2;
                break;
            case R.id.wednesday:
                idx = 3;
                break;
            case R.id.thursday:
                idx = 4;
                break;
            case R.id.friday:
                idx = 5;
                break;
            case R.id.saturday:
                idx = 6;
                break;
        }
        if (idx < 0) {
            if (BuildConfig.DEBUG) Log.e(TAG, "error");
            return;
        }
        isDaysChk[idx] = !isDaysChk[idx];
        getActivity().getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).edit().putBoolean(daysKey[idx], isDaysChk[idx]).commit();
        notiData.setDays(isDaysChk);
        setDaysButton(idx);
    }

    private void setDaysButton(int idx) {
        if (isDaysChk[idx]) {
            daysBtns[idx].setActivated(true);
            daysBtns[idx].setTextColor(ContextCompat.getColor(getActivity(), R.color.colorFFFFFFFF));
        } else {
            daysBtns[idx].setActivated(false);
            daysBtns[idx].setTextColor(ContextCompat.getColor(getActivity(), R.color.colorFF2F2F2F));
        }
    }

	public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case PICKER:
				showTimePicker();
				break;
		}
	}

	public void hidePopup() {
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
	}

	public View getMagView() {
		if (popup != null) {
			return popup.getContentView();
		} else {
			return getView();
		}
	}
}