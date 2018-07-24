package com.doubleh.lumidiet;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doubleh.lumidiet.data.UserData;
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

import static com.doubleh.lumidiet.BaseActivity.hideSoftKeyboardFromFocusedView;
import static com.doubleh.lumidiet.BaseActivity.showSoftKeyboardFromFocusedView;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoManagementFragment extends Fragment {
    String TAG = "InfoManagementFragment";

    EditText age_EditText, height_EditText, weight_EditText;
    ImageButton male_Btn, female_Btn;
    RelativeLayout ok_Btn, progressLayer;

    int age, height, weight;
    UserData userData;
    View mView;
	PopupWindow popup;

    enum PopupState {
		NONE, AGE, HEIGHT, WEIGHT, INPUT_MSG
	}
	PopupState state = PopupState.NONE;

    boolean maleCheck = true, isFocusStart = true;

    public InfoManagementFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_info_management, container, false);

        (mView.findViewById(R.id.info_management_rlayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((MainActivity)getActivity()).hideSoftKeyboardFromFocusedView(mView.getContext(), age_EditText);
                age_EditText.clearFocus();
                height_EditText.clearFocus();
                weight_EditText.clearFocus();
                removeFocusAll();
                ((MainActivity)getActivity()).hideUI();
            }
        });

        progressLayer = (RelativeLayout) mView.findViewById(R.id.info_management_progress_layer);

        // add code
        ImageButton prev_Btn = (ImageButton) mView.findViewById(R.id.info_management_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                age_EditText.addTextChangedListener(null);
                age_EditText.setOnFocusChangeListener(null);
                height_EditText.addTextChangedListener(null);
                height_EditText.setOnFocusChangeListener(null);
                weight_EditText.addTextChangedListener(null);
                weight_EditText.setOnFocusChangeListener(null);

                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setPrevContentFragmentLayout();
                hideSoftKeyboardFromFocusedView(mView.getContext(), age_EditText);
                mainActivity.hideUI();
            }
        });

        male_Btn = (ImageButton) mView.findViewById(R.id.info_management_btn_male);
        female_Btn = (ImageButton) mView.findViewById(R.id.info_management_btn_female);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();

                if (id == R.id.info_management_btn_male) {
                    if (!maleCheck) {
                        maleCheck = !maleCheck;
                        setMaleFemaleBtn();
                    }
                }
                else {
                    if (maleCheck) {
                        maleCheck = !maleCheck;
                        setMaleFemaleBtn();
                    }
                }
            }
        };

        male_Btn.setOnClickListener(listener);
        female_Btn.setOnClickListener(listener);

        age_EditText = (EditText) mView.findViewById(R.id.info_management_edittext_age);
        height_EditText = (EditText) mView.findViewById(R.id.info_management_edittext_height);
        weight_EditText = (EditText) mView.findViewById(R.id.info_management_edittext_weight);

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
                        state = PopupState.AGE;
                    } else if (height_EditText.isFocused()) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "2focus: height");
                        state = PopupState.HEIGHT;
                    } else if (weight_EditText.isFocused()) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "2focus: weight");
                        state = PopupState.WEIGHT;
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

        ok_Btn = (RelativeLayout) mView.findViewById(R.id.info_management_btn_ok);
        ok_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				requestUpdateInfo();
            }
        });

        setInfoData();

        return mView;
    }

    void removeFocusAll() {
        hideSoftKeyboardFromFocusedView(getActivity(), age_EditText);
        hideSoftKeyboardFromFocusedView(getActivity(), height_EditText);
        hideSoftKeyboardFromFocusedView(getActivity(), weight_EditText);
    }

    public void showValueCheckPopup() {
        if (BuildConfig.DEBUG) Log.d(TAG, "showValueCheckPopup()");
        removeFocusAll();

        StringBuilder sb = new StringBuilder("");

        sb.append(getString(R.string.measure_input_err_msg2));

        progressLayer.setVisibility(View.VISIBLE);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((MainActivity) getActivity()).dispatchTouchEvent(event);
                return false;
            }
        });

        ((MainActivity) getActivity()).hideUI();

        ((TextView) layout.findViewById(R.id.popup_type2_1l_body)).setText(sb);

        layout.findViewById(R.id.popup_type2_1l_btn_ok).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (state == PopupState.AGE) {
                    // 나이
                    if (userData.getAge() > 0)
                        age_EditText.setText(Integer.toString(userData.getAge()));
                    else
                        age_EditText.setText("");
                    age_EditText.requestFocus();
                } else if (state == PopupState.HEIGHT) {
                    // 키
                    if (userData.getHeight() > 0)
                        height_EditText.setText(Integer.toString(userData.getHeight()));
                    else
                        height_EditText.setText("");
                    height_EditText.requestFocus();
                } else if (state == PopupState.WEIGHT) {
                    // 몸무게
                    if (userData.getWeight() > 0)
                        weight_EditText.setText(Integer.toString(userData.getWeight()));
                    else
                        weight_EditText.setText("");
                    weight_EditText.requestFocus();
                }
                popup.dismiss();
                popup = null;
                progressLayer.setVisibility(View.INVISIBLE);
                state = PopupState.NONE;
            }
        });
    }

    public void setInfoData() {
        userData = ((MainActivity) getActivity()).getUserData();

        if (BuildConfig.DEBUG) Log.d(TAG, "master key: " + userData.getMasterKey());

        if (userData.getAge() > 0)
            age_EditText.setText(Integer.toString(userData.getAge()));
        if (userData.getHeight() > 0)
            height_EditText.setText(Integer.toString(userData.getHeight()));
        if (userData.getWeight() > 0)
            weight_EditText.setText(Integer.toString(userData.getWeight()));

        if (userData.getSex() == 0) {
            maleCheck = false;
        }
        else {
            maleCheck = true;
        }

        setMaleFemaleBtn();
    }

    public void setMaleFemaleBtn() {
        male_Btn.setActivated(maleCheck);
        female_Btn.setActivated(!maleCheck);
    }

    void inputMsgPopup() {
        state = PopupState.INPUT_MSG;

        progressLayer.setVisibility(View.VISIBLE);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
        popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((MainActivity) getActivity()).dispatchTouchEvent(event);
                return false;
            }
        });

        ((MainActivity) getActivity()).hideUI();

        ((TextView) layout.findViewById(R.id.popup_type2_1l_body)).setText(getActivity().getString(R.string.input_check_msg));

        layout.findViewById(R.id.popup_type2_1l_btn_ok).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                popup.dismiss();
                popup = null;
                progressLayer.setVisibility(View.INVISIBLE);
                state = PopupState.NONE;
            }
        });
    }

    public void requestUpdateInfo() {
        if (age_EditText.getText().toString().length() <= 0) {
            age = 0;
        }
        else {
            age = Integer.parseInt(age_EditText.getText().toString());
        }
        if (height_EditText.getText().toString().length() <= 0) {
            height = 0;
        }
        else {
            height = Integer.parseInt(height_EditText.getText().toString());
        }
        if (weight_EditText.getText().toString().length() <= 0) {
            weight = 0;
        }
        else {
            weight = Integer.parseInt(weight_EditText.getText().toString());
        }

        if (age_EditText.getText().length() <= 0) {
            age = 0;
        } else {
            age = Integer.parseInt(age_EditText.getText().toString());

            if (age < 1 || age > 150) {
                state = PopupState.AGE;
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
                state = PopupState.HEIGHT;
                showValueCheckPopup();
                return;
            }
        }
        if (weight_EditText.getText().length() <= 0) {
            weight = 0;
        } else {
            weight = Integer.parseInt(weight_EditText.getText().toString());

            if (weight < 20 || weight > 250) {
                state = PopupState.WEIGHT;
                showValueCheckPopup();
                return;
            }
        }

        /*if (age == 0 || height == 0 || weight == 0) {
            inputMsgPopup();
            return;
        }*/

        try {
            JSONObject json = new JSONObject();
            json.put("mode", "up");
            json.put("masterkey", (userData.getMasterKey()));
            json.put("sex", maleCheck ? 1 : 0);
            json.put("age", age);
            json.put("height", height);
            json.put("weight", weight);
            if (userData.getFacebook())
                json.put("tp", 2);
            else
                json.put("tp", 1);

            new JSONNetworkManager(JSONNetworkManager.MEMBER, json){
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
                    progressLayer.setVisibility(View.INVISIBLE);
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

                userData.setMasterKey(json.getString("masterkey"));
                userData.setUserID(json.getString("userid"));
                userData.setSex(json.getInt("sex"));
                userData.setAge(json.getInt("age"));
                userData.setHeight(json.getInt("height"));
                userData.setWeight(json.getInt("weight"));
                userData.setMailChk(json.getString("mail_ch") == "Y" ? true : false);

                Toast.makeText(getActivity(), getString(R.string.my_info_manage_success_msg), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

	public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case AGE:
            case HEIGHT:
            case WEIGHT:
				showValueCheckPopup();
				break;
            case INPUT_MSG:
                inputMsgPopup();
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
