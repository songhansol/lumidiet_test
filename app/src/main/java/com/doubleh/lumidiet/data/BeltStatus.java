package com.doubleh.lumidiet.data;

/**
 * Created by user-pc on 2016-10-06.
 */

public class BeltStatus {
    String userId;
    long usingTime;
    int useMemory, totalMemory;
    short usingStatus, mode, battery;

    /**
     * set user id(master key) value
     * @param userId    master key
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    /**
     * set using status
     * @param usingStatus    착용 : 1, 미착용 : 2
     */
    public void setUsingStatus(short usingStatus) {
        this.usingStatus = usingStatus;
    }
    /**
     * set using time
     * @param usingTime    second
     */
    public void setUsingTime(long usingTime) {
        this.usingTime = usingTime;
    }
    /**
     * set belt mode
     * @param mode    LED is always on, 0: motor off, 1: motor on  and off, 2: motor always on
     */
    public void setMode(short mode) {
        this.mode = mode;
    }
    /**
     * set battery
     * @param battery    0~100(%)
     */
    public void setBattery(short battery) {
        this.battery = battery;
    }
    /**
     * set used memory
     * @param useMemory    used memory size
     */
    public void setUseMemory(int useMemory) {
        this.useMemory = useMemory;
    }
    /**
     * set total memory size
     * @param totalMemory    total memory size
     */
    public void setTotalMemory(int totalMemory) {
        this.totalMemory = totalMemory;
    }
    /**
     * get using status
     * 착용 : 1, 미착용 : 2
     */
    public short getUsingStatus() {
        return this.usingStatus;
    }
    /**
     * get using time
     * second
     */
    public long getUsingTime() { return this.usingTime; }
    /**
     * get belt mode
     * LED is always on, 0: motor off, 1: motor on  and off, 2: motor always on
     */
    public short getMode() { return this.mode; }
    /**
     * get battery status
     * 0~100(%)
     */
    public short getBattery() { return this.battery; }
    /**
     * get used memory
     */
    public int getUseMemory() { return this.useMemory; }

    /**
     * get total memory
     * @return
     */
    public int getTotalMemory() { return this.totalMemory; }
}
