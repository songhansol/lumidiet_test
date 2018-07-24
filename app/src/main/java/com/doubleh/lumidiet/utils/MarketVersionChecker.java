package com.doubleh.lumidiet.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.doubleh.lumidiet.BuildConfig;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by byj05 on 2016-12-16.
 */

public abstract class MarketVersionChecker {

    //public MarketVersionChecker() {};

    public abstract void callback(String marketVersion);

    public void getMarketVersion(final String packageName) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {

                try {
                    Document doc = Jsoup
                            .connect(
                                    "https://play.google.com/store/apps/details?id=" + packageName)
                            .get();

                    Elements Version = doc.select(".htlgb ");

                    for (Element m : Version) {
                        //if (BuildConfig.DEBUG) Log.d("getMarketVersion", "element text value: " + m.text());
                        String ret = m.text();
                        if (Pattern.matches("^[0-9]{1}.[0-9]{1}.[0-9]{1}$", ret)) {
                            return ret;
                        }
                    }   // google play html 구조 변경에 따른 소스코드 수정
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //if (BuildConfig.DEBUG) Log.d("MarketVersionChecker", "getMarketVersion result: " + s);
                callback(s);
            }
        }.execute(packageName);
    }

    @Deprecated
    public String getMarketVersionFast(String packageName) {
        String mData = "", mVer = null;

        try {
            URL mUrl = new URL("https://play.google.com/store/apps/details?id="
                    + packageName);
            HttpURLConnection mConnection = (HttpURLConnection) mUrl
                    .openConnection();

            if (mConnection == null)
                return null;

            mConnection.setConnectTimeout(5000);
            mConnection.setUseCaches(false);
            mConnection.setDoOutput(true);

            if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader mReader = new BufferedReader(
                        new InputStreamReader(mConnection.getInputStream()));

                while (true) {
                    String line = mReader.readLine();
                    if (line == null)
                        break;
                    mData += line;
                }

                mReader.close();
            }

            mConnection.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        String startToken = "softwareVersion\">";
        String endToken = "<";
        int index = mData.indexOf(startToken);

        if (index == -1) {
            mVer = null;

        } else {
            mVer = mData.substring(index + startToken.length(), index
                    + startToken.length() + 100);
            mVer = mVer.substring(0, mVer.indexOf(endToken)).trim();
        }

        return mVer;
    }
}
