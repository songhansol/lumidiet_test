package com.doubleh.lumidiet;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.doubleh.lumidiet.data.HelpData;
import com.doubleh.lumidiet.data.NoticeData;
import com.doubleh.lumidiet.utils.DatabaseManager;
import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.ListViewItem;
import com.doubleh.lumidiet.utils.ListViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;
import static com.doubleh.lumidiet.BaseActivity.KEY_BADGE_COUNT;
import static com.doubleh.lumidiet.BaseActivity.Preferences_LOGIN;
import static com.doubleh.lumidiet.BaseActivity.Preferences_LOGIN_AUTO;
import static com.doubleh.lumidiet.BaseActivity.getLanguage;
import static com.doubleh.lumidiet.BaseActivity.isTest;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_NAME;
import static com.doubleh.lumidiet.utils.DatabaseManager.DATABASE_VERSION;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoticeFragment extends Fragment implements View.OnClickListener {

    String TAG = "NoticeFragment";

    RelativeLayout box_Btn, mybox_Btn;
    TextView box_TextView, mybox_TextView;
    WebView webView;

    View mView;
    ListView listView;
    ListViewAdapter adapter;
    ArrayList<NoticeData> noticeDatas;
    ArrayList<HelpData> helpDatas;

    String realURLPrefix = "http://app.lumidiet.com/";
    String testURLPrefix = "http://devapp.pubple.com/";

    boolean mFocus;

    public NoticeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_notice, container, false);

        ImageButton prev_Btn = (ImageButton) mView.findViewById(R.id.notice_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.getVisibility() == View.VISIBLE) {
                    webView.setVisibility(View.INVISIBLE);
                } else {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.setNoticeButton();
                    mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                }
            }
        });

        box_Btn = (RelativeLayout) mView.findViewById(R.id.notice_btn_box);
        mybox_Btn = (RelativeLayout) mView.findViewById(R.id.notice_btn_mybox);
        box_Btn.setOnClickListener(this);
        mybox_Btn.setOnClickListener(this);
        box_TextView = (TextView) mView.findViewById(R.id.notice_txt_box);
        mybox_TextView = (TextView) mView.findViewById(R.id.notice_txt_mybox);
        webView = (WebView) mView.findViewById(R.id.webview);

        listView = (ListView) mView.findViewById(R.id.notice_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (BuildConfig.DEBUG) Log.d(TAG, "item touch: " + mFocus);

                StringBuilder sb = new StringBuilder();

                if (isTest) {
                    sb.append(testURLPrefix);
                } else {
                    sb.append(realURLPrefix);
                }

                if (mFocus) {
                    NoticeData data = noticeDatas.get(position);
                    sb.append("notice_view.php?notice_idx=");
                    sb.append(data.getKey());
                    Log.d(TAG, "test: "+data.getRead());
                    if (!data.getRead()) {
						sendNoticeRead(((MainActivity) getActivity()).getUserData().getMasterKey(), data.getKey(), "notice", data, position);
                    }
                } else {
                    HelpData data = helpDatas.get(position);
                    sb.append("help_view.php?help_idx=");
                    sb.append(data.getIdx());
                    sb.append("&help_key=");
                    sb.append(((MainActivity) getActivity()).getUserData().getMasterKey());
                    if (!data.getRead()) {
						sendNoticeRead(((MainActivity) getActivity()).getUserData().getMasterKey(), data.getIdx(), "help", data, position);
                    }
                }

                if (BuildConfig.DEBUG) Log.d(TAG, "item url: " + sb);
                //adapter.setBodyVisible(position);
                webView.setVisibility(View.VISIBLE);
                webView.setWebViewClient(new WebViewClient());
                //webView.getSettings().setJavaScriptEnabled(true);
                webView.clearCache(true);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                        if (url != null) {
                            new AlertDialog.Builder(getActivity())
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

                webView.loadUrl(sb.toString());
            }
        });

        mFocus = true;
        setTap(mFocus);

        return mView;
    }

    void sendNoticeRead(String masterkey, int idx, final String mode, final Object data, final int pos) {
		try {
			JSONObject json = new JSONObject();
			json.put("masterkey", masterkey);
			json.put("notice_data", idx);
			json.put("mode", mode);
            if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko"))
                json.put("kind", "kr");
            else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja"))
                json.put("kind", "jp");
            else if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh"))
                json.put("kind", "cn");
            else
                json.put("kind", "us");

			new JSONNetworkManager(JSONNetworkManager.NOTICE_READ, json){
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

                            new android.app.AlertDialog.Builder(getActivity())
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
						int result = responseJson.getInt("result");

						if (result == 0) {
							if (BuildConfig.DEBUG) Log.d(TAG, "sendNoticeRead failed");
						} else {
							updateDatabase(data, mode, pos);
                            updateBadge(responseJson.getInt("notice_count"));
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

	void updateDatabase(Object data, String mode, int pos) {
		if (mode.equals("notice")) {
			((NoticeData) data).setRead(true);
			DatabaseManager.getInstance().updateNoticeHistory(((MainActivity) getActivity()).getUserData().getMasterKey(), ((NoticeData) data).getKey(), ((NoticeData) data).getRead());
		} else if (mode.equals("help")) {
			((HelpData) data).setRead(true);
			DatabaseManager.getInstance().updateHelpHistory(((MainActivity) getActivity()).getUserData().getMasterKey(), ((HelpData) data).getTime(), ((HelpData) data).getRead());
		} else {
			if (BuildConfig.DEBUG) Log.d(TAG, "Who are you?");
		}
        adapter.setReadNotice(pos);
	}

	void updateBadge(int badgeCount) {
		/*int badgeCount = DatabaseManager.getInstance(getActivity().getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION)
				.countNoticeNotRead(((MainActivity) getActivity()).getUserData().getMasterKey(), getLanguage());*/

		if (badgeCount < 0) {
			if (BuildConfig.DEBUG) Log.e(TAG, "Badge count error: " + badgeCount);
			return;
		}

		if (BuildConfig.DEBUG) Log.d(TAG, "Badge count: " + badgeCount);
        getActivity().getSharedPreferences(((MainActivity) getActivity()).getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_BADGE_COUNT, badgeCount).commit();

        if (((MainActivity) getActivity()).getUserData().getFacebook()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "facebook login user");
            return;
        }

        if (!getActivity().getSharedPreferences(Preferences_LOGIN, MODE_PRIVATE).getBoolean(Preferences_LOGIN_AUTO, false)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "not auto login mode");
            return;
        }

		Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
		intent.putExtra("badge_count", badgeCount);
		//앱의  패키지 명
		intent.putExtra("badge_count_package_name", getActivity().getPackageName());
		// AndroidManifest.xml에 정의된 메인 activity 명
		intent.putExtra("badge_count_class_name", "com.doubleh.lumidiet.LoginActivity");
		getActivity().sendBroadcast(intent);
	}

    private void setTap(boolean isLeft) {
        box_Btn.setActivated(isLeft);
        mybox_Btn.setActivated(!isLeft);
        if (isLeft) {
            box_TextView.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFFFFFEFE));
            mybox_TextView.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
        }
        else {
            box_TextView.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFF5A5050));
            mybox_TextView.setTextColor(ContextCompat.getColor(mView.getContext(), R.color.colorFFFFFEFE));
        }

        setItem();
    }

    private void setItem() {
        if (adapter != null)
            adapter.clearItem();
        adapter = null;
        adapter = new ListViewAdapter();
        listView.setAdapter(adapter);

        if (mFocus) {
            if (BuildConfig.DEBUG) Log.d(TAG, ""+mFocus);
            mView.findViewById(R.id.notice_txt_mybox_msg).setVisibility(View.INVISIBLE);

            noticeDatas = DatabaseManager.getInstance().selectNoticeHistory(((MainActivity)getActivity()).getUserData().getMasterKey(), ((MainActivity)getActivity()).getLanguage());

			for (NoticeData data : noticeDatas) {
                adapter.addItem(data.getTitle(), new SimpleDateFormat("MM/dd").format(new Date(data.getTime() * 1000)), data.getBody(), data.getRead());
            }
        }
        else {
            if (BuildConfig.DEBUG) Log.d(TAG, ""+mFocus);
            helpDatas = DatabaseManager.getInstance().selectHelpHistory(((MainActivity)getActivity()).getUserData().getMasterKey());

            if (helpDatas.size() <= 0) {
                mView.findViewById(R.id.notice_txt_mybox_msg).setVisibility(View.VISIBLE);
            }
            else {
                mView.findViewById(R.id.notice_txt_mybox_msg).setVisibility(View.INVISIBLE);

                for (HelpData data : helpDatas) {
                    adapter.addItem(data.getTitle(), new SimpleDateFormat("MM/dd").format(new Date(data.getTime() * 1000)), data.getBody(), data.getReply(), data.getContact(), data.getRead());
                }
            }
        }

        adapter.notifyDataSetChanged();
        //listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.notice_btn_box:
                if (mFocus)
                    return;
                mFocus = true;
                setTap(mFocus);
                break;
            case R.id.notice_btn_mybox:
                if (!mFocus)
                    return;
                mFocus = false;
                setTap(mFocus);
                break;
        }
    }
}