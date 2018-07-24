package com.doubleh.lumidiet;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePWFragment extends Fragment {

    String TAG = "ChangePWFragment";

    EditText now_EditText, new_EditText, newChk_EditText;
    RelativeLayout ok_Btn, popup_back_Layer;
    PopupWindow popup;

    String newPW;

    enum PopupState {
		NONE, CUR_ERR, NEW_ERR, INVALID, SUCCESS
	}

	PopupState state = PopupState.NONE;

    public ChangePWFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_pw, container, false);

        // add code
        ImageButton prev_Btn = (ImageButton) view.findViewById(R.id.changepw_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.hideSoftKeyboardFromFocusedView(mainActivity.getApplicationContext(), now_EditText);
                mainActivity.hideUI();
                mainActivity.setPrevContentFragmentLayout();
                mainActivity.hideUI();
            }
        });

        (view.findViewById(R.id.changepw_rlayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideSoftKeyboardFromFocusedView(getActivity().getApplicationContext(), now_EditText);
                ((MainActivity)getActivity()).hideUI();
            }
        });

        popup_back_Layer = (RelativeLayout) view.findViewById(R.id.popup_layer);

        now_EditText = (EditText) view.findViewById(R.id.changepw_edittext_now);
        new_EditText = (EditText) view.findViewById(R.id.changepw_edittext_new);
        newChk_EditText = (EditText) view.findViewById(R.id.changepw_edittext_new_chk);

        ok_Btn = (RelativeLayout) view.findViewById(R.id.changepw_btn_ok);
        ok_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPW();
            }
        });

        return view;
    }

    void checkPW() {
        String nowPW = ((MainActivity) getActivity()).getSharedPreferences(MainActivity.Preferences_LOGIN, Context.MODE_PRIVATE).getString(MainActivity.Preferences_PW, "");

        if (nowPW.equals("") || !now_EditText.getText().toString().equals(nowPW)) {
			showCurrentPasswordErrorPopup();
            return;
        }

        String newPW = new_EditText.getText().toString();
        String newPWChk = newChk_EditText.getText().toString();
        if (!newPW.equals(newPWChk)) {
			showNewPasswordErrorPopup();
            return;
        }

        if (!((MainActivity) getActivity()).isValidPassword(newPW)) {
			showInvalidPasswordErrorPopup();
            return;
        }

        this.newPW = newPW;

        requestChange();
    }

    void requestChange() {
        popup_back_Layer.setVisibility(View.VISIBLE);
        popup_back_Layer.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        try {
            JSONObject json = new JSONObject();
            json.put("mode", "up_pw");
            json.put("masterkey", ((MainActivity) getActivity()).getUserData().getMasterKey());
            json.put("password", newPW);
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
                    responseChange(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void responseChange(JSONObject json) {
        try {
            int result = json.getInt("result");

            if (result == 0) {
                // failed...
            } else {
                popup_back_Layer.findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
				showSuccessPopup();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void showCurrentPasswordErrorPopup() {
		state = PopupState.CUR_ERR;
		LayoutInflater inflater = (LayoutInflater) ((MainActivity) getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((MainActivity) getActivity()).dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_1l_btn_ok);

		String body = getString(R.string.pw_change_err_1);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_1l_body);
		tv.setText(body);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;
			}
		});
	}

	void showNewPasswordErrorPopup() {
		state = PopupState.NEW_ERR;
		LayoutInflater inflater = (LayoutInflater) ((MainActivity) getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_2l, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((MainActivity) getActivity()).dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_2l_btn_ok);

		String body = getString(R.string.pw_change_err_2);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_2l_body);
		tv.setText(body);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;
			}
		});
	}

	void showInvalidPasswordErrorPopup() {
		state = PopupState.INVALID;
		LayoutInflater inflater = (LayoutInflater) ((MainActivity) getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_3l, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((MainActivity) getActivity()).dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_3l_btn_ok);

		String body = getString(R.string.pw_change_err_3);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_3l_body);
		tv.setText(body);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;
			}
		});
	}

	void showSuccessPopup() {
		state = PopupState.SUCCESS;
		((MainActivity) getActivity()).getSharedPreferences(MainActivity.Preferences_LOGIN, Context.MODE_PRIVATE).edit().putString(MainActivity.Preferences_PW, newPW).commit();

		LayoutInflater inflater = (LayoutInflater) ((MainActivity) getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((MainActivity) getActivity()).dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_1l_btn_ok);

		String body = getString(R.string.pw_change_ok_msg);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_1l_body);
		tv.setText(body);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;

				MainActivity activity = (MainActivity) getActivity();
				activity.setPrevContentFragmentLayout();
				activity.hideUI();
			}
		});
	}

	public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case SUCCESS:
				showSuccessPopup();
				break;
			case CUR_ERR:
				showCurrentPasswordErrorPopup();
				break;
			case NEW_ERR:
				showNewPasswordErrorPopup();
				break;
			case INVALID:
				showInvalidPasswordErrorPopup();
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