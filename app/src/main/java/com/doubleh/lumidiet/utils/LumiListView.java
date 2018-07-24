package com.doubleh.lumidiet.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by user-pc on 2016-10-07.
 * PopupWindow 내에서 사용하기 위한 ListView
 * PopupWindow 의 focusable을 false로하면 ListView의 아이템이 선택되지 않는 점을 수정하기 위해 제작
 * reference : http://stackoverflow.com/questions/30778295/android-listviews-onitemclicklistener-not-working-on-non-focusable-popupwindow
 */

public class LumiListView extends ListView {
    public LumiListView(Context context) {
        super(context);
    }

    public LumiListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LumiListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LumiListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean hasFocus() {
        //return super.hasFocus();
        return true;
    }

    @Override
    public boolean isFocused() {
        //return super.isFocused();
        return true;
    }

    @Override
    public boolean hasWindowFocus() {
        //return super.hasWindowFocus();
        return true;
    }
}
