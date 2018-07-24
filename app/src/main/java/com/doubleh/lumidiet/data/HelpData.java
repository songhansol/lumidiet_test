package com.doubleh.lumidiet.data;

/**
 * Created by user-pc on 2016-10-10.
 */

public class HelpData {
    String title, body, reply, contact;
    long time;
    int idx;
    boolean isRead;

    public void setTime(long time) {
        this.time = time;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public long getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getReply() {
        return reply;
    }

    public boolean getRead() {
        return isRead;
    }

	public String getContact() {
		return contact;
	}

	public int getIdx() {
		return idx;
	}
}
