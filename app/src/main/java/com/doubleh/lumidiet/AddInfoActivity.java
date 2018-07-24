package com.doubleh.lumidiet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.OnSingleClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AddInfoActivity extends BaseActivity {
    String TAG = "AddInfoActivity";

    ImageButton prev_Btn, male_Btn, female_Btn;
    EditText age_EditText, height_EditText, weight_EditText;
    RelativeLayout next_Btn, progressLayer;
    PopupWindow popup;

    int age, height, weight, state;
    int NONE = 0, AGE = 1, HEIGHT = 2, WEIGHT = 3;
    boolean maleChk = false, isFirst = true, isFocusStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_info);

        setDisplay(R.id.addinfo_activity);

        activityList.add(this);

        age = 0;
        height = 0;
        weight = 0;

        prev_Btn = (ImageButton) findViewById(R.id.addinfo_btn_prev);
        prev_Btn.setOnClickListener(this);
        male_Btn = (ImageButton) findViewById(R.id.addinfo_btn_male);
        male_Btn.setOnClickListener(this);
        female_Btn = (ImageButton) findViewById(R.id.addinfo_btn_female);
        female_Btn.setOnClickListener(this);

        next_Btn = (RelativeLayout) findViewById(R.id.addinfo_btn_next);
        next_Btn.setOnClickListener(this);

        age_EditText = (EditText) findViewById(R.id.addinfo_edittext_age);
        height_EditText = (EditText) findViewById(R.id.addinfo_edittext_height);
        weight_EditText = (EditText) findViewById(R.id.addinfo_edittext_weight);

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

                if (popup != null) {
                    return;
                }

                int temp = Integer.parseInt(s.toString());
                if (BuildConfig.DEBUG) Log.d(TAG, "final data: "+temp);

                if (temp >= 1000) {
                    if (age_EditText.isFocused()) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "2focus: age");
                        state = AGE;
                    } else if (height_EditText.isFocused()) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "2focus: height");
                        state = HEIGHT;
                    } else if (weight_EditText.isFocused()) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "2focus: weight");
                        state = WEIGHT;
                    } else {
                        // what the...
                        if (BuildConfig.DEBUG) Log.d(TAG, "2focus: err");
                    }
                    removeFocusAll();
                    showValueCheckPopup();
                }
            }
        };

        age_EditText.addTextChangedListener(textWatcher);
        height_EditText.addTextChangedListener(textWatcher);
        weight_EditText.addTextChangedListener(textWatcher);

        progressLayer = (RelativeLayout) findViewById(R.id.addinfo_progress_layer);
    }

    void removeFocusAll() {
        hideSoftKeyboardFromFocusedView(this, age_EditText);
        hideSoftKeyboardFromFocusedView(this, height_EditText);
        hideSoftKeyboardFromFocusedView(this, weight_EditText);
    }

    public void showValueCheckPopup() {
        if (BuildConfig.DEBUG) Log.d(TAG, "showValueCheckPopup()");
        removeFocusAll();

        StringBuilder sb = new StringBuilder("");

        sb.append(getString(R.string.measure_input_err_msg2));
        /*if (state == AGE) {
            // 나이
            sb.append(getString(R.string.check_et_age));
        } else if (state == HEIGHT) {
            // 키
            sb.append(getString(R.string.check_et_height));
        } else if (state == WEIGHT) {
            // 몸무게
            sb.append(getString(R.string.check_et_weight));
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "showValueCheckPopup() error");
            return;
        }*/

        progressLayer.setVisibility(View.VISIBLE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        hideUI();

        ((TextView) layout.findViewById(R.id.popup_type2_1l_body)).setText(sb);

        layout.findViewById(R.id.popup_type2_1l_btn_ok).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (state == AGE) {
                    // 나이
                    age_EditText.setText("");
                    age_EditText.requestFocus();
                } else if (state == HEIGHT) {
                    // 키
                    height_EditText.setText("");
                    height_EditText.requestFocus();
                } else if (state == WEIGHT) {
                    // 몸무게
                    weight_EditText.setText("");
                    weight_EditText.requestFocus();
                }
                popup.dismiss();
                popup = null;
                progressLayer.setVisibility(View.INVISIBLE);
                state = NONE;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id)
        {
            case R.id.addinfo_btn_prev:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick Previous Button");
                activityList.remove(this);
                finish();
                break;
            case R.id.addinfo_btn_male:
                if (!isFirst && maleChk)
                    return;
                isFirst = false;
                genderOnOff(maleChk = true);
                break;
            case R.id.addinfo_btn_female:
                if (!isFirst && !maleChk)
                    return;
                isFirst = false;
                genderOnOff(maleChk = false);
                break;
            case R.id.addinfo_btn_next:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick Next Button");
                requestAddProfile();
                break;
        }
    }

    protected void requestAddProfile() {
        progressLayer.setVisibility(View.VISIBLE);

        if (age_EditText.getText().length() <= 0) {
            age = 0;
        } else {
            age = Integer.parseInt(age_EditText.getText().toString());

            if (age < 1 || age > 150) {
                state = AGE;
                removeFocusAll();
                showValueCheckPopup();
                return;
            }
        }
        if (height_EditText.getText().length() <= 0) {
            height = 0;
        } else {
            height = Integer.parseInt(height_EditText.getText().toString());

            if (height < 50 || height > 250) {
                state = HEIGHT;
                showValueCheckPopup();
                return;
            }
        }
        if (weight_EditText.getText().length() <= 0) {
            weight = 0;
        } else {
            weight = Integer.parseInt(weight_EditText.getText().toString());

            if (weight < 20 || weight > 250) {
                state = WEIGHT;
                showValueCheckPopup();
                return;
            }
        }

        try {
            JSONObject json = new JSONObject();
            json.put("masterkey", getUserData().getMasterKey());
            json.put("sex", maleChk ? 1 : 0);
            json.put("age", age);
            json.put("height", height);
            json.put("weight", weight);
            json.put("tp", 1);
            json.put("mode", "up");

            new JSONNetworkManager(JSONNetworkManager.MEMBER, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

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

                            new AlertDialog.Builder(AddInfoActivity.this)
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener)
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    progressLayer.setVisibility(View.INVISIBLE);
                    responseAddProfile(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void responseAddProfile(JSONObject json) {
        try {
            int result = json.getInt("result");

            if (result == 0) {
                // failed...
            } else {
                //getUserData().setMasterKey(json.getString("masterkey"));
                getUserData().setUserID(json.getString("userid"));
                getUserData().setSex(json.getInt("sex"));
                getUserData().setAge(json.getInt("age"));
                getUserData().setHeight(json.getInt("height"));
                getUserData().setWeight(json.getInt("weight"));
                getUserData().setMailChk(json.getString("mail_ch") == "Y" ? true : false);

                startGuideActivity();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startGuideActivity() {
        Intent guideActivity = new Intent(this, GuideActivity.class);
        startActivity(guideActivity);
        activityList.remove(this);
        finish();
    }

    public void genderOnOff(boolean value) {
        male_Btn.setActivated(value);
        female_Btn.setActivated(!value);
        if (value) {
            if (BuildConfig.DEBUG) Log.d(TAG, "male selected");
        }
        else {
            if (BuildConfig.DEBUG) Log.d(TAG, "female selected");
        }
    }

    public void onLayoutClick(View v) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onLayoutClick");

        age_EditText.clearFocus();
        height_EditText.clearFocus();
        weight_EditText.clearFocus();

        hideSoftKeyboardFromFocusedView(mContext, age_EditText);
        /*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(id_EditText.getWindowToken(), 0);*/
        hideUI();
    }
}