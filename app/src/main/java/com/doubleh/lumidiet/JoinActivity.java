package com.doubleh.lumidiet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.data.NoticeData;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.doubleh.lumidiet.utils.JSONNetworkManager;

import org.json.JSONArray;
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
import java.util.ArrayList;

import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_NAME;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_VERSION;

public class JoinActivity extends BaseActivity {
    String TAG = "Join Activity";

    ImageButton prev_Btn;
    Button agree_Btn, dupl_chk_Btn;
    EditText id_EditText, pw_EditText, pw_reconfirm_EditText;
    RelativeLayout join_Btn, progressLayer;
    TextView tv, id_error_TextView;

    String id, pw, countryCode;
    PopupWindow popup;

    boolean agree = false, idChk = false, pwChk = false, pwReChk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        setDisplay(R.id.join_activity);

        activityList.add(this);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Activity.TELEPHONY_SERVICE);
        countryCode = telephonyManager.getNetworkCountryIso();

        ((TextView) findViewById(R.id.join_txt_tos)).setMovementMethod(new ScrollingMovementMethod());
        tv = (TextView) findViewById(R.id.join_txt_pw_error);
        id_error_TextView = (TextView) findViewById(R.id.join_txt_id_error);

        agree_Btn = (Button) findViewById(R.id.join_btn_agree);
        agree_Btn.setOnClickListener(this);

        dupl_chk_Btn = (Button) findViewById(R.id.join_btn_dupl_check);
        dupl_chk_Btn.setOnClickListener(this);
        dupl_chk_Btn.setEnabled(false);
        dupl_chk_Btn.setActivated(false);

        join_Btn = (RelativeLayout) findViewById(R.id.join_btn_join);
        join_Btn.setOnClickListener(this);
        join_Btn.setEnabled(false);

        prev_Btn = (ImageButton) findViewById(R.id.join_btn_prev);
        prev_Btn.setOnClickListener(this);

        id_EditText = (EditText) findViewById(R.id.join_edittext_id);
        pw_EditText = (EditText) findViewById(R.id.join_edittext_pw);
        pw_reconfirm_EditText = (EditText) findViewById(R.id.join_edittext_pw_reconfirm);

        id_EditText.setNextFocusDownId(R.id.join_edittext_pw);
        pw_EditText.setNextFocusDownId(R.id.join_edittext_pw_reconfirm);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (id_EditText.isFocused())
                {
                    if (isValidId(s))
                    {
                        id_error_TextView.setVisibility(View.INVISIBLE);
                        dupl_chk_Btn.setEnabled(true);
                        dupl_chk_Btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFFFFFEFE));
                    }
                    else
                    {
                        id_error_TextView.setText(getString(R.string.id_error));
                        id_error_TextView.setVisibility(View.VISIBLE);
                        dupl_chk_Btn.setEnabled(false);
                        dupl_chk_Btn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color99FFFEFE));
                    }
                    dupl_chk_Btn.setActivated(false);
                    idChk = false;
                }
                else if (pw_EditText.isFocused())
                {
                    if (isValidPassword(s))
                    {
                        pwChk = true;
                        tv.setVisibility(View.INVISIBLE);

                        String pw_re = s.toString();

                        if (pw_re.compareTo(pw_reconfirm_EditText.getText().toString()) != 0)
                        {
                            tv.setText(getString(R.string.pw_reconfirm_error));
                            tv.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        pwChk = false;
                        tv.setText(getString(R.string.pw_error));
                        tv.setVisibility(View.VISIBLE);
                    }
                }
                else if (pw_reconfirm_EditText.isFocused())
                {
                    if (!pwChk)
                        return;
                    String pw_re = s.toString();
                    if (pw_re.compareTo(pw_EditText.getText().toString()) == 0)
                    {
                        tv.setVisibility(View.INVISIBLE);
                        pwReChk = true;
                    }
                    else
                    {
                        tv.setText(getString(R.string.pw_reconfirm_error));
                        tv.setVisibility(View.VISIBLE);
                        pwReChk = false;
                    }
                }
                if (idChk && pwChk && pwReChk)
                {
                    joinBtnEnabled(agree);
                }
                else
                {
                    joinBtnEnabled(false);
                }
            }
        };
        id_EditText.addTextChangedListener(watcher);
        pw_EditText.addTextChangedListener(watcher);
        pw_reconfirm_EditText.addTextChangedListener(watcher);

        progressLayer = (RelativeLayout) findViewById(R.id.join_progress_layer);

        StringBuilder tos = new StringBuilder();
        InputStream inputStream = getResources().openRawResource(R.raw.tos);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i = 0;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }

            tos.append(new String(byteArrayOutputStream.toByteArray()/*,"UTF-8"*/));
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.join_txt_tos)).setText(tos);
    }

    public void joinBtnEnabled(boolean enabled) {
        if (enabled) {
            ((TextView) findViewById(R.id.join_txt_join)).setTextColor(ContextCompat.getColor(mContext, R.color.colorFFFFFFFF));
            findViewById(R.id.join_img_join).setBackgroundResource(R.drawable.common_bottom_arrow_nor);
            join_Btn.setBackgroundResource(R.drawable.common_bottom_btn_nor);
            join_Btn.setEnabled(true);
        }
        else {
            ((TextView) findViewById(R.id.join_txt_join)).setTextColor(ContextCompat.getColor(mContext, R.color.colorFFBCBCBC));
            findViewById(R.id.join_img_join).setBackgroundResource(R.drawable.common_bottom_arrow_dim);
            join_Btn.setBackgroundResource(R.drawable.common_bottom_btn_dim);
            join_Btn.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id)
        {
            case R.id.join_btn_agree:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick Agree Button");
                agree = !agree;
                Button b = (Button) v;
                if (agree) {
                    b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.join_agree_btn_on, 0, 0, 0);
                    if (BuildConfig.DEBUG) Log.d(TAG, ""+idChk+" "+pwChk+" "+pwReChk);
                    if (idChk && pwChk && pwReChk) {
                        joinBtnEnabled(agree);
                    }
                }
                else {
                    b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.join_agree_btn_off, 0, 0, 0);
                    joinBtnEnabled(agree);
                }
                break;
            case R.id.join_btn_dupl_check:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick Duplicate Check Button");
                hideSoftKeyboardFromFocusedView(this, id_EditText);
                hideUI();
                if (idChk)
                    return;
                this.id = id_EditText.getText().toString();
                requestDuplCheck();
                break;
            case R.id.join_btn_join:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick Join Button");
                requestJoin();
                break;
            case R.id.join_btn_prev:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick Previous Button");
                activityList.remove(this);
                finish();
                break;
        }
    }

    public void requestDuplCheck() {
        try {
            progressLayer.setVisibility(View.VISIBLE);
            JSONObject json = new JSONObject();

            json.put("userid", id);
            json.put("mode", "se_id");

            if (BuildConfig.DEBUG) Log.d(TAG, "json data: "+json);

            new JSONNetworkManager(JSONNetworkManager.MEMBER, json) {
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

                            new AlertDialog.Builder(JoinActivity.this)
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener)
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }

                @Override
                public void responseCallback(JSONObject responseJson) {
                    responseDuplCheck(responseJson);
                    progressLayer.setVisibility(View.INVISIBLE);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void responseDuplCheck(JSONObject json) {
        try {
            int result = json.getInt("result");
            if (result == 0) {
                // impossible to use
                if (BuildConfig.DEBUG) Log.d(TAG, "impossible");
                idChk = false;
                id_error_TextView.setText(getString(R.string.id_dupl_chk_impossible));
                id_error_TextView.setVisibility(View.VISIBLE);
            } else {
                // possible to use
                if (BuildConfig.DEBUG) Log.d(TAG, "possible");
                idChk = true;
                id_error_TextView.setText(getString(R.string.id_dupl_chk_possible));
                id_error_TextView.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestJoin() {
        id = id_EditText.getText().toString();
        pw = pw_EditText.getText().toString();

        if (id == null || pw == null) {
            return;
        }
        try {
            progressLayer.setVisibility(View.VISIBLE);
            JSONObject json = new JSONObject();

            json.put("userid", id);
            json.put("password", pw);
            json.put("kind", countryCode);
            json.put("tp", 1);
            json.put("mode", "in");

            if (BuildConfig.DEBUG) Log.d(TAG, "json data: "+json);

            new JSONNetworkManager(JSONNetworkManager.MEMBER, json) {
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

                            new AlertDialog.Builder(JoinActivity.this)
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
                    responseJoin(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void responseJoin(JSONObject json) {
        try {
            int result = json.getInt("result");

            if (result == 0) {
                // 가입 실패시에 나타나야 할 화면은?
            } else {
                getUserData().setMasterKey(json.getString("masterkey"));
                getUserData().setUserID(id);
                getUserData().setCountry(countryCode);
                getUserData().setFacebook(false);
                if (BuildConfig.DEBUG) Log.d(TAG, "Master Key is " + getUserData().getMasterKey());

                LayoutInflater inflater = (LayoutInflater) JoinActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.layout_popup_type2_4l, null);
                progressLayer.setVisibility(View.VISIBLE);
                findViewById(R.id.join_progress_network).setVisibility(View.INVISIBLE);
                popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
                popup.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
                popup.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
                popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
                RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_4l_btn_ok);

                String body = getString(R.string.join_message);

                TextView tv = (TextView) layout.findViewById(R.id.popup_type2_4l_body);
                tv.setText(body);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getSharedPreferences(Preferences_LOGIN, MODE_PRIVATE).edit().putString(Preferences_ID, id).commit();
                        getSharedPreferences(Preferences_LOGIN, MODE_PRIVATE).edit().putString(Preferences_PW, pw).commit();
                        popup.dismiss();
                        //progressLayer.setVisibility(View.INVISIBLE);
                        startAddInfoActivity();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startAddInfoActivity() {
        ((LoginActivity) activityList.get(0)).id_EditText.setText(id);
        ((LoginActivity) activityList.get(0)).pw_EditText.setText(pw);
        ((LoginActivity) activityList.get(0)).setLoginButtonEnabled(true);
        Intent addinfoActivity = new Intent(this, AddInfoActivity.class);
        startActivity(addinfoActivity);
        activityList.remove(this);
        finish();
    }

    public void onLayoutClick(View v) {
        hideSoftKeyboardFromFocusedView(mContext, id_EditText);
        /*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(id_EditText.getWindowToken(), 0);*/
        hideUI();
    }
}
