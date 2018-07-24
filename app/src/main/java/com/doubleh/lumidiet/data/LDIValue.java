package com.doubleh.lumidiet.data;

/**
 * Created by user-pc on 2016-10-06.
 */

public class LDIValue {
    String userId, deviceId, rawLDIValue;
    long measureTime;
    int ldiValue, age, height, weight, gender;
    short type, mode;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setMeasureTime(long measureTime) {
        this.measureTime = measureTime;
    }

    public long getMeasureTime() {
        return measureTime;
    }

    public void setType(short type) {
        this.type = type;
    }

    public short getType() {
        return type;
    }

    public void setMode(short mode) {
        this.mode = mode;
    }

    public short getMode() {
        return mode;
    }

    public void setLdiValue(int ldiValue) {
        this.ldiValue = ldiValue;
    }

    public int getLdiValue() {
        return ldiValue;
    }

    public void setRawLDIValue(String rawLDIValue) {
        this.rawLDIValue = rawLDIValue;
    }

    public String getRawLDIValue() {
        return rawLDIValue;
    }

	public void setAge(int age) {
		this.age = age;
	}

	public int getAge() {
		return age;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getGender() {
		return gender;
	}
}
