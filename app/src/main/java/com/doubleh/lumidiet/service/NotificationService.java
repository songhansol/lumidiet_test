package com.doubleh.lumidiet.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.LoginActivity;
import com.doubleh.lumidiet.MainActivity;
import com.doubleh.lumidiet.R;

import java.util.Calendar;

import static com.doubleh.lumidiet.BaseActivity.isRun;
import static com.doubleh.lumidiet.BaseActivity.nowActivity;

public class NotificationService extends Service {
    public static final String NAME_NOTIFICATION = "noti";
    public static final String KEY_SUN = "sun";
    public static final String KEY_MON = "mon";
    public static final String KEY_TUE = "tue";
    public static final String KEY_WED = "wed";
    public static final String KEY_THR = "thr";
    public static final String KEY_FRI = "fri";
    public static final String KEY_SAT = "sat";
    public static final String KEY_HOUR = "hh";
    public static final String KEY_MINUTE = "mm";
    public static final String KEY_SET_NOTI = "isnoti";

    public static NotiData notiData = null;

    NotificationManager notificationManager;
    ServiceThread serviceThread = null;
    Notification notification = null;
    boolean isNoti = true;
    boolean isTest = false;

    public static class NotiData {
        int hour, minute;
        boolean[] days = {
                false, false, false, false, false, false, false
        };

        public void setHour(int hour) {
            this.hour = hour;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public void setDays(boolean[] days) {
            this.days = days;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public boolean[] getDays() {
            return days;
        }

        public boolean getDays(int idx) {
            return days[idx];
        }
    }

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d("NotificationService", "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        if (BuildConfig.DEBUG) Log.d("NotificationService", "onStartCommand");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        serviceThread = new ServiceThread(handler);
        serviceThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();
        serviceThread.stopForever();
        serviceThread = null;
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = null;//new Intent(NotificationService.this, LoginActivity.class);

            if (isRun) {
                if (nowActivity.equals("MainActivity"))
                    intent = new Intent(NotificationService.this, MainActivity.class);
                else if (nowActivity.equals("LoginActivity"))
                    intent = new Intent(NotificationService.this, LoginActivity.class);
            } else {
                intent = new Intent(NotificationService.this, LoginActivity.class);
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.noti_msg))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(getApplicationContext().getString(R.string.app_name))
                    .setContentIntent(pendingIntent)
                    .build();

            //소리추가
            notification.defaults = Notification.DEFAULT_SOUND;

            //알림 소리를 한번만 내도록
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;

            //확인하면 자동으로 알림이 제거 되도록
            notification.flags = Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(777 , notification);

            if (isTest) {
                int min = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getInt(KEY_MINUTE, -1) + 2;
                getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).edit().putInt(KEY_MINUTE, min).commit();
                notiData.setMinute(min);
            }
        }
    };

    public class ServiceThread extends Thread {

        Handler handler;
        boolean isRun = true;

        public ServiceThread(Handler handler) {
            this.handler = handler;
        }

        public void stopForever() {
            synchronized (this) {
                this.isRun = false;
            }
        }

        public void setNotiData() {
            notiData = new NotiData();

            boolean days[] = new boolean[7];
            days[0] = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_SUN, false);
            days[1] = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_MON, false);
            days[2] = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_TUE, false);
            days[3] = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_WED, false);
            days[4] = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_THR, false);
            days[5] = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_FRI, false);
            days[6] = getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getBoolean(KEY_SAT, false);

            notiData.setDays(days);
            notiData.setHour(getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getInt(KEY_HOUR, -1));
            notiData.setMinute(getSharedPreferences(NAME_NOTIFICATION, MODE_PRIVATE).getInt(KEY_MINUTE, -1));
        }

        @Override
        public void run() {
            //super.run();
            while (isRun) {
                if (notiData == null || notiData.getDays() == null || notiData.getHour() == -1 || notiData.getMinute() == -1) {
                    setNotiData();
                }

                try {
                    Calendar c = Calendar.getInstance();
                    //if (BuildConfig.DEBUG) Log.d("NotificationService", "c		: " + c.toString());
					if (BuildConfig.DEBUG) Log.d("NotificationService", "milli	: " + c.getTimeInMillis());
					if (BuildConfig.DEBUG) Log.d("NotificationService", "zone	: " + c.getTimeZone());
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    if (notiData.getHour() == hour) {
                        int min = c.get(Calendar.MINUTE);
                        if (notiData.getMinute() == min) {
                            if (notiData.getDays(c.get(Calendar.DAY_OF_WEEK) - 1)) {
                                if (isNoti) {
                                    handler.sendEmptyMessage(0);
                                }
                                isNoti = false;
                            } else {
                                isNoti = true;
                                Thread.sleep(1000);
                            }
                        } else {
                            isNoti = true;
                            Thread.sleep(1000);
                        }
                    } else {
                        isNoti = true;
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}