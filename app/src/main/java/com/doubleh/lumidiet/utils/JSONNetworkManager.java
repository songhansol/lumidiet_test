package com.doubleh.lumidiet.utils;

import android.os.AsyncTask;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.doubleh.lumidiet.BuildConfig;

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

import static com.doubleh.lumidiet.BaseActivity.isTest;

/**
 * Created by byj05 on 2016-11-08.
 */

public abstract class JSONNetworkManager {
	String TAG = "JSONNetworkManager";
	String urlAddress = "";
	JSONObject requestJson, responseJson;

	String realURLPrefix = "http://app.lumidiet.com/";
	String testURLPrefix = "http://devapp.pubple.com/";

	final public static String MEMBER		= "member.php";
    final public static String MAIL			= "mail.php";
    final public static String VER_CH		= "ver_ch.php";
    final public static String USE_DATE		= "usedate.php";
    final public static String LDI			= "ldi.php";
	final public static String LUMIDIET		= "lumidiet.php";
    final public static String HELP			= "help.php";
	final public static String DAN_NOTICE	= "dan_notice.php";
    final public static String FIRMWARE		= "firmware.php";
	final public static String NOTICE_READ	= "notice_read.php";
	final public static String PUSH_INFO	= "push_info.php";

    final public static String WALKS_INFO	= "walks.php";

    boolean isError = false;
    int errorCode;

    public JSONNetworkManager(String phpName, JSONObject requestJson) {
        if (isTest) {
            this.urlAddress = testURLPrefix + phpName;
        } else {
            this.urlAddress = realURLPrefix + phpName;
        }
        this.requestJson = requestJson;
		//if (BuildConfig.DEBUG) Log.d(TAG, "address is: "+urlAddress);
    }

    public JSONNetworkManager(String phpName, JSONObject requestJson, boolean forceTest) {
        if (forceTest || isTest) {
            this.urlAddress = testURLPrefix + phpName;
        } else {
            this.urlAddress = realURLPrefix + phpName;
        }
        this.requestJson = requestJson;
        //if (BuildConfig.DEBUG) Log.d(TAG, "address is: "+urlAddress);
    }

    public abstract void responseCallback(JSONObject responseJson);

	@CallSuper
	public void errorCallback(int status) {
		if (BuildConfig.DEBUG) Log.e(TAG, "http status code: " + status);
	}

    public void sendJson() {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if (BuildConfig.DEBUG) Log.d(TAG, "json data: "+requestJson);

                    URL url = new URL(urlAddress);

                    HttpURLConnection conn = null;

                    OutputStream os = null;
                    InputStream is = null;
                    ByteArrayOutputStream baos = null;

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setDefaultUseCaches(false);

                    if (requestJson != null) {
						os = conn.getOutputStream();
						os.write(requestJson.toString().getBytes());
						os.flush();

						if (BuildConfig.DEBUG) Log.d(TAG, "json byte: "+requestJson.toString().getBytes());
					}

                    String response;

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        is = conn.getInputStream();

                        StringBuilder builder = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line + "\n");
                        }

                        response = builder.toString();

                        if (BuildConfig.DEBUG) Log.d(TAG, "DATA response = \n" + response);

                        responseJson = new JSONObject(response);
                    } else {
                        //if (BuildConfig.DEBUG) Log.d(TAG, "response code(http status code): " + responseCode);

                        isError = true;
						//errorCallback(responseCode);
                        errorCode = responseCode;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    isError = true;
                    errorCode = -1;
//                    errorCallback(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    isError = true;
                    errorCode = -2;
//                    errorCallback(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                    isError = true;
                    errorCode = -3;
//                    errorCallback(2);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (!isError)
                    responseCallback(responseJson);
                else
                    //responseCallback(null);
                    errorCallback(errorCode);
            }
        }.execute();
    }
}
