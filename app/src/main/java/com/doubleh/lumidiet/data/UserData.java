package com.doubleh.lumidiet.data;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by user-pc on 2016-09-23.
 */

public class UserData {
    private String masterKey="null", userID, email, country, facebookID, name;
    private int sex, age, height, weight;
    private boolean isMailChk, isFacebook;

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setMailChk(boolean isMailChk) {
        this.isMailChk = isMailChk;
    }

    public void setFacebook(boolean isFacebook) {
        this.isFacebook = isFacebook;
    }

    public void setCountry(String country) { this.country = country; }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasterKey() {
        return this.masterKey;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getEmail() {
        return this.email;
    }

    public int getSex() {
        return this.sex;
    }

    public int getAge() {
        return this.age;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWeight() {
        return this.weight;
    }

    public boolean getMailChk() {
        return this.isMailChk;
    }

    public boolean getFacebook() {
        return this.isFacebook;
    }

    public String getCountry() { return this.country; }

    public String getFacebookID() {
        return facebookID;
    }

    public String getName() {
        return name;
    }
}
