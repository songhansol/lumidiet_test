package com.doubleh.lumidiet.data;

/**
 * Created by user-pc on 2016-10-10.
 */

public class NoticeData {
    String title, body, country, language;
    long time;
    int key;
    boolean isRead;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getKey() {
        return key;
    }

    public long getTime() {
        return time;
    }

    public String getBody() {
        return body;
    }

    public String getCountry() {
        return country;
    }

    public String getTitle() {
        return title;
    }

    public boolean getRead() {
        return isRead;
    }

    public String getLanguage() {
        return language;
    }
}
