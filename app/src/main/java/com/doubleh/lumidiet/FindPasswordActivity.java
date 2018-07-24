package com.doubleh.lumidiet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.utils.JSONNetworkManager;

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
import java.util.Locale;

public class FindPasswordActivity extends BaseActivity {

    String TAG = "FindPasswordActivity";

    ImageButton prev_Btn;
    EditText email_EditText;
    RelativeLayout issue_Btn, progressLayer;

    String email_address;
    PopupWindow popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);

        setDisplay(R.id.find_pw_activity);

        progressLayer = (RelativeLayout) findViewById(R.id.find_pw_progress_layer);

        prev_Btn = (ImageButton) findViewById(R.id.find_pw_btn_prev);
        prev_Btn.setOnClickListener(this);

        issue_Btn = (RelativeLayout) findViewById(R.id.find_pw_btn_issue);
        issue_Btn.setOnClickListener(this);

        setIssueButtonEnabled(false);

        email_EditText = (EditText) findViewById(R.id.find_pw_edittext_email);

        email_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (email_EditText.isFocused()) {
                    setIssueButtonEnabled(isValidEmail(s));
                }
            }
        });
    }

    private void setIssueButtonEnabled(boolean isEnabled) {
        issue_Btn.setEnabled(isEnabled);
        ImageView iv = (ImageView) findViewById(R.id.find_pw_img_issue);
        iv.setEnabled(isEnabled);
        TextView tv = (TextView) findViewById(R.id.find_pw_txt_issue);
        if (isEnabled)
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.colorFFFFFFFF));
        else
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.colorFFBCBCBC));
    }

    public void onLayoutClick(View v) {
        hideSoftKeyboardFromFocusedView(mContext, email_EditText);
        hideUI();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.find_pw_btn_prev:
                finish();
                break;
            case R.id.find_pw_btn_issue:
                requestFindPW();
                break;
        }
    }

    private void requestFindPW() {
        progressLayer.setVisibility(View.VISIBLE);

        email_address = email_EditText.getText().toString();

        try {
            JSONObject json = new JSONObject();
            json.put("mode", "pw");
            json.put("userid", email_address);
			if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko")) {
				json.put("kind", "korean");
			} else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja")) {
                json.put("kind", "japanese");
            } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh")) {
                json.put("kind", "chinese");
            }

            new JSONNetworkManager(JSONNetworkManager.MAIL, json){
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

                            new AlertDialog.Builder(FindPasswordActivity.this)
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
                    responseFindPW(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void responseFindPW(JSONObject json) {
        try {
            int result = json.getInt("result");

            setTouchEnabled(false);

            LayoutInflater inflater = (LayoutInflater) FindPasswordActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
            progressLayer.setVisibility(View.VISIBLE);
            findViewById(R.id.find_pw_progress_network).setVisibility(View.INVISIBLE);
            popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
            popup.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
            popup.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
            popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
            RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_1l_btn_ok);
            String body;

            if (result == 0) {
                body = getString(R.string.find_pw_err_msg);
            }
            else {
                body = getString(R.string.find_pw_ok_msg);
            }

            TextView tv = (TextView) layout.findViewById(R.id.popup_type2_1l_body);
            tv.setText(body);
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup.dismiss();
                    findViewById(R.id.find_pw_progress_network).setVisibility(View.VISIBLE);
                    progressLayer.setVisibility(View.INVISIBLE);
                    setTouchEnabled(true);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTouchEnabled(boolean isEnabled) {
        findViewById(R.id.find_pw_activity).setEnabled(isEnabled);
        issue_Btn.setEnabled(isEnabled);
    }
}