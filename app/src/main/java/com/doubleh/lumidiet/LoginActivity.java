package com.doubleh.lumidiet;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.doubleh.lumidiet.common.Constants;
import com.doubleh.lumidiet.data.BeltHistory;
import com.doubleh.lumidiet.data.HelpData;
import com.doubleh.lumidiet.data.LDIValue;
import com.doubleh.lumidiet.data.NoticeData;
import com.doubleh.lumidiet.pedometer.SensorListenerService;
import com.doubleh.lumidiet.service.NotificationService;
import com.doubleh.lumidiet.utils.BackPressCloseHandler;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.OnSingleClickListener;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static com.doubleh.lumidiet.service.NotificationService.KEY_SET_NOTI;
import static com.doubleh.lumidiet.service.NotificationService.NAME_NOTIFICATION;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_NAME;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_VERSION;
import static com.doubleh.lumidiet.utils.JSONNetworkManager.DAN_NOTICE;

public class LoginActivity extends BaseActivity {
    String TAG = "Login Activity";

    private static final int PERMISSION_REQUEST_LUMIDIET = 248;
	private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    SharedPreferences loginPreferences;

    Button pw_find_Btn, auto_login_Btn, join_Btn;
    public EditText id_EditText, pw_EditText;
    LinearLayout fb_login_Btn, login_Btn;
    RelativeLayout progressLayer;
    PopupWindow popup;
	WebView browser;

    Handler handler;

    // facebook login variables
    private CallbackManager callbackManager;
    private BackPressCloseHandler backPressCloseHandler;

	int ANDROID_INDEX = 0, NOTI_IDX = -1;
    String id, pw, ALARM_NOTICE = "notice", KEY_NOT = "not_again";
    boolean auto = false, isInputID = false, isInputPW = false, isStart = true;

    @Override
    protected void onResume() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onResume()");
        super.onResume();
        if (isStart) {
            isStart = false;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), SplashActivity.class));
                }
            });
        }

		/*for (AppCompatActivity a : activityList) {
			Log.d(TAG, a.getPackageName());
			Log.d(TAG, a.getLocalClassName());
		}*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (isValidId(id_EditText.getText())) {
            isInputID = true;
        } else {
            isInputID = false;
        }

        setAutoLogin(loginPreferences.getBoolean(Preferences_LOGIN_AUTO, false));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRun = false;
    }

	@Override
	protected void onRestart() {
		super.onRestart();

		if (BuildConfig.DEBUG) Log.d(TAG, "onRestart");

		if (!isTaskRoot()) {
			if (BuildConfig.DEBUG) Log.d(TAG, "isTaskRoot false");
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);

		if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");

		SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
		boolean isEnabled = prefs.getBoolean("isEnabled", true);

        setDisplay(R.id.login_activity);
        mContext = this;

        activityList.add(this);

        createUserData();

        isRun = true;
        nowActivity = "LoginActivity";//getClass().getSimpleName();
        backPressCloseHandler = new BackPressCloseHandler(this);
        handler = new Handler(Looper.getMainLooper());

        if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko")) {
            setLanguage("KO");
        } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja")) {
            setLanguage("JA");
        } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh")) {
            setLanguage("ZH");
        } else {
            setLanguage("EN");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {	// token delete code
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                if (BuildConfig.DEBUG) Log.d(TAG, "Token : " + FirebaseInstanceId.getInstance().getToken());
                if (BuildConfig.DEBUG) Log.d(TAG, "Token Create Time: " + FirebaseInstanceId.getInstance().getCreationTime());
            }
        }).start();

        if (getSharedPreferences(Preferences_BLE, MODE_PRIVATE).getBoolean(KEY_AUTO, true)) {
            // bluetooth on
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled())
                bluetoothAdapter.enable();
        }

        if (getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_SET_NOTI, false))
            startService(new Intent(getApplicationContext(), NotificationService.class));

        fb_login_Btn = (LinearLayout) findViewById(R.id.login_btn_facebook);
        fb_login_Btn.setOnClickListener(this);

        login_Btn = (LinearLayout) findViewById(R.id.login_btn);
        login_Btn.setOnClickListener(this);
        login_Btn.setEnabled(false);

        pw_find_Btn = (Button)findViewById(R.id.login_btn_pw_find);
        pw_find_Btn.setOnClickListener(this);

        auto_login_Btn = (Button)findViewById(R.id.login_btn_auto);
        auto_login_Btn.setOnClickListener(this);

        join_Btn = (Button)findViewById(R.id.login_btn_join);
        join_Btn.setOnClickListener(this);

        id_EditText = (EditText)findViewById(R.id.login_edittext_id);
        pw_EditText = (EditText)findViewById(R.id.login_edittext_pw);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (id_EditText.isFocused()) {
                    if (isValidId(s)) {
                        isInputID = true;
                    }
                    else {
                        isInputID = false;
                    }
                }
                else if (pw_EditText.isFocused()) {
                    if (isValidPassword(s)) {
                        isInputPW = true;
                    }
                    else {
                        isInputPW = false;
                    }
                }

                if (isInputPW && isInputID) {
                    setLoginButtonEnabled(true);
                }
                else {
                    setLoginButtonEnabled(false);
                }
            }
        };
        id_EditText.addTextChangedListener(watcher);
        pw_EditText.addTextChangedListener(watcher);

        progressLayer = (RelativeLayout) findViewById(R.id.login_progress_layer);

        loginPreferences = getSharedPreferences(Preferences_LOGIN, MODE_PRIVATE);
        id = null;
        pw = null;

        if (isEnabled) {
            startService(new Intent(this, SensorListenerService.class));
        }

		startLoad();
    }

	private void startLoad() {

		new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setScale();
                networkCheck();
            }
        }, 200);

		if (isTest) {
			findViewById(R.id.isTest).setVisibility(View.VISIBLE);
			findViewById(R.id.update_popup_btn).setVisibility(View.VISIBLE);
			findViewById(R.id.update_popup_btn).setOnClickListener(new OnSingleClickListener() {
				@Override
				public void onSingleClick(View v) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.layout_popup_appup, null);
					progressLayer.setVisibility(View.VISIBLE);
					popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
					popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

					// exit
					layout.findViewById(R.id.popup_btn_right).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// 종료
							finishAffinity();
							System.runFinalization();
							System.exit(0);
						}
					});

					// update
					layout.findViewById(R.id.popup_btn_left).setOnClickListener(new OnSingleClickListener() {
						@Override
						public void onSingleClick(View v) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
						}
					});
				}
			});
		}
	}

	void networkCheck() {
		NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.layout_popup_err1, null);
			progressLayer.setVisibility(View.VISIBLE);
			popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
			popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

			// ok - application exit
			layout.findViewById(R.id.popup_btn).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					popup.dismiss();
					popup = null;
					progressLayer.setVisibility(View.INVISIBLE);

					finishAffinity();
					System.runFinalization();
					System.exit(0);
				}
			});
		} else {
			noticeCheck();
		}
	}

	void noticeCheck() {
		progressLayer.setVisibility(View.VISIBLE);
		new JSONNetworkManager(DAN_NOTICE, null) {
			@Override
			public void responseCallback(JSONObject responseJson) {
				try {
					if (responseJson.getInt("result") == 1) {
						JSONArray notices = responseJson.getJSONArray("notice");

						for (int i = 0; i < notices.length(); i++) {
							JSONObject notice = notices.getJSONObject(i);
							if (BuildConfig.DEBUG) Log.d(TAG, notice.toString());
							if (notice.getInt("dan_kind") == 2) {
								if (BuildConfig.DEBUG) Log.d(TAG, "chk version: " + notice.getString("content").split("\\/")[ANDROID_INDEX]);
								if (BuildConfig.DEBUG) Log.d(TAG, "now version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

								String[] nowVersions = getPackageManager().getPackageInfo(getPackageName(), 0).versionName.split("\\.");
								String[] lastVersions = notice.getString("content").split("\\/")[ANDROID_INDEX].split("\\.");

								for (int j = 0; j < 3;) {
									if (Integer.parseInt(nowVersions[j]) < Integer.parseInt(lastVersions[j])) {
										// update
										if (BuildConfig.DEBUG) Log.d(TAG, "must update");
										LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
										View layout = inflater.inflate(R.layout.layout_popup_appup, null);
										popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
										popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

										// exit
										layout.findViewById(R.id.popup_btn_right).setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												// 종료
												finishAffinity();
												System.runFinalization();
												System.exit(0);
											}
										});

										// update
										layout.findViewById(R.id.popup_btn_left).setOnClickListener(new OnSingleClickListener() {
											@Override
											public void onSingleClick(View v) {
												startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
											}
										});
										return;
									} else {
										if (Integer.parseInt(nowVersions[j]) > Integer.parseInt(lastVersions[j]) || j == 2) {
											//
											if (BuildConfig.DEBUG) Log.d(TAG, "do not need update");
											notices.remove(i);

											if (notices.length() <= 0) {
												// 알림 공지 없음
												progressLayer.setVisibility(View.INVISIBLE);
												autoLoginCheck();
												return;
											} else {
												i = -1;
												break;
											}
										}
										j++;
									}
								}
							} else {
								if (!notice.getString("language").equalsIgnoreCase(getLanguage())) {
									if (BuildConfig.DEBUG) Log.d(TAG, "delete data: " + notice.toString());
									notices.remove(i--);
								}
							}
						}
						// for 문 종료, 남아있는 가장 첫 번째 데이터를 사용, 가장 처음이 최신
						if (BuildConfig.DEBUG) Log.d(TAG, "last data: " + notices.getJSONObject(0).toString());
						NOTI_IDX = notices.getJSONObject(0).getInt("idx");
						//notices.getJSONObject(0).getString("content");
						if (NOTI_IDX == getSharedPreferences(ALARM_NOTICE, MODE_PRIVATE).getInt(KEY_NOT, -1)) {
							// 이미 다시보지 않기를 누른 경우
							if (BuildConfig.DEBUG) Log.d(TAG, "already click don't show again");
							progressLayer.setVisibility(View.INVISIBLE);
							autoLoginCheck();
						} else {
							// 다시보지 않기를 누르지 않은 경우 혹은 새로운 공지인 경우
							LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View layout = inflater.inflate(R.layout.layout_popup_noti, null);
							//progressLayer.setVisibility(View.VISIBLE);
							popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
							popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

							//((TextView) layout.findViewById(R.id.popup_body)).setText(notices.getJSONObject(0).getString("content"));
							browser = (WebView) layout.findViewById(R.id.popup_webview);
							//browser.setWebViewClient(new WebViewClient());
							browser.getSettings().setJavaScriptEnabled(true);
							browser.clearCache(true);

							browser.setWebViewClient(new WebViewClient() {
								@Override
								public boolean shouldOverrideUrlLoading(WebView view, final String url) {
									if (url != null) {
										new AlertDialog.Builder(LoginActivity.this)
												.setTitle(getString(R.string.move_link_msg))
												.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														//Yes
														startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
													}
												})
												.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														//No
														return;
													}
												})
												.show();

										return true;
									}

									return super.shouldOverrideUrlLoading(view, url);
								}
							});

							browser.loadUrl(notices.getJSONObject(0).getString("notice_url"));

							// ok
							layout.findViewById(R.id.popup_btn_right).setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if (BuildConfig.DEBUG) Log.d(TAG, "click ok");
									popup.dismiss();
									popup = null;

									browser.clearHistory();
									browser.clearCache(true);
									browser.loadUrl("about:blank");
									browser.removeAllViews();
									browser = null;

									progressLayer.setVisibility(View.INVISIBLE);
									autoLoginCheck();
								}
							});

							// don't show again
							layout.findViewById(R.id.popup_btn_left).setOnClickListener(new OnSingleClickListener() {
								@Override
								public void onSingleClick(View v) {
									if (BuildConfig.DEBUG) Log.d(TAG, "click don't show again");
									popup.dismiss();
									popup = null;
									progressLayer.setVisibility(View.INVISIBLE);
									getSharedPreferences(ALARM_NOTICE, MODE_PRIVATE).edit().putInt(KEY_NOT, NOTI_IDX).commit();
									autoLoginCheck();
								}
							});
						}
					} else {

					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void errorCallback(final int status) {
				super.errorCallback(status);

				if (status == 0 || status == 1 || status == 2) {
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

							new android.app.AlertDialog.Builder(LoginActivity.this)
									.setTitle(getString(R.string.network_err_msg))
									.setPositiveButton(getString(R.string.ok), exitListener)
									.setCancelable(false)
									.show();
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View layout = inflater.inflate(R.layout.layout_popup_err2, null);
							//progressLayer.setVisibility(View.VISIBLE);
							popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
							popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

							((TextView) layout.findViewById(R.id.popup_body)).setText("NetworkError, CODE: " + status);

							// ok
							layout.findViewById(R.id.popup_btn).setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									popup.dismiss();
									popup = null;
									progressLayer.setVisibility(View.INVISIBLE);
									//noticeCheck();
								}
							});
						}
					});
				}
			}
		}.sendJson();
	}

	void autoLoginCheck() {
		if (loginPreferences.getBoolean(Preferences_LOGIN_AUTO, false)) {
			id = loginPreferences.getString(Preferences_ID, "");
			pw = loginPreferences.getString(Preferences_PW, "");
			id_EditText.setText(id);
			pw_EditText.setText(pw);

			setAutoLogin(true);
			setLoginButtonEnabled(true);
			isInputID = true;
			isInputPW = true;

			if (pw != null && !pw.equals("")) {
				NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
				if (networkInfo == null || !networkInfo.isConnected()) {
					Toast.makeText(this, getString(R.string.network_err_msg), Toast.LENGTH_LONG).show();
				} else {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							basicLogin();
						}
					}, 1000);
				}
			}
		}
	}

    void setScale() {

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getRealSize(size);
        int screen_height = size.y;

        RelativeLayout mRLayout = (RelativeLayout) findViewById(R.id.login_activity);

        int layout_height = mRLayout.getLayoutParams().height;

        float scale = 1.0f;

        if (layout_height != screen_height)
        {
            scale = (float)screen_height / (float)layout_height;
        }
        setScale(scale);
    }

    public void setLoginButtonEnabled(boolean isEnabled) {
        login_Btn.setEnabled(isEnabled);
        if (isEnabled) {
            login_Btn.setBackgroundResource(R.drawable.login_btn_nor);
            ((TextView) findViewById(R.id.login_txt_login)).setTextColor(ContextCompat.getColor(mContext, R.color.colorFF2F2F2F));
            findViewById(R.id.login_img_arrow).setBackgroundResource(R.drawable.common_btn_arrow);
        }
        else {
            login_Btn.setBackgroundResource(R.drawable.login_btn_dim);
            ((TextView)findViewById(R.id.login_txt_login)).setTextColor(ContextCompat.getColor(mContext, R.color.colorFF0CCB54));
            findViewById(R.id.login_img_arrow).setBackgroundResource(R.drawable.common_btn_arrow_dim);
        }
    }

    @Override
    public void onClick(View v) {
        NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.network_err_msg), Toast.LENGTH_LONG).show();
            return;
        }

        int id = v.getId();
        switch (id)
        {
            case R.id.login_btn_facebook:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick facebook login button");
                facebookLogin();
                break;
            case R.id.login_btn:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick login button");
                hideSoftKeyboardFromFocusedView(this, pw_EditText);
                basicLogin();
                break;
            case R.id.login_btn_auto:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick auto login button");
                setAutoLogin(!auto);
                break;
            case R.id.login_btn_join:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick join button");
                Intent joinActivity = new Intent(this, JoinActivity.class);
                startActivity(joinActivity);
                break;
            case R.id.login_btn_pw_find:
                if (BuildConfig.DEBUG) Log.d(TAG, "onClick password find button");
                Intent findPWActivity = new Intent(this, FindPasswordActivity.class);
                startActivity(findPWActivity);
                break;
        }
    }

    public void setAutoLogin(boolean isAuto) {
        auto = isAuto;
        Button b = (Button) findViewById(R.id.login_btn_auto);
        if (auto) {
            b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.login_auto_on, 0, 0, 0);
        }
        else {
            b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.login_auto_off, 0, 0, 0);
        }
        loginPreferences.edit().putBoolean(Preferences_LOGIN_AUTO, auto).commit();
    }

    public void onLayoutClick(View v) {
        hideSoftKeyboardFromFocusedView(this, id_EditText);
        hideUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            }
        }

		if(null != callbackManager) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

	int retryCnt = 0;
    public void facebookLogin() {
        progressLayer.setVisibility(View.VISIBLE);

        // facebook sdk initialize
        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                GraphRequest request;
                request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "facebook login result: " + response.toString());

                        try {
                            if (object.isNull("email")) {
								// 전화번호 사용자들을 위함
								if (BuildConfig.DEBUG) Log.d(TAG, "email is null");
							} else {
								getUserData().setUserID(object.getString("email"));
								getUserData().setEmail(getUserData().getUserID());
                            }
                            getUserData().setFacebook(true);
                            getUserData().setFacebookID(object.getString("id"));
                            getUserData().setName(object.getString("name"));
                            getUserData().setSex(object.getString("gender").equals("male") ? 1 : 0);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        facebookLoginStep1();
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                progressLayer.setVisibility(View.INVISIBLE);
                LoginManager.getInstance().logOut();
            }

            @Override
            public void onError(FacebookException error) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error: " + error);
                progressLayer.setVisibility(View.INVISIBLE);
                LoginManager.getInstance().logOut();
				if (retryCnt < 3) {
					if (BuildConfig.DEBUG) Log.e(TAG, "retry, count: " + retryCnt);
					facebookLogin();
				} else {
					if (BuildConfig.DEBUG) Log.e(TAG, "retry failed");
					retryCnt = 0;
				}
				retryCnt++;
            }
        });
    }

    // 회원가입 확인
    private void facebookLoginStep1() {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", "se_id");
			json.put("userid", getUserData().getFacebookID());

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

							new android.app.AlertDialog.Builder(LoginActivity.this)
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
						if (responseJson.getInt("result") == 1) {
							if (getUserData().getUserID() == null || getUserData().getUserID().equals("")) {
								facebookLoginStep2();
							} else {
								JSONObject json = new JSONObject();
								json.put("mode", "se_id");
								json.put("userid", getUserData().getUserID());

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

												new android.app.AlertDialog.Builder(LoginActivity.this)
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
											if (responseJson.getInt("result") == 1) {
												facebookLoginStep2();
											} else {
												facebookLoginStep3();
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								}.sendJson();
							}
						} else {
							facebookLoginStep3();
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

    // 회원가입
    private void facebookLoginStep2() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Activity.TELEPHONY_SERVICE);
        getUserData().setCountry(telephonyManager.getNetworkCountryIso());

        try {
            JSONObject json = new JSONObject();
            json.put("mode", "in");
            json.put("userid", getUserData().getFacebookID());
            json.put("kind", getUserData().getCountry());
            json.put("tp", 2);
			json.put("fcode", getUserData().getFacebookID());

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

							new android.app.AlertDialog.Builder(LoginActivity.this)
									.setTitle(getString(R.string.network_err_msg))
									.setPositiveButton(getString(R.string.ok), exitListener)
									.setCancelable(false)
									.show();
						}
					});
				}

                @Override
                public void responseCallback(JSONObject responseJson) {
                    facebookLoginStep3();
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 로그인
    private void facebookLoginStep3() {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", "se");
			if (getUserData().getUserID() == null || getUserData().getUserID().equals("")) {
				if (BuildConfig.DEBUG) Log.d(TAG, "is null");
				json.put("userid", getUserData().getFacebookID());
			} else {
				if (BuildConfig.DEBUG) Log.d(TAG, "not null");
				json.put("userid", getUserData().getUserID());
			}
            json.put("tp", 2);
			json.put("fcode", getUserData().getFacebookID());

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

							new android.app.AlertDialog.Builder(LoginActivity.this)
									.setTitle(getString(R.string.network_err_msg))
									.setPositiveButton(getString(R.string.ok), exitListener)
									.setCancelable(false)
									.show();
						}
					});
				}

                @Override
                public void responseCallback(JSONObject responseJson) {
                    loginCheck(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void basicLogin() {
        progressLayer.setVisibility(View.VISIBLE);

        id = id_EditText.getText().toString();
        pw = pw_EditText.getText().toString();

        if (id == null || pw == null) {
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("userid", id);
            json.put("password", pw);
            json.put("tp", 1);
            json.put("mode", "se");

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

							new android.app.AlertDialog.Builder(LoginActivity.this)
									.setTitle(getString(R.string.network_err_msg))
									.setPositiveButton(getString(R.string.ok), exitListener)
									.setCancelable(false)
									.show();
						}
					});
				}

                @Override
                public void responseCallback(JSONObject responseJson) {
                    loginCheck(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void devBasicLogin() {
		id = id_EditText.getText().toString();
		pw = pw_EditText.getText().toString();

		if (id == null || pw == null) {
			return;
		}

		try {
			JSONObject json = new JSONObject();
			json.put("userid", id);
			json.put("password", pw);
			json.put("tp", 1);
			json.put("mode", "se");

			new JSONNetworkManager(JSONNetworkManager.MEMBER, json, true){
				@Override
				public void errorCallback(int status) {
					super.errorCallback(status);
					Log.e(TAG, "devBasicLogin Error status =" + status);
				}

				@Override
				public void responseCallback(JSONObject responseJson) {
					Log.d(TAG, "devBasicLogin success masterkey = " + responseJson.optString("masterkey"));
				}
			}.sendJson();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

    public void loginCheck(JSONObject json) {
        try {
            int result = json.getInt("result");

            if (result == 0) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Login failed");

                setTouchEnabled(false);

                LayoutInflater inflater = (LayoutInflater) LoginActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.layout_popup_type2_2l, null);
                if (progressLayer.getVisibility() == View.INVISIBLE)
                    progressLayer.setVisibility(View.VISIBLE);
                findViewById(R.id.login_progress_network).setVisibility(View.INVISIBLE);
                popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
                popup.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
                popup.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
                popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
                RelativeLayout closeBtn = (RelativeLayout) layout.findViewById(R.id.popup_type2_2l_btn_ok);

                String body = getString(R.string.login_error1);

                TextView tv = (TextView) layout.findViewById(R.id.popup_type2_2l_body);
                tv.setText(body);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                        findViewById(R.id.login_progress_network).setVisibility(View.VISIBLE);
                        progressLayer.setVisibility(View.INVISIBLE);
                        setTouchEnabled(true);
                    }
                });
            }
            else {
                // login success

				getUserData().setMasterKey(json.getString("masterkey"));
				getUserData().setUserID(json.getString("userid"));
				getUserData().setEmail(json.getString("email"));
                if (!json.getString("sex").equals(""))
					getUserData().setSex(json.getInt("sex"));
				getUserData().setAge(json.getInt("age"));
				getUserData().setHeight(json.getInt("height"));
				getUserData().setWeight(json.getInt("weight"));
				getUserData().setMailChk(json.getString("mail_ch").equalsIgnoreCase("Y") ? true : false);
				getUserData().setCountry(((TelephonyManager)getSystemService(Activity.TELEPHONY_SERVICE)).getNetworkCountryIso());

                getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_CUR, json.getInt("use_date_num")).commit();
                getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putLong(KEY_DATE, json.getLong("use_date")).commit();
                getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_MAX, json.getInt("max_use_date_num")).commit();
                getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putLong(MainActivity.Preferences_LDI_STANDARD_DATE, json.getLong("ldi_center")).commit();

                if (getUserData().getFacebook()) {
                    loginPreferences.edit().putString(Preferences_ID, "").commit();
                    loginPreferences.edit().putString(Preferences_PW, "").commit();
                    loginPreferences.edit().putBoolean(Preferences_LOGIN_AUTO, false).commit();
                } else {
					loginPreferences.edit().putString(Preferences_MK, getUserData().getMasterKey());
                    loginPreferences.edit().putString(Preferences_ID, id).commit();
                    loginPreferences.edit().putString(Preferences_PW, pw).commit();
                    loginPreferences.edit().putBoolean(Preferences_LOGIN_AUTO, auto).commit();
                }
				if (Constants.IS_USE_DEV_SERVER) {
                	devBasicLogin();
                }
                sendPushData();
                requestVersionSync();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendPushData() {
		try {
			JSONObject json = new JSONObject();
			json.put("masterkey", getUserData().getMasterKey());
			json.put("push_token", FirebaseInstanceId.getInstance().getToken());
			json.put("uuid", Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID));
			json.put("login_status", auto ? "on" : "off");
			json.put("os_info", "android");
			if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko"))
				json.put("kind", "kr");
			else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja"))
				json.put("kind", "jp");
			else if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh"))
				json.put("kind", "cn");
			else
				json.put("kind", "us");

			new JSONNetworkManager(JSONNetworkManager.PUSH_INFO, json) {
				@Override
				public void responseCallback(JSONObject responseJson) {
					try {
						int result = responseJson.getInt("result");
						if (result == 0) {
							if (BuildConfig.DEBUG) Log.d(TAG, "Push Data send failed");
						} else {
							if (BuildConfig.DEBUG) Log.d(TAG, "Push Data send success");
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

    private void requestVersionSync() {
        try {
            JSONObject json = new JSONObject();
            json.put("mode", "ver_ch");
            json.put("masterkey", getUserData().getMasterKey());
            //json.put("notice_ver", getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).getLong(Preferences_NOTICE_HISTORY_VERSION, 0));
            json.put("notice_ver", 0);
            //json.put("help_ver", getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).getLong(Preferences_HELP_HISTORY_VERSION, 0));
            json.put("device_ver", getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).getLong(Preferences_BELT_HISTORY_VERSION, 0));
            json.put("device_ldi_ver", getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).getLong(Preferences_LDI_HISTORY_VERSION, 0));
            json.put("lang", getLanguage());
            JSONArray jsonArr = new JSONArray();
            DatabaseManager dbManager = DatabaseManager.getInstance(getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
            ArrayList<NoticeData> noticeDatas = dbManager.selectNoticeHistory(getUserData().getMasterKey(), getLanguage());

            for (NoticeData noticeData : noticeDatas) {
                if (noticeData.getRead()) {
                    JSONObject arrObj = new JSONObject();
                    arrObj.put("idx", noticeData.getKey());
                    jsonArr.put(arrObj);
                }
            }

            if (jsonArr.length() <= 0) {
                JSONObject item = new JSONObject();
                item.put("idx", 0);
                jsonArr.put(item);
            }
            json.put("notice_data", jsonArr);

            dbManager.deleteNoticeHistories(getUserData().getMasterKey());
			dbManager.deleteHelpHistories(getUserData().getMasterKey());

            new JSONNetworkManager(JSONNetworkManager.VER_CH, json){
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

							new android.app.AlertDialog.Builder(LoginActivity.this)
									.setTitle(getString(R.string.network_err_msg))
									.setPositiveButton(getString(R.string.ok), exitListener)
									.setCancelable(false)
									.show();
						}
					});
				}

                @Override
                public void responseCallback(JSONObject responseJson) {
                    responseVersionSync(responseJson);
                }
            }.sendJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void responseVersionSync(JSONObject json) {
        try {
            int result = json.getInt("result");

            if (result == 0) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Version Sync failed");
            }
            else {
                // version sync success
                long noticeVersion = json.getLong("notice_ver");
                long helpVersion = json.getLong("help_ver");
                long beltVersion = json.getLong("device_ver");
                long ldiVersion = json.getLong("device_ldi_ver");
                getFirmwareData().setVersion(json.getString("firmware_ver"));
                getFirmwareData().setUrl(json.getString("firmwarefile"));

				int badgeCount = json.getInt("notice_count");
				getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_BADGE_COUNT, badgeCount).commit();
				if (BuildConfig.DEBUG) Log.d(TAG, "not read notice count: " + badgeCount);

                if (loginPreferences.getBoolean(Preferences_LOGIN_AUTO, false)) {
                    if (badgeCount < 0) {
                        if (BuildConfig.DEBUG) Log.e(TAG, "Badge count error: " + badgeCount);
                    } else {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Reset Badge count: " + badgeCount);

                        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
                        intent.putExtra("badge_count", badgeCount);
                        //앱의  패키지 명
                        intent.putExtra("badge_count_package_name", getPackageName());
                        // AndroidManifest.xml에 정의된 메인 activity 명
                        intent.putExtra("badge_count_class_name", "com.doubleh.lumidiet.LoginActivity");
                        sendBroadcast(intent);
                    }
                }

                JSONArray responseArr;
                responseArr = json.getJSONArray("notice_data");

                DatabaseManager dbManager = DatabaseManager.getInstance();

                if (responseArr.length() > 0) {
                    getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putLong(Preferences_NOTICE_HISTORY_VERSION, noticeVersion).commit();

                    ArrayList<NoticeData> datas = new ArrayList<>();

                    for (int i = 0; i < responseArr.length(); i++) {
                        JSONObject obj = responseArr.getJSONObject(i);
                        NoticeData data = new NoticeData();
                        data.setKey(obj.getInt("idx"));
                        data.setTitle(obj.getString("title"));
                        data.setBody(obj.getString("content"));
                        data.setTime(obj.getLong("crdate"));
                        data.setCountry(obj.getString("kind"));
                        data.setRead(obj.getInt("readYN") == 0 ? false : true);
                        data.setLanguage(obj.getString("language"));

                        datas.add(data);
                    }
                    dbManager.insertNoticeHistory(datas, Integer.parseInt(getUserData().getMasterKey()));
                }
                responseArr = json.getJSONArray("help_data");
                if (responseArr.length() > 0) {
                    getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putLong(Preferences_HELP_HISTORY_VERSION, helpVersion).commit();

                    ArrayList<HelpData> datas = new ArrayList<>();

                    for (int i = 0; i < responseArr.length(); i++) {
                        JSONObject obj = responseArr.getJSONObject(i);
						if (BuildConfig.DEBUG) Log.d(TAG, "help_data["+i+"]: "+obj);
                        HelpData data = new HelpData();
                        data.setTitle(obj.getString("title"));
                        data.setBody(obj.getString("content"));
                        data.setReply(obj.getString("Reply"));
                        data.setTime(obj.getLong("crdate"));
						data.setContact(obj.getString("email"));
						data.setIdx(obj.getInt("idx"));
                        data.setRead(obj.getInt("readYN") == 0 ? false : true);

                        datas.add(data);
                    }
                    dbManager.insertHelpHistory(datas, Integer.parseInt(getUserData().getMasterKey()));
                }
                responseArr = json.getJSONArray("device_data");
                if (responseArr.length() > 0) {
                    getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putLong(Preferences_BELT_HISTORY_VERSION, beltVersion).commit();

                    ArrayList<BeltHistory> datas = new ArrayList<>();

                    for (int i = 0; i < responseArr.length(); i++) {
                        JSONObject obj = responseArr.getJSONObject(i);
                        BeltHistory data = new BeltHistory();
                        data.setUserId(obj.getString("masterkey"));
                        data.setType((short)obj.getInt("type"));
                        data.setMode((short)obj.getInt("ledmode"));
                        data.setLdiValue(obj.getInt("ldi"));
                        data.setStartTime(obj.getLong("use_s_time"));
                        data.setEndTime(obj.getLong("use_e_time"));
                        data.setDeviceId(obj.getString("device"));

                        datas.add(data);
                    }
                    dbManager.insertBeltHistory(datas);
                }
                responseArr = json.getJSONArray("device_ldi_data");
                if (responseArr.length() > 0) {
                    getSharedPreferences(getUserData().getMasterKey(), MODE_PRIVATE).edit().putLong(Preferences_LDI_HISTORY_VERSION, ldiVersion).commit();

                    ArrayList<LDIValue> datas = new ArrayList<>();

                    for (int i = 0; i < responseArr.length(); i++) {
                        JSONObject obj = responseArr.getJSONObject(i);
                        LDIValue data = new LDIValue();
                        data.setUserId(obj.getString("masterkey"));
                        //data.setType((short)obj.getInt("type"));
                        //data.setMode((short)obj.getInt("ledmode"));
                        data.setLdiValue(obj.getInt("ldi"));
                        data.setMeasureTime(obj.getLong("use_s_time"));
                        data.setDeviceId(obj.getString("device"));

                        datas.add(data);
                    }
                    dbManager.insertLDIHistory(datas);
                }

                progressLayer.setVisibility(View.INVISIBLE);

                checkPermission();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTouchEnabled(boolean isEnabled) {
        findViewById(R.id.login_activity).setEnabled(isEnabled);
        findViewById(R.id.login_btn_auto).setEnabled(isEnabled);
        findViewById(R.id.login_btn_join).setEnabled(isEnabled);
        findViewById(R.id.login_btn_pw_find).setEnabled(isEnabled);
    }

    /**
     * Permission check.
     */
    private void checkPermission() {
        if (BuildConfig.DEBUG) Log.d(TAG, "checkPermission()");
        if (Build.VERSION.SDK_INT >= 23 &&
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CALENDAR, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LUMIDIET);
        } else {
            // 항상 허용인 경우
            pw_EditText.setText("");
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_LUMIDIET) {
            if (grantResults.length <= 0)
                return;

            boolean allAllow = true;

            for (int value : grantResults) {
                if (value ==PackageManager.PERMISSION_DENIED) {
                    allAllow = false;
                    break;
                }
            }

            if (allAllow) {
                pw_EditText.setText("");
                Intent mainActivity = new Intent(this, MainActivity.class);
                startActivity(mainActivity);
            } else  {
                Toast.makeText(this, getString(R.string.permission_err), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}