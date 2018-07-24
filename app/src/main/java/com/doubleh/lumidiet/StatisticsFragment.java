package com.doubleh.lumidiet;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.common.Constants;
import com.doubleh.lumidiet.data.BeltHistory;
import com.doubleh.lumidiet.data.LDIValue;
import com.doubleh.lumidiet.data.WalkLog;
import com.doubleh.lumidiet.pedometer.Database;
import com.doubleh.lumidiet.utils.CrownButton;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.MyCalendarUtil;
import com.doubleh.lumidiet.utils.NumberFormatUtil;
import com.doubleh.lumidiet.utils.OnSingleClickListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment implements View.OnClickListener {
    String TAG = "StatisticsFragment";
    View mView;
    Button days_Btn, week_Btn, month_Btn, prev_Btn, prevGraph_btn, nextGraph_btn;
    RelativeLayout days_Layout, week_Layout, month_Layout;
    TextView range_Txt;
    int focus = 0;  // 0 is days, 1 is week, 2 is month
    float mDensity;
    Calendar calendar;
    DatabaseManager mDatabaseManager;

    final int daysLength = 7;
    final int weeksLength = 6;
    final int monthsLength = 12;

    final int standardUsingDaysInWeek = 4;
    final int standardUsingDaysInMonth = 15;

    final long aDayInMillis = 24 * 60 * 60 * 1000;
    //final long aWeekInMillis = 7 * 24 * 60 * 60 * 1000;
    PopupWindow popup = null;
    RelativeLayout popupLayer;
    private Gson gson;
    private ArrayList<WalkLog> weekWalkLogs;
    private ArrayList<WalkLog> dayWalkLogs;
    private String popupPedometerPeriod;
    private int goalSteps;
    private int weekGoalMax;

    enum PopupState {
		NONE, CROWN, PEDOMETER
	}
	PopupState state = PopupState.NONE;

    int[] daysTop = {
            R.id.statistics_days_txt_graph_days_1_top,
            R.id.statistics_days_txt_graph_days_2_top,
            R.id.statistics_days_txt_graph_days_3_top,
            R.id.statistics_days_txt_graph_days_4_top,
            R.id.statistics_days_txt_graph_days_5_top,
            R.id.statistics_days_txt_graph_days_6_top,
            R.id.statistics_days_txt_graph_days_7_top
    };
    int[] daysBottom = {
            R.id.statistics_days_txt_graph_days_1_bottom,
            R.id.statistics_days_txt_graph_days_2_bottom,
            R.id.statistics_days_txt_graph_days_3_bottom,
            R.id.statistics_days_txt_graph_days_4_bottom,
            R.id.statistics_days_txt_graph_days_5_bottom,
            R.id.statistics_days_txt_graph_days_6_bottom,
            R.id.statistics_days_txt_graph_days_7_bottom
    };

    int[] daysPedometer = {
            R.id.statistics_days_txt_graph_days_1_pedometer,
            R.id.statistics_days_txt_graph_days_2_pedometer,
            R.id.statistics_days_txt_graph_days_3_pedometer,
            R.id.statistics_days_txt_graph_days_4_pedometer,
            R.id.statistics_days_txt_graph_days_5_pedometer,
            R.id.statistics_days_txt_graph_days_6_pedometer,
            R.id.statistics_days_txt_graph_days_7_pedometer
    };

    int[] daysGraphLayoutsPedometer = {
            R.id.statistics_days_rlayout_graph_1_pedometer,
            R.id.statistics_days_rlayout_graph_2_pedometer,
            R.id.statistics_days_rlayout_graph_3_pedometer,
            R.id.statistics_days_rlayout_graph_4_pedometer,
            R.id.statistics_days_rlayout_graph_5_pedometer,
            R.id.statistics_days_rlayout_graph_6_pedometer,
            R.id.statistics_days_rlayout_graph_7_pedometer,
    };

    int[] weeksTop = {
            R.id.statistics_week_txt_graph_week_1_top,
            R.id.statistics_week_txt_graph_week_2_top,
            R.id.statistics_week_txt_graph_week_3_top,
            R.id.statistics_week_txt_graph_week_4_top,
            R.id.statistics_week_txt_graph_week_5_top,
            R.id.statistics_week_txt_graph_week_6_top
    };
    int[] weeksBottom = {
            R.id.statistics_week_txt_graph_week_1_bottom,
            R.id.statistics_week_txt_graph_week_2_bottom,
            R.id.statistics_week_txt_graph_week_3_bottom,
            R.id.statistics_week_txt_graph_week_4_bottom,
            R.id.statistics_week_txt_graph_week_5_bottom,
            R.id.statistics_week_txt_graph_week_6_bottom
    };

    int[] weeksPedometer = {
            R.id.statistics_week_txt_graph_week_1_pedometer,
            R.id.statistics_week_txt_graph_week_2_pedometer,
            R.id.statistics_week_txt_graph_week_3_pedometer,
            R.id.statistics_week_txt_graph_week_4_pedometer,
            R.id.statistics_week_txt_graph_week_5_pedometer,
            R.id.statistics_week_txt_graph_week_6_pedometer
    };

    int[] weeksGraphLayoutsPedometer = {
            R.id.statistics_week_rlayout_graph_1_pedometer,
            R.id.statistics_week_rlayout_graph_2_pedometer,
            R.id.statistics_week_rlayout_graph_3_pedometer,
            R.id.statistics_week_rlayout_graph_4_pedometer,
            R.id.statistics_week_rlayout_graph_5_pedometer,
            R.id.statistics_week_rlayout_graph_6_pedometer,
    };


    int[] graphBarsTop = {
            R.id.statistics_img_graph_bar_1_top,
            R.id.statistics_img_graph_bar_2_top,
            R.id.statistics_img_graph_bar_3_top,
            R.id.statistics_img_graph_bar_4_top,
            R.id.statistics_img_graph_bar_5_top,
            R.id.statistics_img_graph_bar_6_top,
            R.id.statistics_img_graph_bar_7_top,
            R.id.statistics_img_graph_bar_8_top,
            R.id.statistics_img_graph_bar_9_top,
            R.id.statistics_img_graph_bar_10_top,
            R.id.statistics_img_graph_bar_11_top,
            R.id.statistics_img_graph_bar_12_top
    };
    int[] graphDotsTop = {
            R.id.statistics_img_graph_dot_1_top,
            R.id.statistics_img_graph_dot_2_top,
            R.id.statistics_img_graph_dot_3_top,
            R.id.statistics_img_graph_dot_4_top,
            R.id.statistics_img_graph_dot_5_top,
            R.id.statistics_img_graph_dot_6_top,
            R.id.statistics_img_graph_dot_7_top,
            R.id.statistics_img_graph_dot_8_top,
            R.id.statistics_img_graph_dot_9_top,
            R.id.statistics_img_graph_dot_10_top,
            R.id.statistics_img_graph_dot_11_top,
            R.id.statistics_img_graph_dot_12_top
    };
    int[] graphBarsBot = {
            R.id.statistics_img_graph_bar_1_bottom,
            R.id.statistics_img_graph_bar_2_bottom,
            R.id.statistics_img_graph_bar_3_bottom,
            R.id.statistics_img_graph_bar_4_bottom,
            R.id.statistics_img_graph_bar_5_bottom,
            R.id.statistics_img_graph_bar_6_bottom,
            R.id.statistics_img_graph_bar_7_bottom,
            R.id.statistics_img_graph_bar_8_bottom,
            R.id.statistics_img_graph_bar_9_bottom,
            R.id.statistics_img_graph_bar_10_bottom,
            R.id.statistics_img_graph_bar_11_bottom,
            R.id.statistics_img_graph_bar_12_bottom
    };
    int[] graphDotsBot = {
            R.id.statistics_img_graph_dot_1_bottom,
            R.id.statistics_img_graph_dot_2_bottom,
            R.id.statistics_img_graph_dot_3_bottom,
            R.id.statistics_img_graph_dot_4_bottom,
            R.id.statistics_img_graph_dot_5_bottom,
            R.id.statistics_img_graph_dot_6_bottom,
            R.id.statistics_img_graph_dot_7_bottom,
            R.id.statistics_img_graph_dot_8_bottom,
            R.id.statistics_img_graph_dot_9_bottom,
            R.id.statistics_img_graph_dot_10_bottom,
            R.id.statistics_img_graph_dot_11_bottom,
            R.id.statistics_img_graph_dot_12_bottom
    };

    int[] monthGraphLayoutsPedometer = {
            R.id.statistics_month_rlayout_graph_1_pedometer,
            R.id.statistics_month_rlayout_graph_2_pedometer,
            R.id.statistics_month_rlayout_graph_3_pedometer,
            R.id.statistics_month_rlayout_graph_4_pedometer,
            R.id.statistics_month_rlayout_graph_5_pedometer,
            R.id.statistics_month_rlayout_graph_6_pedometer,
            R.id.statistics_month_rlayout_graph_7_pedometer,
            R.id.statistics_month_rlayout_graph_8_pedometer,
            R.id.statistics_month_rlayout_graph_9_pedometer,
            R.id.statistics_month_rlayout_graph_10_pedometer,
            R.id.statistics_month_rlayout_graph_11_pedometer,
            R.id.statistics_month_rlayout_graph_12_pedometer
    };

    int[] graphBarsPedometer = {
            R.id.statistics_img_graph_bar_1_pedometer,
            R.id.statistics_img_graph_bar_2_pedometer,
            R.id.statistics_img_graph_bar_3_pedometer,
            R.id.statistics_img_graph_bar_4_pedometer,
            R.id.statistics_img_graph_bar_5_pedometer,
            R.id.statistics_img_graph_bar_6_pedometer,
            R.id.statistics_img_graph_bar_7_pedometer,
            R.id.statistics_img_graph_bar_8_pedometer,
            R.id.statistics_img_graph_bar_9_pedometer,
            R.id.statistics_img_graph_bar_10_pedometer,
            R.id.statistics_img_graph_bar_11_pedometer,
            R.id.statistics_img_graph_bar_12_pedometer
    };
    int[] graphDotsPedometer = {
            R.id.statistics_img_graph_dot_1_pedometer,
            R.id.statistics_img_graph_dot_2_pedometer,
            R.id.statistics_img_graph_dot_3_pedometer,
            R.id.statistics_img_graph_dot_4_pedometer,
            R.id.statistics_img_graph_dot_5_pedometer,
            R.id.statistics_img_graph_dot_6_pedometer,
            R.id.statistics_img_graph_dot_7_pedometer,
            R.id.statistics_img_graph_dot_8_pedometer,
            R.id.statistics_img_graph_dot_9_pedometer,
            R.id.statistics_img_graph_dot_10_pedometer,
            R.id.statistics_img_graph_dot_11_pedometer,
            R.id.statistics_img_graph_dot_12_pedometer
    };

    int[] monthPedometer = {
            R.id.statistics_month_txt_graph_month_1_pedometer,
            R.id.statistics_month_txt_graph_month_2_pedometer,
            R.id.statistics_month_txt_graph_month_3_pedometer,
            R.id.statistics_month_txt_graph_month_4_pedometer,
            R.id.statistics_month_txt_graph_month_5_pedometer,
            R.id.statistics_month_txt_graph_month_6_pedometer,
            R.id.statistics_month_txt_graph_month_7_pedometer,
            R.id.statistics_month_txt_graph_month_8_pedometer,
            R.id.statistics_month_txt_graph_month_9_pedometer,
            R.id.statistics_month_txt_graph_month_10_pedometer,
            R.id.statistics_month_txt_graph_month_11_pedometer,
            R.id.statistics_month_txt_graph_month_12_pedometer,
    };


    int[] usageTime = {
            R.id.statistics_txt_graph_usage_time_1_top,
            R.id.statistics_txt_graph_usage_time_2_top,
            R.id.statistics_txt_graph_usage_time_3_top,
            R.id.statistics_txt_graph_usage_time_4_top,
            R.id.statistics_txt_graph_usage_time_5_top,
            R.id.statistics_txt_graph_usage_time_6_top,
            R.id.statistics_txt_graph_usage_time_7_top,
            R.id.statistics_txt_graph_usage_time_8_top,
            R.id.statistics_txt_graph_usage_time_9_top,
            R.id.statistics_txt_graph_usage_time_10_top,
            R.id.statistics_txt_graph_usage_time_11_top,
            R.id.statistics_txt_graph_usage_time_12_top
    };
    int[] measureLDI = {
            R.id.statistics_txt_graph_usage_time_1_bottom,
            R.id.statistics_txt_graph_usage_time_2_bottom,
            R.id.statistics_txt_graph_usage_time_3_bottom,
            R.id.statistics_txt_graph_usage_time_4_bottom,
            R.id.statistics_txt_graph_usage_time_5_bottom,
            R.id.statistics_txt_graph_usage_time_6_bottom,
            R.id.statistics_txt_graph_usage_time_7_bottom,
            R.id.statistics_txt_graph_usage_time_8_bottom,
            R.id.statistics_txt_graph_usage_time_9_bottom,
            R.id.statistics_txt_graph_usage_time_10_bottom,
            R.id.statistics_txt_graph_usage_time_11_bottom,
            R.id.statistics_txt_graph_usage_time_12_bottom
    };

    int[] measurePedometer = {
            R.id.statistics_txt_graph_usage_time_1_pedometer,
            R.id.statistics_txt_graph_usage_time_2_pedometer,
            R.id.statistics_txt_graph_usage_time_3_pedometer,
            R.id.statistics_txt_graph_usage_time_4_pedometer,
            R.id.statistics_txt_graph_usage_time_5_pedometer,
            R.id.statistics_txt_graph_usage_time_6_pedometer,
            R.id.statistics_txt_graph_usage_time_7_pedometer,
            R.id.statistics_txt_graph_usage_time_8_pedometer,
            R.id.statistics_txt_graph_usage_time_9_pedometer,
            R.id.statistics_txt_graph_usage_time_10_pedometer,
            R.id.statistics_txt_graph_usage_time_11_pedometer,
            R.id.statistics_txt_graph_usage_time_12_pedometer
    };

    int[] crownTop = {
            R.id.statistics_btn_crown_1_top,
            R.id.statistics_btn_crown_2_top,
            R.id.statistics_btn_crown_3_top,
            R.id.statistics_btn_crown_4_top,
            R.id.statistics_btn_crown_5_top,
            R.id.statistics_btn_crown_6_top,
            R.id.statistics_btn_crown_7_top,
            R.id.statistics_btn_crown_8_top,
            R.id.statistics_btn_crown_9_top,
            R.id.statistics_btn_crown_10_top,
            R.id.statistics_btn_crown_11_top,
            R.id.statistics_btn_crown_12_top
    };

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_statistics, container, false);

        gson = new Gson();

        mDensity = ((MainActivity) getActivity()).getDensity();
        mDatabaseManager = DatabaseManager.getInstance();

        SharedPreferences prefs = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        goalSteps = prefs.getInt("goal", 5000);
        weekGoalMax = 70000;

        // add code
        calendar = Calendar.getInstance();
        prev_Btn = (Button) mView.findViewById(R.id.statistics_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
            }
        });
        range_Txt = (TextView) mView.findViewById(R.id.statistics_txt_graph_range);

        prevGraph_btn = (Button) mView.findViewById(R.id.statistics_btn_graph_prev);
        prevGraph_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (focus == 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, -7);
                    setDaysGraph();
                } else if (focus == 1) {
                    calendar.add(Calendar.MONTH, -1);
                    setWeekGraph();
                } else if (focus == 2) {
                    calendar.add(Calendar.YEAR, -1);
                    setMonthGraph();
                }
            }
        });

        nextGraph_btn = (Button) mView.findViewById(R.id.statistics_btn_graph_next);
        nextGraph_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (focus == 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
                    setDaysGraph();
                } else if (focus == 1) {
                    calendar.add(Calendar.MONTH, 1);
                    setWeekGraph();
                } else if (focus == 2) {
                    calendar.add(Calendar.YEAR, 1);
                    setMonthGraph();
                }
            }
        });

        days_Btn = (Button) mView.findViewById(R.id.statistics_btn_days);
        days_Btn.setOnClickListener(this);
        week_Btn = (Button) mView.findViewById(R.id.statistics_btn_week);
        week_Btn.setOnClickListener(this);
        month_Btn = (Button) mView.findViewById(R.id.statistics_btn_month);
        month_Btn.setOnClickListener(this);

        // Graph layout parameter
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)getResources().getDimension(R.dimen.activity_width)
                                                    , (int)getResources().getDimension(R.dimen.activity_height)+80);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(Gravity.CENTER_HORIZONTAL);
        // days
        days_Layout = (RelativeLayout) View.inflate(getActivity(), R.layout.layout_statistics_days, null);
        days_Layout.setGravity(Gravity.CENTER | Gravity.TOP);
        days_Layout.setVisibility(View.INVISIBLE);
        // week
        week_Layout = (RelativeLayout) View.inflate(getActivity(), R.layout.layout_statistics_week, null);
        week_Layout.setGravity(Gravity.CENTER | Gravity.TOP);
        week_Layout.setVisibility(View.INVISIBLE);
        // month
        month_Layout = (RelativeLayout) View.inflate(getActivity(), R.layout.layout_statistics_month, null);
        month_Layout.setGravity(Gravity.CENTER | Gravity.TOP);
        month_Layout.setVisibility(View.INVISIBLE);

        popupLayer = (RelativeLayout) mView.findViewById(R.id.popup_layer);

        for (int i = 0; i < monthsLength; i++) {
            month_Layout.findViewById(crownTop[i]).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    popupLayer.setVisibility(View.VISIBLE);
					showCrownPopup();
                }
            });
        }

        RelativeLayout rlGraphContainer = (RelativeLayout) mView.findViewById(R.id.rlGraphContainer);
        rlGraphContainer.addView(days_Layout, params);
        rlGraphContainer.addView(week_Layout, params);
        rlGraphContainer.addView(month_Layout, params);

        setDaysGraph();

        return mView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id)
        {
            case R.id.statistics_btn_days:
                if (focus == 0) {
                    return;
                }
                else if (focus == 1) {
                    week_Btn.setBackgroundResource(R.drawable.common_tap_center_dim);
                    week_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
                }
                else {
                    month_Btn.setBackgroundResource(R.drawable.common_tap_right_dim);
                    month_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
                }
                calendar = Calendar.getInstance();
                days_Btn.setBackgroundResource(R.drawable.common_tap_left);
                days_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFFFFFEFE));
                focus = 0;
                setDaysGraph();
                break;
            case R.id.statistics_btn_week:
                if (focus == 0) {
                    days_Btn.setBackgroundResource(R.drawable.common_tap_left_dim);
                    days_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
                }
                else if (focus == 1) {
                    return;
                }
                else {
                    month_Btn.setBackgroundResource(R.drawable.common_tap_right_dim);
                    month_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
                }
                calendar = Calendar.getInstance();
                week_Btn.setBackgroundResource(R.drawable.common_tap_center);
                week_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFFFFFEFE));
                focus = 1;
                setWeekGraph();
                break;
            case R.id.statistics_btn_month:
                if (focus == 0) {
                    days_Btn.setBackgroundResource(R.drawable.common_tap_left_dim);
                    days_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
                }
                else if (focus == 1) {
                    week_Btn.setBackgroundResource(R.drawable.common_tap_center_dim);
                    week_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
                }
                else {
                    return;
                }
                calendar = Calendar.getInstance();
                month_Btn.setBackgroundResource(R.drawable.common_tap_right);
                month_Btn.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFFFFFEFE));
                focus = 2;
                setMonthGraph();
                break;
        }
    }

    // days graph data setting
    public void setDaysGraph() {
        StringBuilder rangeText = new StringBuilder();
        rangeText.append(" ~ ");
        rangeText.append(calendar.get(Calendar.MONTH) + 1);
        rangeText.append("/");
        rangeText.append(calendar.get(Calendar.DATE));
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        rangeText.insert(0, calendar.get(Calendar.DATE));
        rangeText.insert(0, "/");
        rangeText.insert(0, calendar.get(Calendar.MONTH) + 1);

        range_Txt.setText(rangeText);
        for (int i = 0; i < daysTop.length; i++) {
            StringBuilder dateText = new StringBuilder();
            dateText.append(calendar.get(Calendar.MONTH) + 1);
            dateText.append("/");
            dateText.append(calendar.get(Calendar.DATE));

            ((TextView) days_Layout.findViewById(daysTop[i])).setText(dateText);
            ((TextView) days_Layout.findViewById(daysBottom[i])).setText(dateText);
            ((TextView) days_Layout.findViewById(daysPedometer[i])).setText(dateText);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        setGraphLayout();

        updateWalkLogsByDay(null);

        Date baseTime = calendar.getTime();
        requestDaysListWalkSummary(baseTime);

        // usage time setting
        for (int idx = 0; idx < daysLength; idx++) {
            long[] times = MyCalendarUtil.getDayInMillis(calendar, (daysLength - 1) - idx);
            ArrayList<BeltHistory> datas = mDatabaseManager.
                    selectBeltHistory(Integer.parseInt(((MainActivity)getActivity()).getUserData().getMasterKey()), times[0] / 1000, times[1] / 1000);

            long usingTime = 0;
            for (BeltHistory data : datas) {
                usingTime += (data.getUsingTime() * 1000);
            }

            usingTime = (long)(usingTime / (1000.0f * 60.0f));
            ((TextView) days_Layout.findViewById(usageTime[idx])).setText(Long.toString(usingTime));

            if (usingTime >= 15) {
                days_Layout.findViewById(graphDotsTop[idx]).setActivated(true);
                days_Layout.findViewById(graphBarsTop[idx]).setActivated(true);
            } else {
                days_Layout.findViewById(graphDotsTop[idx]).setActivated(false);
                days_Layout.findViewById(graphBarsTop[idx]).setActivated(false);
            }

            float graphValue = 10.0f / 3.0f * (float)usingTime;
            if (graphValue >= 100.0f) {
                graphValue = 100.0f;
//                days_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.VISIBLE);
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
                days_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.INVISIBLE);
            } else {
//                days_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.VISIBLE);
            }
            days_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.INVISIBLE);
            days_Layout.findViewById(graphBarsTop[idx]).getLayoutParams().height
                    = Math.round (graphValue * mDensity);

            // ldi value setting
            long standardDate = getActivity().getSharedPreferences(((MainActivity)getActivity()).getUserData().getMasterKey(), MODE_PRIVATE).
                    getLong(MainActivity.Preferences_LDI_STANDARD_DATE, 0);
            LDIValue standardLdiValue = mDatabaseManager.selectLDIHistory(
                    ((MainActivity)getActivity()).getUserData().getMasterKey(), standardDate);

            ArrayList<LDIValue> ldiValues = mDatabaseManager.
                    selectLDIHistory(((MainActivity)getActivity()).getUserData().getMasterKey(), (times[0] / 1000), (times[1] / 1000));

            float ldiValue = 0.0f;

			if (ldiValues.size() > 0) {
				ldiValue = ldiValues.get(ldiValues.size() - 1).getLdiValue();
			}

            /*for (LDIValue value : ldiValues) {
                ldiValue += value.getLdiValue();
            }*/

            //if (BuildConfig.DEBUG) Log.d(TAG, "ldiValue1: " + ldiValue);
            //if (ldiValues.size() != 0)
                //ldiValue = ldiValue / ldiValues.size();
            //if (BuildConfig.DEBUG) Log.d(TAG, "ldiValue2: " + ldiValue);
            ldiValue = ldiValue / (float)standardLdiValue.getLdiValue() * 100.0f;
            //if (BuildConfig.DEBUG) Log.d(TAG, "ldiValue3: " + ldiValue);

            if (ldiValue <= 100.0f) {
                days_Layout.findViewById(graphDotsBot[idx]).setActivated(true);
                days_Layout.findViewById(graphBarsBot[idx]).setActivated(true);
            } else {
                days_Layout.findViewById(graphDotsBot[idx]).setActivated(false);
                days_Layout.findViewById(graphBarsBot[idx]).setActivated(false);
            }

            ((TextView) days_Layout.findViewById(measureLDI[idx])).setText(Integer.toString((int)ldiValue));

            graphValue = (50.0f * ldiValue) / 100.0f;
            if (Float.isNaN(graphValue)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "no have standard LDI value");
                graphValue = 0.0f;
            }
            if (graphValue >= 100.0f) {
                graphValue = 100.0f;
//                days_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.VISIBLE);
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
                days_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.INVISIBLE);
            } else {
//                days_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.VISIBLE);
            }
            days_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.INVISIBLE);
            days_Layout.findViewById(graphBarsBot[idx]).getLayoutParams().height
                    = Math.round (graphValue * mDensity);

        }
    }

    private int getStepsBy(long[] times){
        long endTime;
        long startTime;
        int steps = 0;

        startTime = times[0]/1000;
        endTime = times[1]/1000;
        startTime = startTime *1000;
        endTime = endTime *1000;

        java.text.DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        try {
            Database db = Database.getInstance(getActivity());

                steps = db.getSteps(startTime);

                if(steps < 0) {
                    int today_offset = steps;
                    steps = db.getCurrentSteps();
                    if (steps > 0) {
                        if (today_offset == Integer.MIN_VALUE) today_offset = -steps;
                        steps = (today_offset + steps);
                    }
                }

                String startDay = dateFormat.format(startTime);
                String endDay = dateFormat.format(endTime);
                Log.e(TAG, "" + startDay + " ~ " + endDay + ", count:" + steps);
                if(steps < 0)
                    steps = 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return steps;

    }


    // week graph data setting
    public void setWeekGraph() {
        StringBuilder rangeText = new StringBuilder();
        rangeText.append(calendar.get(Calendar.YEAR));
        //rangeText.append(getString(R.string.year));
        rangeText.append(". ");
        rangeText.append(calendar.get(Calendar.MONTH) + 1);
        //rangeText.append(getString(R.string.month));

        range_Txt.setText(rangeText);

        ((TextView) week_Layout.findViewById(R.id.tvMiddle)).setText(String.format(Locale.US, "%.1f", 35.0f));

        setGraphLayout();

        updateWalkLogsByWeek(null);

        calendar.add(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE) - calendar.get(Calendar.DATE));

        Date baseTime = calendar.getTime();
        requestWeeksListWalkSummary(baseTime);

        for (int idx = weeksLength - 1; idx >= 0; idx--) {
            long[] times = MyCalendarUtil.getWeekOfFirstDayInMillis(calendar, (weeksLength - 1) - idx);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(times[0]);
            StringBuilder weekText = new StringBuilder();
            weekText.append(c.get(Calendar.MONTH ) + 1);
            weekText.append("/");
            weekText.append(c.get(Calendar.DAY_OF_MONTH));
            weekText.append("\n~\n");
            c.setTimeInMillis(times[1] - 1000);
            weekText.append(c.get(Calendar.MONTH ) + 1);
            weekText.append("/");
            weekText.append(c.get(Calendar.DAY_OF_MONTH));

            ((TextView) week_Layout.findViewById(weeksTop[idx])).setText(weekText);
            ((TextView) week_Layout.findViewById(weeksBottom[idx])).setText(weekText);
            ((TextView) week_Layout.findViewById(weeksPedometer[idx])).setText(weekText);

            // set using days
            int usingDays = 0;

            for (long time = times[0]; time < times[1]; time += aDayInMillis) {
                if (mDatabaseManager.selectCountValidBeltHistory(Integer.parseInt(((MainActivity)getActivity()).getUserData().getMasterKey()), time / 1000, (time + aDayInMillis) / 1000) > 0)
                    usingDays++;
            }

            /*ArrayList<BeltHistory> datas = mDatabaseManager.selectBeltHistory(Integer.parseInt(((MainActivity)getActivity()).getUserData().getMasterKey()), times[0], times[1]);

            for (long time = times[0]; time < times[1]; time += aDayInMillis) {
                for (BeltHistory data : datas) {
                    if (data.getStartTime() >= time && data.getStartTime() < time + aDayInMillis) {
                        usingDays++;
                        break;
                    }
                }
            }*/

            ((TextView) week_Layout.findViewById(usageTime[idx])).setText(Long.toString(usingDays));

            if (usingDays >= standardUsingDaysInWeek) {
                week_Layout.findViewById(graphDotsTop[idx]).setActivated(true);
                week_Layout.findViewById(graphBarsTop[idx]).setActivated(true);
            } else {
                week_Layout.findViewById(graphDotsTop[idx]).setActivated(false);
                week_Layout.findViewById(graphBarsTop[idx]).setActivated(false);
            }

            float graphValue = 100.0f / 7.0f * (float)usingDays;
            if (usingDays <= standardUsingDaysInWeek) {
                graphValue = 100.0f / 8.0f * (float)usingDays;
            }

            if (graphValue >= 100.0f) {
                graphValue = 100.0f;
//                week_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.VISIBLE);
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
                week_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.INVISIBLE);
            } else {
//                week_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.VISIBLE);
            }
            /*if (usingDays == standardUsingDaysInWeek) {
                graphValue = 50.0f;
            }*/
            week_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.INVISIBLE);
            week_Layout.findViewById(graphBarsTop[idx]).getLayoutParams().height
                    = Math.round (graphValue * mDensity);

            // ldi value setting
            long standardDate = getActivity().getSharedPreferences(((MainActivity)getActivity()).getUserData().getMasterKey(), MODE_PRIVATE).
                    getLong(MainActivity.Preferences_LDI_STANDARD_DATE, 0);
            LDIValue standardLdiValue = mDatabaseManager.selectLDIHistory(
                    ((MainActivity)getActivity()).getUserData().getMasterKey(), standardDate);

            // 임시로 가장 최근 데이터를 standard 로 설정, not used
            /*if (standardLdiValue.getLdiValue() == 0) {
                ArrayList<LDIValue> values = mDatabaseManager.selectLDIHistory(((MainActivity)getActivity()).getUserData().getMasterKey());
                if (values.size() > 0) {
                    standardLdiValue = values.get(0);
                }
            }*/

            ArrayList<LDIValue> ldiValues = mDatabaseManager.
                    selectLDIHistory(((MainActivity)getActivity()).getUserData().getMasterKey(), times[0] / 1000, times[1] / 1000);

            float ldiValue = 0.0f;

            for (LDIValue value : ldiValues) {
                ldiValue += value.getLdiValue();
            }
            if (ldiValues.size() != 0)
                ldiValue = ldiValue / ldiValues.size();

            ldiValue = ldiValue / (float)standardLdiValue.getLdiValue() * 100.0f;

            if (ldiValue <= 100.0f) {
                week_Layout.findViewById(graphDotsBot[idx]).setActivated(true);
                week_Layout.findViewById(graphBarsBot[idx]).setActivated(true);
            } else {
                week_Layout.findViewById(graphDotsBot[idx]).setActivated(false);
                week_Layout.findViewById(graphBarsBot[idx]).setActivated(false);
            }

            ((TextView) week_Layout.findViewById(measureLDI[idx])).setText(Integer.toString((int) ldiValue));

            graphValue = (50.0f * ldiValue) / 100.0f;
            if (Float.isNaN(graphValue)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "no have standard LDI value");
                graphValue = 0.0f;
            }
            if (graphValue >= 100.0f) {
                graphValue = 100.0f;
//                week_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.VISIBLE);
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
                week_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.INVISIBLE);
            } else {
//                week_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.VISIBLE);
            }
            week_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.INVISIBLE);
            week_Layout.findViewById(graphBarsBot[idx]).getLayoutParams().height
                    = Math.round (graphValue * mDensity);
        }
    }

    // month graph data setting
    public void setMonthGraph() {
        StringBuilder rangeText = new StringBuilder();
        rangeText.append(calendar.get(Calendar.YEAR));
        //rangeText.append(getString(R.string.year));

        range_Txt.setText(rangeText);

        ((TextView) month_Layout.findViewById(R.id.tvMiddle)).setText(String.format(Locale.US, "%.1f", 150.0f));

        setGraphLayout();

        updateWalkLogsByMonth(null);

        Date baseTime = calendar.getTime();
        requestMonthsListWalkSummary(baseTime);

        for (int idx = 0; idx < monthsLength; idx++) {
            calendar.add(Calendar.MONTH, 11 - calendar.get(Calendar.MONTH));

            long[] times = MyCalendarUtil.getMonthInMillis(calendar, (monthsLength - 1) - idx);

            // set using days
            int lastDay = calendar.getActualMaximum(Calendar.DATE);
            int usingDays = 0;

            for (long time = times[0]; time < times[1]; time += aDayInMillis) {
                if (mDatabaseManager.selectCountValidBeltHistory(Integer.parseInt(((MainActivity)getActivity()).getUserData().getMasterKey()), time / 1000, (time + aDayInMillis) / 1000) > 0)
                    usingDays++;
            }

            ((TextView) month_Layout.findViewById(usageTime[idx])).setText(Long.toString(usingDays));

            if (usingDays >= standardUsingDaysInMonth) {
                month_Layout.findViewById(graphDotsTop[idx]).setActivated(true);
                month_Layout.findViewById(graphBarsTop[idx]).setActivated(true);
            } else {
                month_Layout.findViewById(graphDotsTop[idx]).setActivated(false);
                month_Layout.findViewById(graphBarsTop[idx]).setActivated(false);
            }

            float graphValue = 100.0f / (float)lastDay * (float)usingDays;
            if (usingDays <= standardUsingDaysInMonth) {
                graphValue = 100.0f / (standardUsingDaysInMonth * 2.0f) * (float)usingDays;
            }

            month_Layout.findViewById(crownTop[idx]).setVisibility(View.VISIBLE);
            if (usingDays >= 25) {
                ((CrownButton) month_Layout.findViewById(crownTop[idx])).setCrownState(CrownButton.EXCELLENT);
            } else if (usingDays >= 20) {
                ((CrownButton) month_Layout.findViewById(crownTop[idx])).setCrownState(CrownButton.GREAT);
            } else if (usingDays >= 15) {
                ((CrownButton) month_Layout.findViewById(crownTop[idx])).setCrownState(CrownButton.GOOD);
            } else {
                month_Layout.findViewById(crownTop[idx]).setVisibility(View.INVISIBLE);
            }

            if (graphValue >= 100.0f) {
                graphValue = 100.0f;
//                month_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.VISIBLE);
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
                month_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.INVISIBLE);
            } else {
//                month_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.VISIBLE);
            }
            month_Layout.findViewById(graphDotsTop[idx]).setVisibility(View.INVISIBLE);
            month_Layout.findViewById(graphBarsTop[idx]).getLayoutParams().height
                    = Math.round (graphValue * mDensity);

            // ldi value setting
            long standardDate = getActivity().getSharedPreferences(((MainActivity)getActivity()).getUserData().getMasterKey(), MODE_PRIVATE).
                    getLong(MainActivity.Preferences_LDI_STANDARD_DATE, 0);
            LDIValue standardLdiValue = mDatabaseManager.selectLDIHistory(
                    ((MainActivity)getActivity()).getUserData().getMasterKey(), standardDate);

            ArrayList<LDIValue> ldiValues = mDatabaseManager.selectLDIHistory(((MainActivity)getActivity()).getUserData().getMasterKey(), times[0] / 1000, times[1] / 1000);

            float ldiValue = 0.0f;

            for (LDIValue value : ldiValues) {
                ldiValue += value.getLdiValue();
            }
            if (ldiValues.size() != 0)
                ldiValue = ldiValue / ldiValues.size();

            ldiValue = ldiValue / (float)standardLdiValue.getLdiValue() * 100.0f;

            if (ldiValue <= 100.0f) {
                month_Layout.findViewById(graphDotsBot[idx]).setActivated(true);
                month_Layout.findViewById(graphBarsBot[idx]).setActivated(true);
            } else {
                month_Layout.findViewById(graphDotsBot[idx]).setActivated(false);
                month_Layout.findViewById(graphBarsBot[idx]).setActivated(false);
            }

            ((TextView) month_Layout.findViewById(measureLDI[idx])).setText(Integer.toString((int)ldiValue));

            graphValue = (50.0f * ldiValue) / 100.0f;
            if (Float.isNaN(graphValue)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "no have standard LDI value");
                graphValue = 0.0f;
            }
            if (graphValue >= 100.0f) {
                graphValue = 100.0f;
//                month_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.VISIBLE);
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
                month_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.INVISIBLE);
            } else {
//                month_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.VISIBLE);
            }
            month_Layout.findViewById(graphDotsBot[idx]).setVisibility(View.INVISIBLE);
            month_Layout.findViewById(graphBarsBot[idx]).getLayoutParams().height
                    = Math.round (graphValue * mDensity);

        }
    }

    public void setGraphLayout() {
        switch (focus)
        {
            case 0:
                days_Layout.setVisibility(View.VISIBLE);
                week_Layout.setVisibility(View.INVISIBLE);
                month_Layout.setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.statistics_img_top).setActivated(false);
                ((TextView) mView.findViewById(R.id.statistics_txt_top)).setText(getString(R.string.encourage_day));
                break;
            case 1:
                days_Layout.setVisibility(View.INVISIBLE);
                week_Layout.setVisibility(View.VISIBLE);
                month_Layout.setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.statistics_img_top).setActivated(true);
                ((TextView) mView.findViewById(R.id.statistics_txt_top)).setText(getString(R.string.encourage_week));
                break;
            case 2:
                days_Layout.setVisibility(View.INVISIBLE);
                week_Layout.setVisibility(View.INVISIBLE);
                month_Layout.setVisibility(View.VISIBLE);
                mView.findViewById(R.id.statistics_img_top).setActivated(true);
                ((TextView) mView.findViewById(R.id.statistics_txt_top)).setText(getString(R.string.encourage_month));
                break;
        }
    }

    void showCrownPopup() {
        state = PopupState.CROWN;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_crown_info, null);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getActivity().dispatchTouchEvent(event);
                return false;
            }
        });


        layout.findViewById(R.id.popup_btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                popupLayer.setVisibility(View.INVISIBLE);
                popup = null;
                state = PopupState.NONE;
            }
        });
    }

    int popupPedometerIdx;

    void showPedometerPopup() {
        state = PopupState.PEDOMETER;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_pedometer, null);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getActivity().dispatchTouchEvent(event);
                return false;
            }
        });


        layout.findViewById(R.id.popup_btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
                popupLayer.setVisibility(View.INVISIBLE);
                popup = null;
                state = PopupState.NONE;
            }
        });

        TextView tvSteps = layout.findViewById(R.id.tvSteps);
        TextView tvRanking = layout.findViewById(R.id.tvRanking);
        TextView tvPeriod = layout.findViewById(R.id.tvPeriod);

        ArrayList<WalkLog> selectedWalkLogs = null;
        switch (focus) {
            case 0:
                selectedWalkLogs = dayWalkLogs;
                break;
            case 1:
                selectedWalkLogs = weekWalkLogs;
                break;
            case 2:
                selectedWalkLogs = monthWalkLogs;
                break;
        }

        if( null != selectedWalkLogs && 0 <= popupPedometerIdx && popupPedometerIdx < selectedWalkLogs.size()) {
            tvPeriod.setText(popupPedometerPeriod);
            WalkLog walkLog = selectedWalkLogs.get(popupPedometerIdx);
            String sumScope = walkLog.getSumscope();
            if(!TextUtils.isEmpty(sumScope)) {
                tvSteps.setText(NumberFormatUtil.commaedNumber(Long.valueOf(sumScope)));
            } else {
                tvSteps.setText("0");
            }
            tvRanking.setText(""+walkLog.getRank());
        } else {
            tvPeriod.setText("-");
            tvSteps.setText("0");
            tvRanking.setText("0");
        }
    }

	public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case CROWN:
				showCrownPopup();
				break;
            case PEDOMETER:
                showPedometerPopup();
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

    private void requestDaysListWalkSummary(Date baseDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            JSONObject json = new JSONObject();
            json.put("func", "list_walk_summary");
//            json.put("userid", "");
            json.put("page", 0);
            String date = dateFormat.format(baseDate);
            json.put("base", date);
            json.put("masterkey", ((BaseActivity)getActivity()).getUserData().getMasterKey());
            json.put("section", "walks");
            json.put("scale", "date");

            // TODO 개발용 서버 접속 운영배포시에는 변경 필요
            new JSONNetworkManager(JSONNetworkManager.WALKS_INFO, json, Constants.IS_USE_DEV_SERVER) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);
                    Log.e(TAG, "error status = " + status);
                    updateWalkLogsByDay(null);
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
                                dayWalkLogs = walkLogs;

                                updateWalkLogsByDay(walkLogs);
                            }
                        }
                    }
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateWalkLogsByDay(ArrayList<WalkLog> walkLogs) {
        long[] today = MyCalendarUtil.getDayInMillis(0);
        for (int idx = 0; idx < daysLength; idx++) {
            long[] times = MyCalendarUtil.getDayInMillis(calendar, (daysLength - 1) - idx);
            Log.d(TAG, "times0: " + times[0] + ", today0: " + today[0]);

            int stepCount = 0;
            // 서버에 없는 오늘 걸음 처리 추가
            if (times[0]/1000 == today[0]/1000 && times[1]/1000 == today[1]/1000) {
                stepCount = getStepsBy(today);
                if (null != walkLogs && walkLogs.size() > 0 && idx < walkLogs.size()) {
                    WalkLog walkLog = walkLogs.get(idx);
                    String sumScope = walkLog.getSumscope();
                    Log.d(TAG, "sumScope: " + sumScope + " stepCount from db: " + stepCount);
                    walkLog.setSumscope(String.valueOf(stepCount));
                }
            } else {
                if (null != walkLogs && walkLogs.size() > 0 && idx < walkLogs.size()) {
                    WalkLog walkLog = walkLogs.get(idx);
                    String sumScope = walkLog.getSumscope();
                    if (!TextUtils.isEmpty(sumScope)) {
                        stepCount = Integer.valueOf(sumScope);
                    } else {
                        continue;
                    }
                }
            }

            float stepGraphValue = getLogScaleStepGraphValue(stepCount);

            if (stepGraphValue >= 100.0f) {
                stepGraphValue = 100.0f;
                days_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.VISIBLE);
//                days_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.VISIBLE);

            } else if (stepGraphValue <= 0.0f) {
                stepGraphValue = 0.0f;
                days_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.INVISIBLE);
                days_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.INVISIBLE);
            } else {
                days_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.VISIBLE);
//                days_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.VISIBLE);
            }
            days_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.INVISIBLE);

            if (stepCount > goalSteps) {
                days_Layout.findViewById(graphBarsPedometer[idx]).setActivated(true);
//                days_Layout.findViewById(graphDotsPedometer[idx]).setActivated(true);
            } else {
                days_Layout.findViewById(graphBarsPedometer[idx]).setActivated(false);
//                days_Layout.findViewById(graphDotsPedometer[idx]).setActivated(false);
            }

            ((TextView) days_Layout.findViewById(measurePedometer[idx])).setText(String.format(Locale.US, "%.1f", stepCount/1000.f));
            days_Layout.findViewById(graphBarsPedometer[idx]).getLayoutParams().height
                    = Math.round (stepGraphValue * mDensity);

            final int finalIdx = idx;
            days_Layout.findViewById(daysGraphLayoutsPedometer[idx]).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    popupLayer.setVisibility(View.VISIBLE);
                    popupPedometerIdx = finalIdx;
                    TextView tvPeriod = days_Layout.findViewById(daysPedometer[popupPedometerIdx]);
                    popupPedometerPeriod = tvPeriod.getText().toString();
                    showPedometerPopup();
                }
            });
        }
    }

    private float getLogScaleStepGraphValue(int stepCount) {

        final float graphMax = 100.f;

        if (stepCount >= 5000) {
            if (stepCount < 7500) {
                return 6 / 10.f * graphMax;
            } else if (stepCount < 11500) {
                return 7 / 10.f * graphMax;
            } else if (stepCount < 18000) {
                return 8 / 10.f * graphMax;
            } else if (stepCount < 28000) {
                return 9 / 10.f * graphMax;
            } else {
                return 10 / 10.f * graphMax;
            }
        } else {
            return stepCount / 10000.f * graphMax;
        }
    }

    ArrayList<WalkLog> monthWalkLogs;

    private void requestWeeksListWalkSummary(Date baseDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            JSONObject json = new JSONObject();
            json.put("func", "list_walk_summary");
//            json.put("userid", "");
            json.put("page", 0);
            String date = dateFormat.format(baseDate);
            json.put("base", date);
            json.put("masterkey", ((BaseActivity)getActivity()).getUserData().getMasterKey());
            json.put("section", "walks");
            json.put("scale", "week");

            // TODO 개발용 서버 접속 운영배포시에는 변경 필요
            new JSONNetworkManager(JSONNetworkManager.WALKS_INFO, json, Constants.IS_USE_DEV_SERVER) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);
                    Log.e(TAG, "error status = " + status);
                    updateWalkLogsByWeek(null);
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
                                weekWalkLogs = walkLogs;

                                updateWalkLogsByWeek(walkLogs);
                            }
                        }
                    }
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateWalkLogsByWeek(ArrayList<WalkLog> walkLogs) {
        for (int idx = weeksLength - 1; idx >= 0; idx--) {
            long[] times = MyCalendarUtil.getWeekOfFirstDayInMillis(calendar, (weeksLength - 1) - idx);

            int stepCount = 0;
            if (null != walkLogs && walkLogs.size() > 0 && idx < walkLogs.size()) {
                WalkLog walkLog = walkLogs.get(idx);
                String sumScope = walkLog.getSumscope();
                if (!TextUtils.isEmpty(sumScope)) {
                    stepCount = Integer.valueOf(sumScope);
                } else {
                    continue;
                }
            }

            float stepGraphValue = stepCount / (float)weekGoalMax * 100.0f;

            if (stepGraphValue >= 100.0f) {
                stepGraphValue = 100.0f;
                week_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.VISIBLE);
//                week_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.VISIBLE);

            } else if (stepGraphValue <= 0.0f) {
                stepGraphValue = 0.0f;
                week_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.INVISIBLE);
                week_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.INVISIBLE);
            } else {
                week_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.VISIBLE);
//                week_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.VISIBLE);
            }
            week_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.INVISIBLE);

            if (stepCount > 7 * goalSteps) {
                week_Layout.findViewById(graphBarsPedometer[idx]).setActivated(true);
//                week_Layout.findViewById(graphDotsPedometer[idx]).setActivated(true);
            } else {
                week_Layout.findViewById(graphBarsPedometer[idx]).setActivated(false);
//                week_Layout.findViewById(graphDotsPedometer[idx]).setActivated(true);
            }

            ((TextView) week_Layout.findViewById(measurePedometer[idx])).setText(String.format(Locale.US, "%.1f", stepCount/1000.f));
            week_Layout.findViewById(graphBarsPedometer[idx]).getLayoutParams().height
                    = Math.round (stepGraphValue * mDensity);

            final int finalIdx = idx;
            week_Layout.findViewById(weeksGraphLayoutsPedometer[idx]).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    popupLayer.setVisibility(View.VISIBLE);
                    popupPedometerIdx = finalIdx;
                    TextView tvPeriod = week_Layout.findViewById(weeksPedometer[popupPedometerIdx]);
                    popupPedometerPeriod = tvPeriod.getText().toString();
                    popupPedometerPeriod = popupPedometerPeriod.replace("\n", "");
                    showPedometerPopup();
                }
            });
        }
    }

    private void requestMonthsListWalkSummary(Date baseDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            JSONObject json = new JSONObject();
            json.put("func", "list_walk_summary");
//            json.put("userid", "");
            json.put("page", 0);
            String date = dateFormat.format(baseDate);
            json.put("base", date);
            json.put("masterkey", ((BaseActivity)getActivity()).getUserData().getMasterKey());
            json.put("section", "walks");
            json.put("scale", "month");

            monthWalkLogs = null;

            // TODO 개발용 서버 접속 운영배포시에는 변경 필요
            new JSONNetworkManager(JSONNetworkManager.WALKS_INFO, json, Constants.IS_USE_DEV_SERVER) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);
                    Log.e(TAG, "error status = " + status);
                    updateWalkLogsByMonth(null);
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
                                monthWalkLogs = walkLogs;

                                updateWalkLogsByMonth(walkLogs);
                            }
                        }
                    }
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateWalkLogsByMonth(ArrayList<WalkLog> walkLogs) {
        for (int idx = 0; idx < monthsLength; idx++) {
            calendar.add(Calendar.MONTH, 11 - calendar.get(Calendar.MONTH));

            long[] times = MyCalendarUtil.getMonthInMillis(calendar, (monthsLength - 1) - idx);

            int maximumDay = getMaximumDay(times[0]);

            int stepCount = 0;
            if (null != walkLogs && walkLogs.size() > 0 && idx < walkLogs.size()) {
                WalkLog walkLog = walkLogs.get(idx);
                String sumScope = walkLog.getSumscope();
                if (!TextUtils.isEmpty(sumScope)) {
                    stepCount = Integer.valueOf(sumScope);
                } else {
                    continue;
                }
            }

            float stepGraphValue = stepCount / 300000.f * 100.0f;

            if (stepGraphValue >= 100.0f) {
                stepGraphValue = 100.0f;
                month_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.VISIBLE);
//                month_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.VISIBLE);

            } else if (stepGraphValue <= 0.0f) {
                stepGraphValue = 0.0f;
                month_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.INVISIBLE);
                month_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.INVISIBLE);
            } else {
                month_Layout.findViewById(graphBarsPedometer[idx]).setVisibility(View.VISIBLE);
//                month_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.VISIBLE);
            }
            month_Layout.findViewById(graphDotsPedometer[idx]).setVisibility(View.INVISIBLE);

            if (stepCount > maximumDay * goalSteps) {
                month_Layout.findViewById(graphBarsPedometer[idx]).setActivated(true);
//                month_Layout.findViewById(graphDotsPedometer[idx]).setActivated(true);
            } else {
                month_Layout.findViewById(graphBarsPedometer[idx]).setActivated(false);
//                month_Layout.findViewById(graphDotsPedometer[idx]).setActivated(true);
            }

            ((TextView) month_Layout.findViewById(measurePedometer[idx])).setText(String.format(Locale.US, "%.1f", stepCount/1000.f));
            month_Layout.findViewById(graphBarsPedometer[idx]).getLayoutParams().height
                    = Math.round(stepGraphValue * mDensity);

            final int finalIdx = idx;
            month_Layout.findViewById(monthGraphLayoutsPedometer[idx]).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    popupLayer.setVisibility(View.VISIBLE);
                    popupPedometerIdx = finalIdx;
                    TextView tvPeriod = month_Layout.findViewById(monthPedometer[popupPedometerIdx]);
                    popupPedometerPeriod = tvPeriod.getText().toString();
                    showPedometerPopup();
                }
            });
        }
    }

    private int getMaximumDay(long time) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(time);

        int maximumDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return maximumDay;
    }
}
