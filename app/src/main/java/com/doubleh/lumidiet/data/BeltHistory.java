package com.doubleh.lumidiet.data;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Comparator;

/**
 * Created by user-pc on 2016-10-06.
 */

public class BeltHistory {
    String userId, deviceId, ldiRawData;
    long startTime, endTime;
    int ldiValue;
    short type, mode;

    /**
     * set user id
     * @param userId    master key
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * set ldi type
     * @param type    1 : auto, 2 : manual, 81 : user terminated & auto, 82 : user terminated & manual
     */
    public void setType(short type) {
        this.type = type;
    }

    /**
     * set belt mode
     * @param mode    LED always on, 0: motor off, 1: motor on&off, 2: motor always on
     */
    public void setMode(short mode) {
        this.mode = mode;
    }

    /**
     * set start time
     * @param startTime    when start using belt, millisecond
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * set end time
     * @param endTime    when end using belt, millisecond
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * set ldi value
     * @param ldiValue    set ldi value(but, now can't get this data from belt)
     */
    public void setLdiValue(int ldiValue) {
        this.ldiValue = ldiValue;
    }

    /**
     * set ldi low data
     * @param ldiRawData    belt response using history data (0x31)
     */
    public void setLDIRawData(String ldiRawData) {
        this.ldiRawData = ldiRawData;
    }

    /**
     * set device id
     * @param deviceId    belt identifier
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public short getType() {
        return type;
    }

    public short getMode() {
        return mode;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getLdiValue() {
        return ldiValue;
    }

    public String getLDIRawData() {
        return ldiRawData;
    }

    public String getDeviceId() {
        return deviceId;
    }

    /**
     * return to using time, millisecond
     * @return
     */
    public long getUsingTime() {
        return getEndTime() - getStartTime();
    }
}
