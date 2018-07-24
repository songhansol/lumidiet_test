package com.doubleh.lumidiet;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.doubleh.lumidiet.utils.MarketVersionChecker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppInfoFragment extends Fragment {

    String TAG = "AppInfoFragment";
    String lastAppVersion = "";
    TextView appVer_TextView;

    public AppInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_info, container, false);

        // add code
        ImageButton prev_Btn = (ImageButton) view.findViewById(R.id.appinfo_btn_prev);
        prev_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
            }
        });

        new MarketVersionChecker() {
            @Override
            public void callback(String marketVersion) {
                lastAppVersion = marketVersion;
                setLastAppVersion();
            }
        }.getMarketVersion(getActivity().getPackageName());

        TextView appVer_TextView = (TextView) view.findViewById(R.id.appinfo_txt_top);
        StringBuilder appVersion = new StringBuilder(getString(R.string.app_ver_prefix));
        appVersion.append(" ");
        try {
            appVersion.append(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        appVersion.append("\n ");
        appVer_TextView.setText(appVersion);

        /*TextView firmVer_TextView = (TextView) view.findViewById(R.id.appinfo_txt_bottom);
        StringBuilder firmwareVersion = new StringBuilder(getString(R.string.firmware_prefix));
        firmwareVersion.append(" ");
        firmwareVersion.append(((MainActivity) getActivity()).getFirmwareData().getRealVersion());
		firmwareVersion.append(".");
		firmwareVersion.append(((MainActivity) getActivity()).getFirmwareData().getCodeName());
        firmwareVersion.append(getString(R.string.firmware_postfix));
        firmVer_TextView.setText(firmwareVersion);

        if (((MainActivity) getActivity()).getFirmwareData().getRealVersion().equals("")) {
            firmVer_TextView.setVisibility(View.INVISIBLE);
        } else {
            firmVer_TextView.setVisibility(View.VISIBLE);
        }*/

		TextView firmVer_TextView = (TextView) view.findViewById(R.id.appinfo_txt_bottom2);

		if (((MainActivity) getActivity()).getFirmwareData().getRealVersion().equals("")) {
			firmVer_TextView.setText(getString(R.string.belt_disconnected));
		} else {
			StringBuilder firmwareVersion = new StringBuilder();
			firmwareVersion.append(getString(R.string.model));
			firmwareVersion.append(": ");
			firmwareVersion.append(((MainActivity) getActivity()).getFirmwareData().getCodeName());
			firmwareVersion.append("   ");
			firmwareVersion.append(getString(R.string.firmware));
			firmwareVersion.append(": ");
			firmwareVersion.append(((MainActivity) getActivity()).getFirmwareData().getRealVersion());
			firmVer_TextView.setText(firmwareVersion);
		}

        return view;
    }

    public void setLastAppVersion() {
        if (getView() == null) {
			return;
		}

        String nowVersion = "";
        appVer_TextView = (TextView) getView().findViewById(R.id.appinfo_txt_top);

        StringBuilder appVersion = new StringBuilder(getString(R.string.app_ver_prefix));
        appVersion.append(" ");
        try {
            nowVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            appVersion.append(nowVersion);
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Application Now Version: " + nowVersion);
        if (BuildConfig.DEBUG) Log.d(TAG, "Application Last Version: " + lastAppVersion);
        String[] nowVersions = nowVersion.split("\\.");
        String[] lastVersions = lastAppVersion.split("\\.");

		for (int i = 0; i < 3;) {
			if (Integer.parseInt(nowVersions[i]) < Integer.parseInt(lastVersions[i])) {
				// update
				if (BuildConfig.DEBUG) Log.d(TAG, "previous version");

				appVersion.append(getString(R.string.app_ver_postfix_new));

				SpannableString string = new SpannableString(appVersion);
				string.setSpan(new UnderlineSpan(), 0, string.length(), 0);

				appVer_TextView.setText(string);

				appVer_TextView.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN){
							if (appVer_TextView.getClass() == v.getClass()){
								appVer_TextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorFF00BD47));
							}
						}

						if (event.getAction() == MotionEvent.ACTION_UP){
							if(appVer_TextView.getClass() == v.getClass()){
								appVer_TextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorFF2C2C2C));
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName())));
							}
						}

						return true;
					}
				});
				break;
			} else {
				if (Integer.parseInt(nowVersions[i]) == Integer.parseInt(lastVersions[i])) {
					if (i == 2) {
						// latest version
						if (BuildConfig.DEBUG) Log.d(TAG, "latest version");
						appVersion.append(getString(R.string.app_ver_postfix_last));
						appVer_TextView.setText(appVersion);
						break;
					}
					i++;
				} else {
					// test version
					if (BuildConfig.DEBUG) Log.d(TAG, "next version");
					appVersion.append("\nTest Version");
					appVer_TextView.setText(appVersion);
					break;
				}
			}
		}
    }
}