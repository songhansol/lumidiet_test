package com.doubleh.lumidiet;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.doubleh.lumidiet.utils.ByteLengthFilter;
import com.doubleh.lumidiet.utils.JSONNetworkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class SupportFragment extends Fragment {
    String TAG = "SupportFragment";

    EditText title_EditText, body_EditText, contact_EditText;
    RelativeLayout send_Btn, popup_back_Layer;
    CharSequence title, body, contact;
    int result;

    public SupportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_support, container, false);

        // add code
        ImageButton prev_Btn = (ImageButton) view.findViewById(R.id.support_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                mainActivity.hideSoftKeyboardFromFocusedView(mainActivity, title_EditText);
                mainActivity.hideUI();
            }
        });

        (view.findViewById(R.id.support_rlayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideSoftKeyboardFromFocusedView(getActivity().getApplicationContext(), title_EditText);
                ((MainActivity)getActivity()).hideUI();
            }
        });

        title_EditText = (EditText) view.findViewById(R.id.support_edittxt_title);
        body_EditText = (EditText) view.findViewById(R.id.support_edittxt_body);
        contact_EditText = (EditText)  view.findViewById(R.id.support_edittxt_contact);
        popup_back_Layer = (RelativeLayout) view.findViewById(R.id.popup_layer);

        body_EditText.setFilters(new InputFilter[]{new ByteLengthFilter(1024, "UTF-8")});

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (title_EditText.getText().length() > 0 && body_EditText.getText().length() > 0)
                    setEnabledSendBtn(true);
                else
                    setEnabledSendBtn(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        title_EditText.addTextChangedListener(watcher);
        body_EditText.addTextChangedListener(watcher);

        send_Btn = (RelativeLayout) view.findViewById(R.id.support_btn_send);
        setEnabledSendBtn(false);
        send_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSupport();
            }
        });

        return view;
    }

    void sendSupport() {
        popup_back_Layer.setVisibility(View.VISIBLE);

        title		= title_EditText.getText();
        body		= body_EditText.getText();
		contact	= contact_EditText.getText();

        try {
            JSONObject json = new JSONObject();
            json.put("mode", "in");
            json.put("title", title);
            json.put("content", body);
            json.put("email", contact);
            json.put("kind", ((MainActivity) getActivity()).getUserData().getCountry());
            json.put("userid", ((MainActivity) getActivity()).getUserData().getMasterKey());

            new JSONNetworkManager(JSONNetworkManager.HELP, json){
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

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
                    try {
                        if (responseJson.getInt("result") == 0) {
                            if (BuildConfig.DEBUG) Log.d(TAG, "sendSupportData failed");
                            popup_back_Layer.setVisibility(View.VISIBLE);
                        } else {
                            //Log.d(TAG, "sendSupportData success");
                            popup_back_Layer.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), getString(R.string.support_success_msg), Toast.LENGTH_SHORT).show();
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                            mainActivity.hideSoftKeyboardFromFocusedView(mainActivity, title_EditText);
                            mainActivity.hideUI();
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

    void setEnabledSendBtn(boolean isEnabled) {
        send_Btn.setEnabled(isEnabled);
        send_Btn.findViewById(R.id.support_img_send).setEnabled(isEnabled);
    }

    /*void resetEditText() {
        title_EditText.setText("");
        body_EditText.setText("");

        setEnabledSendBtn(false);
    }*/
}
