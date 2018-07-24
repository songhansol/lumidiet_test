package com.doubleh.lumidiet;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.common.Constants;
import com.doubleh.lumidiet.data.BeltHistory;
import com.doubleh.lumidiet.data.LDIValue;
import com.doubleh.lumidiet.data.WalkLog;
import com.doubleh.lumidiet.pedometer.Database;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.MyCalendarUtil;
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
import static com.doubleh.lumidiet.BaseActivity.Preferences_LDI_WEEK_AVG;

public class MainLDIUsageStepsRecordFragment extends Fragment {
    String TAG = "MainLDIUsageStepsRecordF";
    View mView;
    MainActivity mActivity;

    final int[] main_graph_item_ids = {
            R.id.main_graph_item1,
            R.id.main_graph_item2,
            R.id.main_graph_item3,
            R.id.main_graph_item4,
            R.id.main_graph_item5,
            R.id.main_graph_item6,
            R.id.main_graph_item7,
    };

    private int goalSteps;
    private final float graphMax = 85.0f;
    private ArrayList<WalkLog> dayWalkLogs;

    public MainLDIUsageStepsRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_main_ldi_usage_steps_record, container, false);
        mActivity = (MainActivity) getActivity();

        SharedPreferences prefs = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        goalSteps = prefs.getInt("goal", 5000);

        setFragment();

        return mView;
    }

    // not used
    @Deprecated
    public void sendLDIAverage() {
        try {
            JSONObject json = new JSONObject();
            json.put("masterkey", mActivity.getUserData().getMasterKey());
            json.put("ldi_sum", mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).getFloat(Preferences_LDI_WEEK_AVG, 0.0f));
            json.put("mode", "up");

            new JSONNetworkManager(JSONNetworkManager.MEMBER, json){
                @Override
                public void responseCallback(JSONObject responseJson) {
                    try {
                        int result = responseJson.getInt("result");

                        if (result == 0) {
                            if (BuildConfig.DEBUG) Log.d(TAG, "sendLDIAverage failed");
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

    public void setFragment() {
        if (BuildConfig.DEBUG) Log.d(TAG, "setFragment()");
        float ldiValue = -1.0f;

        long standardDate = mActivity.getSharedPreferences(mActivity.getUserData().getMasterKey(), MODE_PRIVATE).
                getLong(MainActivity.Preferences_LDI_STANDARD_DATE, 0);
        LDIValue standardLdiValue = DatabaseManager.getInstance().selectLDIHistory(
                mActivity.getUserData().getMasterKey(), standardDate);

		StringBuilder dateSB = new StringBuilder();

		if (standardDate != 0) {
			// 첫 데이터가 가장 최근 데이터
			LDIValue history = DatabaseManager.getInstance().selectLDIHistory(mActivity.getUserData().getMasterKey()).get(0);

			ldiValue = history.getLdiValue();

			Calendar c = Calendar.getInstance();
            //Log.d(TAG, ""+history.getMeasureTime());
			c.setTimeInMillis(history.getMeasureTime() * 1000);
			dateSB.append(c.get(Calendar.MONTH) + 1);
			dateSB.append("/");
			dateSB.append(c.get(Calendar.DAY_OF_MONTH));
		} else {
			dateSB.append("/");
		}
		((TextView) mView.findViewById(R.id.main_txt_date)).setText(dateSB);

        if (ldiValue == -1.0f) {
            ldiValue = standardLdiValue.getLdiValue();
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "ldiValue: " + ldiValue);
        //if (BuildConfig.DEBUG) Log.d(TAG, "standardLdiValue: " + standardLdiValue.getLdiValue());
        ldiValue = ldiValue / (float) standardLdiValue.getLdiValue() * 100.0f;
        if (BuildConfig.DEBUG) Log.d(TAG, "final ldiValue(f): " + ldiValue);
        if (BuildConfig.DEBUG) Log.d(TAG, "final ldiValue(i): " + (int)ldiValue);

        // ldi value text view
        //((TextView) mView.findViewById(R.id.main_txt_ldi_value)).setText(Integer.toString((int)ldiValue));
        // 측정값 반올림 변경에 따른 표시방식 변경 - 0612
        ((TextView) mView.findViewById(R.id.main_txt_ldi_value)).setText(Integer.toString(Math.round(ldiValue)));

        // icon setting
        if (ldiValue > 100)
            mView.findViewById(R.id.main_img_ldi_icon).setBackgroundResource(R.drawable.common_ldi_icon_bad);
        else if (ldiValue == 0)
            mView.findViewById(R.id.main_img_ldi_icon).setBackgroundResource(R.drawable.common_ldi_icon_dim);
        else
            mView.findViewById(R.id.main_img_ldi_icon).setBackgroundResource(R.drawable.common_ldi_icon_good);

        // set graph
        setGraphDate();

        setGraphWithData();

        requestDaysListWalkSummary(new Date());
    }

    private void setGraphWithData() {
        // data setting
        int idx = 0;

        String masterKey = mActivity.getUserData().getMasterKey();

        for (int id : main_graph_item_ids) {
            long[] times = MyCalendarUtil.getDayInMillis(6 - idx);
            long usingTime = 0;
            if (!TextUtils.isEmpty(masterKey) && !masterKey.equals("null")) {
                ArrayList<BeltHistory> datas
                        = DatabaseManager.getInstance().selectBeltHistory(
                        Integer.parseInt(masterKey)
                        , times[0] / 1000, times[1] / 1000);
                //Log.d(TAG, "get times1: " + times[0] + "  times2: " + times[1]);
                for (BeltHistory data : datas) {
                    usingTime += data.getUsingTime();
                    //Log.d(TAG, "using time: " + usingTime);
                }
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "usingTime(sec): " + usingTime);
            usingTime = (long)(usingTime / 60.0f);
            StringBuilder sb = new StringBuilder();
            sb.append(usingTime);
            //sb.append(getString(R.string.minute_ko));
            View graphItem = mView.findViewById(id);
            ((TextView) graphItem.findViewById(R.id.main_txt_graph_usage_time)).setText(sb);

            View graphBar = graphItem.findViewById(R.id.main_img_graph_bar);
            if (usingTime >= 15) {
                graphBar.setActivated(true);
            } else {
                graphBar.setActivated(false);
            }

            if (BuildConfig.DEBUG) Log.d(TAG, "usingTime: " + usingTime);
            float graphValue = (float)usingTime * graphMax / 30.f;

            graphBar.setVisibility(View.VISIBLE);

            if (BuildConfig.DEBUG) Log.d(TAG, "graphValue: " + graphValue);
            if (graphValue >= graphMax) {
                graphValue = graphMax;
            } else if (graphValue <= 0.0f) {
                graphValue = 0.0f;
                graphBar.setVisibility(View.INVISIBLE);
            }

            int height = Math.round(graphValue * mActivity.getDensity());
            graphBar.getLayoutParams().height = height;

            View graphPedometerBar = graphItem.findViewById(R.id.main_rlayout_graph_pedometer);
            TextView tvPedometerBubbleCount = graphItem.findViewById(R.id.main_txt_pedometer_bubble_count);
            graphPedometerBar.setVisibility(View.INVISIBLE);
            tvPedometerBubbleCount.setVisibility(View.INVISIBLE);

            idx++;
        }
    }

    private void setGraphDate() {
        // 날짜 셋팅
        Calendar c = Calendar.getInstance();
        for (int idx = main_graph_item_ids.length-1; idx >= 0; --idx) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.US, "%02d", c.get(Calendar.MONTH) + 1));
            sb.append(String.format(Locale.US, "%02d", c.get(Calendar.DAY_OF_MONTH)));
            View graphItem = mView.findViewById(main_graph_item_ids[idx]);
            ((TextView) graphItem.findViewById(R.id.main_txt_graph_days)).setText(sb);
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
    }

    private void drawGraph(View graphPedometerBar, TextView tvPedometerBubbleCount, TextView tvPedometerSteps, long steps) {
        if (BuildConfig.DEBUG) Log.d(TAG, "draw graph steps: " + steps);
        float graphValue = getLogScaleGraphValue(steps);

        if (steps >= goalSteps) {
            graphPedometerBar.setActivated(true);
            tvPedometerBubbleCount.setActivated(true);
        } else {
            graphPedometerBar.setActivated(false);
            tvPedometerBubbleCount.setActivated(false);
        }
        graphPedometerBar.setVisibility(View.VISIBLE);
        tvPedometerBubbleCount.setVisibility(View.VISIBLE);

        if (BuildConfig.DEBUG) Log.d(TAG, "draw graph value: " + graphValue);
        if (graphValue >= graphMax) {
            graphValue = graphMax;
        } else if (graphValue <= 0.0f) {
            graphValue = 0.0f;
            graphPedometerBar.setVisibility(View.INVISIBLE);
            tvPedometerBubbleCount.setVisibility(View.INVISIBLE);
        }

        String text = steps > 0 ? String.format(Locale.US, "%.1f", steps / 1000.f) : "0";
        tvPedometerBubbleCount.setText(text);
        tvPedometerSteps.setText(text);

        int height = Math.round(graphValue * mActivity.getDensity());
        graphPedometerBar.getLayoutParams().height = height;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tvPedometerBubbleCount.getLayoutParams();
        layoutParams.bottomMargin = height+Math.round(1 * mActivity.getDensity());
    }



    private float getLogScaleGraphValue(long steps) {
        if (steps >= 5000) {
            // 로그 계산식 그런데 사용하지 않음
//            final double log10_5000 = Math.log10(5000);
//            final double log10_50000 = Math.log10(50000);
//            double divider = log10_50000 - log10_5000;
//            double value = (Math.log10(steps) - log10_5000) / divider * 5000 + 5000;
//            if( value > 10000.f) {
//                value = 10000.f;
//            }
//            return (float)value / 10000.f * graphMax;
            if (steps < 7500) {
                return 6 / 10.f * graphMax;
            } else if (steps < 11500) {
                return 7 / 10.f * graphMax;
            } else if (steps < 18000) {
                return 8 / 10.f * graphMax;
            } else if (steps < 28000) {
                return 9 / 10.f * graphMax;
            } else {
                return 10 / 10.f * graphMax;
            }
        } else {
            return steps / 10000.f * graphMax;
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
                                        Gson gson = new Gson();
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
        int idx = 0;

        for (int id : main_graph_item_ids) {

            View graphItem = mView.findViewById(id);
            String day = ((TextView) graphItem.findViewById(R.id.main_txt_graph_days)).getText().toString();
            if (BuildConfig.DEBUG) Log.d(TAG, "day33: " + day);

            int stepCount = 0;
            if (R.id.main_graph_item7 == id) {
                stepCount = getStepsBy(today);
            } else {
                if (null != walkLogs && walkLogs.size() > 0 && idx < walkLogs.size()) {
                    WalkLog walkLog = walkLogs.get(idx);
                    String sumScope = walkLog.getSumscope();
                    if (!TextUtils.isEmpty(sumScope)) {
                        stepCount = Integer.valueOf(sumScope);
                    }
                }
            }

            View graphPedometerBar = graphItem.findViewById(R.id.main_rlayout_graph_pedometer);
            TextView tvPedometerBubbleCount = graphItem.findViewById(R.id.main_txt_pedometer_bubble_count);
            TextView tvPedometerSteps = graphItem.findViewById(R.id.main_txt_graph_step_count);

            drawGraph(graphPedometerBar, tvPedometerBubbleCount, tvPedometerSteps, stepCount);
            ++idx;
        }

        setPedometerBarMinMax();
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

    public void refresh() {
        try {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setPedometerBarMinMax(){
        int maxIndex = -1;
        int minIndex = -1;
        float maxStepCount = 0;
        float minStepCount = Integer.MAX_VALUE;
        for (int idx = 0; idx < 7; idx++) {
            View graphItem = mView.findViewById(main_graph_item_ids[idx]);
            TextView tvPedometerBubbleCount = graphItem.findViewById(R.id.main_txt_pedometer_bubble_count);
            View graphPedometerBar = graphItem.findViewById(R.id.main_rlayout_graph_pedometer);

            String stepCount = tvPedometerBubbleCount.getText().toString();
            if (TextUtils.isEmpty(stepCount) || stepCount.equals("0") || stepCount.equals("0.0")) {
                graphPedometerBar.setVisibility(View.GONE);
            } else {
                float steps = Float.valueOf(stepCount);
                if (steps >= maxStepCount) {
                    maxStepCount = steps;
                    maxIndex = idx;
                }
                if (steps < minStepCount) {
                    minStepCount = steps;
                    minIndex = idx;
                }
                graphPedometerBar.setVisibility(View.VISIBLE);
            }
        }

        for(int idx=0; idx < 7; ++idx) {
            TextView tvPedometerBubbleCount = mView.findViewById(main_graph_item_ids[idx]).findViewById(R.id.main_txt_pedometer_bubble_count);
            tvPedometerBubbleCount.setVisibility((minIndex == idx || maxIndex == idx) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void updateOnlyTodaySteps() {
        long[] today = MyCalendarUtil.getDayInMillis(0);
        View graphItem = mView.findViewById(R.id.main_graph_item7);
        if (BuildConfig.DEBUG) Log.d(TAG, "updateOnlyTodaySteps");

        int stepCount = getStepsBy(today);

        View graphPedometerBar = graphItem.findViewById(R.id.main_rlayout_graph_pedometer);
        TextView tvPedometerBubbleCount = graphItem.findViewById(R.id.main_txt_pedometer_bubble_count);
        TextView tvPedometerSteps = graphItem.findViewById(R.id.main_txt_graph_step_count);

        drawGraph(graphPedometerBar, tvPedometerBubbleCount, tvPedometerSteps, stepCount);

        setPedometerBarMinMax();
    }
}
