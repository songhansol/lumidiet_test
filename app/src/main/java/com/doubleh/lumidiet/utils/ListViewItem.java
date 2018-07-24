package com.doubleh.lumidiet.utils;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by user-pc on 2016-09-23.
 */

public class ListViewItem {
    private String title;
    private String date;
    private String body;
    private String reply = null, contact = null;
    private boolean isVisible = false, isRead = true;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

	public void setContact(String contact) {
		this.contact = contact;
	}

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDate() {
        return this.date;
    }

    public String getBody() {
        return this.body;
    }

    public boolean getVisible() {
        return isVisible;
    }

    public String getReply() {
        return reply;
    }

	public String getContact() {
		return contact;
	}

	public boolean getRead() {
        return isRead;
    }
}
