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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.data.UserData;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountManagementFragment extends Fragment {
    String TAG = "AccountManagementF";

    TextView now_Txt, confirmCheck_Txt;
    RelativeLayout confirm_Btn, progressLayer;
    //RelativeLayout apply_Btn;
    EditText email_EditText;
    UserData userData;
    PopupWindow popup;

	enum PopupState {
		NONE, SUCCESS, DUPLICATE
	};

	PopupState state = PopupState.NONE;

    public AccountManagementFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_management, container, false);

        //add code
        now_Txt = (TextView) view.findViewById(R.id.account_management_txt_now);
        confirmCheck_Txt = (TextView) view.findViewById(R.id.account_management_txt_confirm_check);
        confirm_Btn = (RelativeLayout) view.findViewById(R.id.account_management_btn_confirm);
        confirm_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).hideUI();
                ((MainActivity) getActivity()).hideSoftKeyboardFromFocusedView(getActivity(), email_EditText);

                requestEmailConfirm();
            }
        });
        //apply_Btn = (RelativeLayout) view.findViewById(R.id.account_management_btn_apply);
        email_EditText = (EditText) view.findViewById(R.id.account_management_edittext_confirm);
        progressLayer = (RelativeLayout) view.findViewById(R.id.account_management_progress_layer);

        email_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (email_EditText.isFocused()) {
                    if (s.length() <= 0) {
                        confirm_Btn.setEnabled(false);
                    }
                    else {
                        confirm_Btn.setEnabled(true);
                    }
                }
            }
        });

        ImageButton prev_Btn = (ImageButton) view.findViewById(R.id.account_management_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setPrevContentFragmentLayout();
                mainActivity.hideSoftKeyboardFromFocusedView(mainActivity.getApplicationContext(), email_EditText);
                mainActivity.hideUI();
            }
        });

        (view.findViewById(R.id.account_management_rlayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideSoftKeyboardFromFocusedView(getActivity().getApplicationContext(), email_EditText);
                ((MainActivity)getActivity()).hideUI();
            }
        });

        setProfile();

        return view;
    }

    private void setProfile() {
        userData = ((MainActivity) getActivity()).getUserData();

        now_Txt.setText(userData.getUserID());
        confirm_Btn.setEnabled(!userData.getMailChk());

        if (userData.getMailChk()) {
            email_EditText.setEnabled(false);
            confirmCheck_Txt.setText(R.string.confirm_ok);
        } else {
            confirmCheck_Txt.setText(R.string.confirm_check);
        }
        if (userData.getEmail().length() > 0) {
            email_EditText.setText(userData.getEmail());
        } else {
            confirm_Btn.setEnabled(false);
        }
    }

    void requestEmailConfirm() {
        progressLayer.setVisibility(View.VISIBLE);

        if (userData.getEmail().equals(email_EditText.getText().toString())){

        } else {
            userData.setEmail(email_EditText.getText().toString());
        }

        try {
            JSONObject json = new JSONObject();
            json.put("mode", "ac");
            json.put("masterkey", userData.getMasterKey());
            json.put("userid", userData.getUserID());
            json.put("email", userData.getEmail());
            if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko")) {
                json.put("kind", "korean");
            } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja")) {
                json.put("kind", "japanese");
            } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh")) {
                json.put("kind", "chinese");
            }

            new JSONNetworkManager(JSONNetworkManager.MAIL, json) {
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
                    responseEmailConfirm(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void showSuccessPopup() {
		state = PopupState.SUCCESS;
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_1l, null);
		progressLayer.setVisibility(View.VISIBLE);
		getActivity().findViewById(R.id.account_management_progress_network).setVisibility(View.INVISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
		popup.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				getActivity().dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_1l_btn_ok);

		String body = getString(R.string.send_email_msg);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_1l_body);
		tv.setText(body);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				getActivity().findViewById(R.id.account_management_progress_network).setVisibility(View.VISIBLE);
				progressLayer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;
			}
		});
	}

	void showDuplicatePopup() {
		state = PopupState.DUPLICATE;
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_type2_2l, null);
		progressLayer.setVisibility(View.VISIBLE);
		getActivity().findViewById(R.id.account_management_progress_network).setVisibility(View.INVISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
		popup.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				((MainActivity) getActivity()).dispatchTouchEvent(event);
				return false;
			}
		});

		RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_2l_btn_ok);

		String body = getString(R.string.send_email_err_msg);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_2l_body);
		tv.setText(body);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				getActivity().findViewById(R.id.account_management_progress_network).setVisibility(View.VISIBLE);
				progressLayer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;
			}
		});
	}

    void responseEmailConfirm(JSONObject json) {
        try {
            int result = json.getInt("result");

            if (result == 0) {
                // request failed
                requestEmailConfirm();
            }
            else if (result == 1) {
                // success
				showSuccessPopup();
            }
            else {
                // duplicate e-mail
				showDuplicatePopup();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case SUCCESS:
				showSuccessPopup();
				break;
			case DUPLICATE:
				showDuplicatePopup();
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