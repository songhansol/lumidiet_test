package com.doubleh.lumidiet;


import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.utils.DatabaseManager;
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
public class MemberLeaveFragment extends Fragment {
    String TAG = "MemberLeaveFragment";
    RelativeLayout leaveChk_Btn, leave_Btn, popup_back_Layer;
    ImageView leaveChk_ImageView;
    EditText pw_EditText;
    PopupWindow popup;

    enum PopupState {
		NONE, CUR_ERR, GOODBYE
	}
	PopupState state = PopupState.NONE;

    String nowPW;
    private boolean isChk = false;

    public MemberLeaveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_member_leave, container, false);

        // add code
        ImageButton prev_Btn = (ImageButton) view.findViewById(R.id.member_leave_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideSoftKeyboardFromFocusedView(getActivity().getApplicationContext(), pw_EditText);
                ((MainActivity) getActivity()).hideUI();
                ((MainActivity) getActivity()).setPrevContentFragmentLayout();
            }
        });

        (view.findViewById(R.id.member_leave_rlayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideSoftKeyboardFromFocusedView(getActivity().getApplicationContext(), pw_EditText);
                ((MainActivity)getActivity()).hideUI();
            }
        });

        popup_back_Layer = (RelativeLayout) view.findViewById(R.id.popup_layer);

        leave_Btn = (RelativeLayout) view.findViewById(R.id.member_leave_btn_leave);
        leave_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveChk();
            }
        });

        leaveChk_ImageView = (ImageView) view.findViewById(R.id.member_leave_img_del_check);

        leaveChk_Btn = (RelativeLayout) view.findViewById(R.id.member_leave_btn_del_check);
        leaveChk_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideSoftKeyboardFromFocusedView(getActivity().getApplicationContext(), pw_EditText);
                ((MainActivity) getActivity()).hideUI();
                isChk = !isChk;
                leaveBtnChk();
            }
        });

        pw_EditText = (EditText) view.findViewById(R.id.member_leave_edittext_pw);

        leaveBtnChk();

        return view;
    }

    private void leaveBtnChk() {
        leaveChk_ImageView.setActivated(isChk);
        leave_Btn.setEnabled(isChk);
    }

    private void leaveChk() {
        MainActivity activity = (MainActivity) getActivity();
        String nowPW = activity.getSharedPreferences(MainActivity.Preferences_LOGIN, Context.MODE_PRIVATE).getString(MainActivity.Preferences_PW, "");

        this.nowPW = pw_EditText.getText().toString();

        if (!nowPW.equals(this.nowPW)) {
			showCurrentPasswordErrorPopup();
            return;
        }

        requestMemberLeave();
    }

    void requestMemberLeave() {
        popup_back_Layer.setVisibility(View.VISIBLE);
        popup_back_Layer.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

        try {
            JSONObject json = new JSONObject();
            json.put("mode", "de");
            json.put("userid", ((MainActivity) getActivity()).getUserData().getUserID());
            json.put("password", nowPW);

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
                    responseMemberLeave(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void responseMemberLeave(JSONObject json) {
        try {
            int result = json.getInt("result");

            if (result == 0) {
                // failed...
                requestMemberLeave();
            } else {
                popup_back_Layer.findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);

                MainActivity activity = (MainActivity) getActivity();
                activity.getSharedPreferences(MainActivity.Preferences_LOGIN, Context.MODE_PRIVATE).edit().putString(MainActivity.Preferences_ID, "").commit();
                activity.getSharedPreferences(MainActivity.Preferences_LOGIN, Context.MODE_PRIVATE).edit().putString(MainActivity.Preferences_PW, "").commit();
                activity.getSharedPreferences(MainActivity.Preferences_LOGIN, Context.MODE_PRIVATE).edit().putBoolean(MainActivity.Preferences_LOGIN_AUTO, false).commit();
                activity.getSharedPreferences(activity.getUserData().getMasterKey(), Context.MODE_PRIVATE).edit().clear().commit();
                activity.hideSoftKeyboardFromFocusedView(activity.getApplicationContext(), pw_EditText);
                activity.hideUI();

                DatabaseManager dbManager = DatabaseManager.getInstance();
                dbManager.deleteBeltHistories(activity.getUserData().getMasterKey());
                dbManager.deleteLDIHistories(activity.getUserData().getMasterKey());
                dbManager.deleteHelpHistories(activity.getUserData().getMasterKey());
                dbManager.deleteNoticeHistories(activity.getUserData().getMasterKey());

				showGoodByePopup();
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

		String body = getString(R.string.member_leave_err_msg1);

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

	void showGoodByePopup() {
		state = PopupState.GOODBYE;

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

		String body = getString(R.string.member_leave_ok_msg);

		TextView tv = (TextView) layout.findViewById(R.id.popup_type2_2l_body);
		tv.setText(body);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;

				// 종료
				getActivity().finishAffinity();
				System.runFinalization();
				System.exit(0);

                        /*getActivity().moveTaskToBack(true);
                        for (AppCompatActivity activity : ((MainActivity) getActivity()).activityList) {
                            activity.finish();
                        }

                        android.os.Process.killProcess(android.os.Process.myPid());*/
			}
		});
	}

	public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case CUR_ERR:
				showCurrentPasswordErrorPopup();
				break;
			case GOODBYE:
				showGoodByePopup();
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
