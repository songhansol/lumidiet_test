package com.doubleh.lumidiet.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.data.BeltHistory;
import com.doubleh.lumidiet.data.HelpData;
import com.doubleh.lumidiet.data.LDIValue;
import com.doubleh.lumidiet.data.NoticeData;

import java.util.ArrayList;

/**
 * Created by user-pc on 2016-10-07.
 */

public class DatabaseManager {
    private static DatabaseManager instance = null;
    public static final String DATABASE_NAME = "Lumi.db";
    public static final int DATABASE_VERSION = 4;

    long validUsingTime = 15 * 60;
    String TAG = "DatabaseManager";
    SQLiteDatabase db;
    MySQLiteOpenHelper helper;

    protected DatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        helper = new MySQLiteOpenHelper(context, name, factory, version);
    }

    static public DatabaseManager getInstance() {
        if (instance == null) {
            if (BuildConfig.DEBUG) Log.i("DatabaseManager", "DatabaseManager is not initialized");
        }
        return instance;
    }

    static public DatabaseManager getInstance(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager(context, name, factory, version);
                }
            }
        }
        return instance;
    }

    /**
     * insert into belt_history table
      * @param masterkey
     * @param type
     * @param mode
     * @param start_time
     * @param end_time
     * @param device_id
     */
    public boolean insertBeltHistory(int masterkey, int type, int mode, long start_time, long end_time, String device_id) {
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("masterkey", masterkey);
        values.put("type", type);
        values.put("mode", mode);
        values.put("start_time", start_time);
        values.put("end_time", end_time);
        values.put("device_id", device_id);

        long returnVal = db.insert("belt_history", null, values);

        if (returnVal < 0) {
            return false;
        }

        return true;
    }

    public boolean insertBeltHistory(BeltHistory history) {
        return insertBeltHistory(Integer.parseInt(history.getUserId()), history.getType(), history.getMode(),
                history.getStartTime(), history.getEndTime(), history.getDeviceId());
    }

    public boolean insertBeltHistory(ArrayList<BeltHistory> histories) {
        for (BeltHistory history : histories) {
            if (insertBeltHistory(history)) {

            }
            else {
                return false;
            }
        }
        return true;
    }

    public ArrayList<BeltHistory> selectBeltHistory(int masterkey) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from belt_history where masterkey=");
        query.append(masterkey);
        query.append(" order by start_time desc");
        query.append(";");

        ArrayList<BeltHistory> histories = new ArrayList<>();

        Cursor c = db.rawQuery(query.toString(), null);
        while (c.moveToNext()) {
            BeltHistory history = new BeltHistory();
            history.setUserId(Integer.toString(c.getInt(0)));
            history.setType(c.getShort(1));
            history.setMode(c.getShort(2));
            history.setStartTime(c.getLong(3));
            history.setEndTime(c.getLong(4));
            history.setDeviceId(c.getString(5));

            histories.add(history);
        }

        c.close();

        return histories;
    }

    /**
     * get belt usage history, where start time between end time
     * @param masterkey
     * @param startTime    standard : usage start time
     * @param endTime      standard : usage start time
     * @return
     */
    public ArrayList<BeltHistory> selectBeltHistory(int masterkey, long startTime, long endTime) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from belt_history where masterkey=");
        query.append(masterkey);
        query.append(" and start_time >= ");
        query.append(startTime);
        query.append(" and start_time < ");
        query.append(endTime);
        query.append(" order by start_time asc");
        query.append(";");

        ArrayList<BeltHistory> histories = new ArrayList<>();

        Cursor c = db.rawQuery(query.toString(), null);
        while (c.moveToNext()) {
            BeltHistory history = new BeltHistory();
            history.setUserId(Integer.toString(c.getInt(0)));
            history.setType(c.getShort(1));
            history.setMode(c.getShort(2));
            history.setStartTime(c.getLong(3));
            history.setEndTime(c.getLong(4));
            history.setDeviceId(c.getString(5));

            histories.add(history);
        }

        c.close();

        return histories;
    }

    public int selectCountBeltHistory(int masterkey, long startTime) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from belt_history where masterkey=");
        query.append(masterkey);
        query.append(" and start_time = ");
        query.append(startTime);
        query.append(";");

        Cursor c = db.rawQuery(query.toString(), null);

        int ret = c.getCount();
        c.close();

        return ret;
    }

    public int selectCountBeltHistory(int masterkey, long startTime, long endTime) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from belt_history where masterkey=");
        query.append(masterkey);
        query.append(" and start_time >= ");
        query.append(startTime);
        query.append(" and start_time < ");
        query.append(endTime);
        query.append(";");

        Cursor c = db.rawQuery(query.toString(), null);

        int ret = c.getCount();
        c.close();

        return ret;
    }

    public int selectCountValidBeltHistory(int masterkey, long startTime, long endTime) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from belt_history where masterkey=");
        query.append(masterkey);
        query.append(" and start_time >= ");
        query.append(startTime);
        query.append(" and start_time < ");
        query.append(endTime);
        query.append(";");

        Cursor c = db.rawQuery(query.toString(), null);

        ArrayList<BeltHistory> histories = new ArrayList<>();

        while (c.moveToNext()) {
            BeltHistory history = new BeltHistory();
            history.setUserId(Integer.toString(c.getInt(0)));
            history.setType(c.getShort(1));
            history.setMode(c.getShort(2));
            history.setStartTime(c.getLong(3));
            history.setEndTime(c.getLong(4));
            history.setDeviceId(c.getString(5));

            histories.add(history);
        }

        c.close();

        long useTime = 0;

        for (BeltHistory history : histories) {
            useTime += history.getUsingTime();
        }

        int ret = 0;

        if (useTime > validUsingTime)
            ret++;

        /*for (int i = 0; i < histories.size();) {
            if (histories.size() <= 0)
                break;

            if (histories.get(i).getUsingTime() < validUsingTime)
                histories.remove(i);
            else {
                i++;
            }
        }

        int ret = histories.size();*/

        histories.clear();
        histories = null;

        return ret;
    }

    public void deleteBeltHistories(String masterkey) {
        db = helper.getReadableDatabase();

        db.delete("belt_history", "masterkey = ?", new String[]{masterkey});
    }

    /**
     * insert into ldi history table
     * @param masterkey
     * @param type
     * @param mode
     * @param ldi_value
     * @param time
     * @param device_id
     */
    public boolean insertLDIHistory(int masterkey, int type, int mode, int ldi_value, long time, String device_id) {
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("masterkey", masterkey);
        values.put("type", type);
        values.put("mode", mode);
        values.put("ldi_value", ldi_value);
        values.put("time", time);
        values.put("device_id", device_id);

        long returnVal = db.insert("ldi_history", null, values);

        if (returnVal < 0) {
            return false;
        }

        return true;
    }

    public boolean insertLDIHistory(LDIValue ldiValue) {
        return insertLDIHistory(Integer.parseInt(ldiValue.getUserId()), ldiValue.getType(), ldiValue.getMode(),
                ldiValue.getLdiValue(), ldiValue.getMeasureTime(), ldiValue.getDeviceId());
    }

    public boolean insertLDIHistory(ArrayList<LDIValue> ldiValues) {
        for (LDIValue ldiValue : ldiValues) {
            if (insertLDIHistory(Integer.parseInt(ldiValue.getUserId()), ldiValue.getType(), ldiValue.getMode(),
                    ldiValue.getLdiValue(), ldiValue.getMeasureTime(), ldiValue.getDeviceId())){

            } else {
                return false;
            }
        }

        return true;
    }

    public ArrayList<LDIValue> selectLDIHistory(String masterkey, long startTime, long endTime) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from ldi_history where masterkey=");
        query.append(masterkey);
        query.append(" and time >= ");
        query.append(startTime);
        query.append(" and time < ");
        query.append(endTime);
        query.append(" order by time asc");
        query.append(";");

        ArrayList<LDIValue> histories = new ArrayList<>();

        Cursor c = db.rawQuery(query.toString(), null);
        while (c.moveToNext()) {
            LDIValue history = new LDIValue();
            history.setUserId(Integer.toString(c.getInt(0)));
            history.setType(c.getShort(1));
            history.setMode(c.getShort(2));
            history.setLdiValue(c.getInt(3));
            history.setMeasureTime(c.getLong(4));
            history.setDeviceId(c.getString(5));

            histories.add(history);
        }

        c.close();

        return histories;
    }

    public LDIValue selectLDIHistory(String masterkey, long measureTime) {
        //if (BuildConfig.DEBUG) Log.d(TAG, "selectLDIHistory(String masterkey, long measureTime)");

        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from ldi_history where masterkey=");
        query.append(masterkey);
        query.append(" and time = ");
        query.append(measureTime);
        query.append(";");

        LDIValue history = new LDIValue();

        Cursor c = db.rawQuery(query.toString(), null);

        if (c.getCount() > 1) {
            if (BuildConfig.DEBUG) Log.d(TAG, "error");
        } else if (c.getCount() <= 0) {
            if (BuildConfig.DEBUG) Log.d(TAG, "error, no have data");
        }

        while (c.moveToNext()) {
            history.setUserId(Integer.toString(c.getInt(0)));
            history.setType(c.getShort(1));
            history.setMode(c.getShort(2));
            history.setLdiValue(c.getInt(3));
            history.setMeasureTime(c.getLong(4));
            history.setDeviceId(c.getString(5));
        }

        c.close();

        return history;
    }

    public ArrayList<LDIValue> selectLDIHistory(String masterkey) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from ldi_history where masterkey=");
        query.append(masterkey);
        query.append(" order by time desc");
        query.append(";");

        ArrayList<LDIValue> histories = new ArrayList<>();

        Cursor c = db.rawQuery(query.toString(), null);
        while (c.moveToNext()) {
            LDIValue history = new LDIValue();
            history.setUserId(Integer.toString(c.getInt(0)));
            history.setType(c.getShort(1));
            history.setMode(c.getShort(2));
            history.setLdiValue(c.getInt(3));
            history.setMeasureTime(c.getLong(4));
            history.setDeviceId(c.getString(5));

            histories.add(history);
        }

        c.close();

        return histories;
    }

    public void deleteLDIHistories(String masterkey) {
        db = helper.getReadableDatabase();

        db.delete("ldi_history", "masterkey = ?", new String[]{masterkey});
    }

    /**
     * insert into notice_history table
     * @param idx          index (is primary key)
     * @param masterkey    masterkey (user id)
     * @param title        title
     * @param body         body
     * @param time         create time
     * @param country      country code
     * @param isRead       is read or not
     * @return
     */
    public boolean insertNoticeHistory(int idx, int masterkey, String title, String body, long time, String country, boolean isRead, String language) {
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("idx", idx);
        values.put("masterkey", masterkey);
        values.put("title", title);
        values.put("body", body);
        values.put("time", time);
        values.put("country", country);
        values.put("isread", isRead ? 1 : 0);
        values.put("language", language);

        long returnVal = db.insert("notice_history", null, values);

        if (returnVal < 0) {
            return false;
        }

        return true;
    }

    public boolean insertNoticeHistory(NoticeData noticeData, int masterkey) {
        return insertNoticeHistory(noticeData.getKey(), masterkey, noticeData.getTitle(),
                noticeData.getBody(), noticeData.getTime(), noticeData.getCountry(), noticeData.getRead(), noticeData.getLanguage());
    }

    public boolean insertNoticeHistory(ArrayList<NoticeData> noticeDatas, int masterkey) {
        for (NoticeData noticeData : noticeDatas) {
            if (insertNoticeHistory(noticeData, masterkey)) {
            }
            else {
                return false;
            }
        }

        return true;
    }

    public ArrayList<NoticeData> selectNoticeHistory(String masterkey, String language) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from notice_history where masterkey=");
        query.append(masterkey);
        query.append(" and language=");
        query.append("'");
        query.append(language);
        query.append("'");
        query.append(" order by idx desc");
        query.append(";");

        ArrayList<NoticeData> histories = new ArrayList<>();

        Cursor c = db.rawQuery(query.toString(), null);
        while (c.moveToNext()) {
            NoticeData history = new NoticeData();
            history.setKey(c.getInt(0));
            //history.setUserId(c.getShort(1));
            history.setTitle(c.getString(2));
            history.setBody(c.getString(3));
            history.setTime(c.getLong(4));
            history.setCountry(c.getString(5));
            history.setRead(c.getInt(6) == 0 ? false : true);
            //history.setCountry(c.getString(7));

            histories.add(history);
        }

        c.close();

        return histories;
    }

    public int countNoticeNotRead(String masterkey, String language) {
		int count = 0;

		db = helper.getReadableDatabase();

		StringBuilder query = new StringBuilder();
		query.append("select count(*) from notice_history where masterkey=");
		query.append(masterkey);
		query.append(" and language=");
		query.append("'");
		query.append(language);
		query.append("'");
		query.append(" and isread = 0;");

		Cursor c = db.rawQuery(query.toString(), null);
		c.moveToFirst();
		count = c.getInt(0);
		//count = c.getCount();
		c.close();

		return count;
	}

    public boolean updateNoticeHistory(String masterkey, int idx, boolean isRead) {
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("isread", isRead ? 1 : 0);
        int returnVal = db.update("notice_history", values, "masterkey = ? AND idx = ?", new String[]{masterkey, Integer.toString(idx)});

        if (returnVal == 0) {
            return false;
        }

        return true;
    }

    public void deleteNoticeHistories(String masterkey) {
        if (BuildConfig.DEBUG) Log.d(TAG, "deleteNoticeHistories()");

        db = helper.getReadableDatabase();

        db.delete("notice_history", "masterkey = ?", new String[]{masterkey});
    }

    public boolean insertHelpHistory(int masterkey, String title, String body, String reply, long time, boolean isRead, String contact, int idx) {
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("masterkey", masterkey);
        values.put("title", title);
        values.put("body", body);
        values.put("reply", reply);
        values.put("time", time);
        values.put("isread", isRead ? 1 : 0);
        values.put("contact", contact);
		values.put("idx", idx);

        long returnVal = db.insert("help_history", null, values);

        if (returnVal < 0) {
            return false;
        }

        return true;
    }

    public boolean insertHelpHistory(HelpData helpData, int masterkey) {
        return insertHelpHistory(masterkey, helpData.getTitle(),
                helpData.getBody(), helpData.getReply(), helpData.getTime(), helpData.getRead(), helpData.getContact(), helpData.getIdx());
    }

    public boolean insertHelpHistory(ArrayList<HelpData> helpDatas, int masterkey) {
        for (HelpData helpData : helpDatas) {
            if (insertHelpHistory(helpData, masterkey)) {
            }
            else {
                return false;
            }
        }

        return true;
    }

    public ArrayList<HelpData> selectHelpHistory(String masterkey) {
        db = helper.getReadableDatabase();

        StringBuilder query = new StringBuilder();
        query.append("select * from help_history where masterkey=");
        query.append(masterkey);
        query.append(" order by time desc");
        query.append(";");

        ArrayList<HelpData> histories = new ArrayList<>();

        Cursor c = db.rawQuery(query.toString(), null);
        while (c.moveToNext()) {
            HelpData history = new HelpData();
            history.setTitle(c.getString(1));
            history.setBody(c.getString(2));
            history.setReply(c.getString(3));
            history.setTime(c.getLong(4));
            history.setRead(c.getInt(5) == 0 ? false : true);
			history.setContact(c.getString(6));
			history.setIdx(c.getInt(7));

            histories.add(history);
        }

        c.close();

        return histories;
    }

    public boolean updateHelpHistory(String masterkey, long time, boolean isRead) {
        db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("isread", isRead ? 1 : 0);
        int returnVal = db.update("help_history", values, "masterkey = ? AND time = ?", new String[]{masterkey, Long.toString(time)});

        if (returnVal == 0) {
            return false;
        }

        return true;
    }

    public void deleteHelpHistories(String masterkey) {
        db = helper.getReadableDatabase();

        db.delete("help_history", "masterkey = ?", new String[]{masterkey});
    }

    // SQLiteOpenHelper
    protected class MySQLiteOpenHelper extends SQLiteOpenHelper {
        public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            {
                String sql = "create table if not exists belt_history(masterkey integer not null," +
                        " type integer, mode integer, start_time integer not null," +
                        " end_time integer not null, device_id text not null);";
                db.execSQL(sql);
            }
            {
                String sql = "create table if not exists ldi_history(masterkey integer not null," +
                        " type integer, mode integer, ldi_value integer not null," +
                        " time integer not null, device_id text not null);";
                db.execSQL(sql);
            }
            {
                String sql = "create table if not exists notice_history(idx integer not null," +
                        " masterkey integer not null, title text, body text," +
                        " time integer not null, country text, isread integer, language text);";
                db.execSQL(sql);
            }
            {
                String sql = "create table if not exists help_history(masterkey integer not null," +
                        " title text, body text, reply text, time integer not null, isread integer, contact text, idx integer);";
                db.execSQL(sql);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*String sql = "drop table if exists belt_history";
            db.execSQL(sql);

            sql = "drop table if exists ldi_history";
            db.execSQL(sql);

            sql = "drop table if exists notice_history";
            db.execSQL(sql);

            sql = "drop table if exists help_history";
            db.execSQL(sql);

            onCreate(db);*/

            if (BuildConfig.DEBUG) Log.d(TAG, "oldVersion: "+ oldVersion);
            if (BuildConfig.DEBUG) Log.d(TAG, "newVersion: "+ newVersion);
            if (oldVersion != newVersion) {
				if (newVersion == 2) {
					String sql = "alter table notice_history add language text";
					db.execSQL(sql);

					//sql = "update notice_history set language='KO' where language is null";
					ContentValues values = new ContentValues();
					values.put("language", "KO");
					int returnVal = db.update("notice_history", values, "", null);
					if (BuildConfig.DEBUG) Log.d(TAG, "returnVal: " + returnVal);
				} else if (newVersion == 3) {
					String sql = "alter table help_history add contact text";
					db.execSQL(sql);

					//sql = "update notice_history set language='KO' where language is null";
					if (BuildConfig.DEBUG) Log.d(TAG, "help_history table update success");
				} else if (newVersion == 4) {
					String sql = "alter table help_history add idx integer";
					db.execSQL(sql);

					//sql = "update notice_history set language='KO' where language is null";
					if (BuildConfig.DEBUG) Log.d(TAG, "help_history table update success (add idx column)");
                }
            }
        }
    }
}