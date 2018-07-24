package com.doubleh.lumidiet.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.transition.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by user-pc on 2016-09-23.
 */

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItems = new ArrayList<>();

    public ListViewAdapter() {}

    @Override
    public int getCount() {
        return listViewItems.size();
    }

    View temp;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_listview_item, parent, false);
        }

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItems.get(position);

        // 아이템 내 각 위젯에 데이터 반영

        // 제목이 길면 줄이기
        StringBuilder title = new StringBuilder(listViewItem.getTitle());
        if (title.length() > 18) {
            title.delete(18, title.length());
            title.append("...");
        }
        StringBuilder body;
        ((TextView) convertView.findViewById(R.id.list_item_title)).setText(title);
        if (!listViewItem.getRead()) {
            ((TextView) convertView.findViewById(R.id.list_item_title)).setTypeface(null, Typeface.BOLD);
        } else {
            ((TextView) convertView.findViewById(R.id.list_item_title)).setTypeface(null, Typeface.NORMAL);
        }
        ((TextView) convertView.findViewById(R.id.list_item_date)).setText(listViewItem.getDate());
        if (listViewItem.getReply() == null || listViewItem.getReply().equals("")) {

            if (listViewItem.getContact() == null || listViewItem.getContact().equals("")) {
                body = new StringBuilder(listViewItem.getBody());
            } else {
                body = new StringBuilder();
                body.append(parent.getContext().getString(R.string.contact));
                //body.append("\n");
				body.append("<br>");
                body.append(listViewItem.getContact());
                //body.append("\n\n");
				body.append("<br>");
				body.append("<br>");
                body.append(listViewItem.getBody());
            }
        } else {
			if (listViewItem.getContact() == null || listViewItem.getContact().equals("")) {
				body = new StringBuilder(listViewItem.getBody());
			} else {
				body = new StringBuilder();
				body.append(parent.getContext().getString(R.string.contact));
                body.append("<br>");
				body.append(listViewItem.getContact());
				body.append("<br>");
				body.append("<br>");
				body.append(listViewItem.getBody());
			}
			body.append("<br>");
			body.append("<br>");
            body.append(parent.getContext().getString(R.string.reply));
			body.append("<br>");
            body.append(listViewItem.getReply());
        }
        String bodyStr = body.toString().replace("¨", "\"");
        if (BuildConfig.DEBUG) Log.d("check", bodyStr);

        temp = convertView.findViewById(R.id.list_item_body);

        /*Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                LevelListDrawable d = new LevelListDrawable();
                //Drawable empty = getResources().getDrawable(R.drawable.ic_launcher);
                Drawable empty = new BitmapDrawable();
                d.addLevel(0, 0, empty);
                d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());

                new LoadImage().execute(source, d, temp);

                return d;
            }
        };

        ((TextView) convertView.findViewById(R.id.list_item_body)).setText(Html.fromHtml(bodyStr, imageGetter, null));*/
        ((TextView) convertView.findViewById(R.id.list_item_body)).setText(Html.fromHtml(bodyStr));
        ((TextView) convertView.findViewById(R.id.list_item_body)).setMovementMethod(LinkMovementMethod.getInstance());
        convertView.findViewById(R.id.list_item_body).setFocusable(false);

        if (listViewItem.getVisible()) {
            ((TextView) convertView.findViewById(R.id.list_item_title)).setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorFF00BD47));
        } else {
            ((TextView) convertView.findViewById(R.id.list_item_title)).setTextColor(ContextCompat.getColor(parent.getContext(), R.color.colorFF2F2F2F));
        }
        convertView.findViewById(R.id.list_item_body_layout).setVisibility(listViewItem.getVisible() ? View.VISIBLE : View.GONE);
        convertView.findViewById(R.id.list_item_switch).setActivated(listViewItem.getVisible());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return listViewItems.get(position);
    }

    public void setBodyVisible(int position) {
        ListViewItem item = listViewItems.get(position);
        item.setVisible(!item.getVisible());

        notifyDataSetChanged();
    }

    public void setReadNotice(int position) {
        ListViewItem item = listViewItems.get(position);
        item.setRead(true);

        notifyDataSetChanged();
    }

    public void addItem(String title, String date, String body, boolean isRead) {
        ListViewItem item = new ListViewItem();
        item.setTitle(title);
        item.setDate(date);
        item.setBody(body);
        item.setRead(isRead);

        listViewItems.add(item);
    }

    public void addItem(String title, String date, String body, String reply, String contact, boolean isRead) {
        ListViewItem item = new ListViewItem();
        item.setTitle(title);
        item.setDate(date);
        item.setBody(body);
        item.setReply(reply);
		item.setContact(contact);
        item.setRead(isRead);

        listViewItems.add(item);
    }

    public void clearItem() {
        listViewItems.clear();
    }

    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;
        private TextView tv;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];
            tv = (TextView) params[2];
            if (BuildConfig.DEBUG) Log.d("ListViewAdapter", "doInBackground " + source);
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (BuildConfig.DEBUG) Log.d("ListViewAdapter", "onPostExecute drawable " + mDrawable);
            if (BuildConfig.DEBUG) Log.d("ListViewAdapter", "onPostExecute bitmap " + bitmap);
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(bitmap);
                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                mDrawable.setLevel(1);
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                CharSequence t = tv.getText();
                tv.setText(t);
            }
        }
    }
}