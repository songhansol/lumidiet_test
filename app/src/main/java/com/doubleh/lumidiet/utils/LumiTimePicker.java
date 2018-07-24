package com.doubleh.lumidiet.utils;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.R;

import java.lang.reflect.Field;

/**
 * Created by byj05 on 2016-11-30.
 */

public class LumiTimePicker extends TimePicker {

    public LumiTimePicker(Context context) {
        super(context);
        init();
    }

    public LumiTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LumiTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void numberPickerTextColor( NumberPicker $v, int $c ){
        for(int i = 0, j = $v.getChildCount() ; i < j; i++){
            View t0 = $v.getChildAt(i);
            if( t0 instanceof EditText ){
                try{
                    if (BuildConfig.DEBUG) Log.d("LumiTimePicker", "getResources().getDisplayMetrics().densityDpi: " + getResources().getDisplayMetrics().densityDpi);
                    Field t1 = $v.getClass() .getDeclaredField( "mSelectorWheelPaint" );
                    t1.setAccessible(true);
                    ((Paint)t1.get($v)) .setColor($c);
                    ((Paint) t1.get($v)).setTextSize(getResources().getDisplayMetrics().densityDpi / 160.0f * 30.0f);
                    ((EditText)t0) .setTextColor($c);
                    ((EditText)t0) .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                    $v.invalidate();
                }catch(Exception e){}
            }
        }
    }

    void textColor(View $v, int $color ){

        if($v instanceof NumberPicker) numberPickerTextColor( (NumberPicker)$v, $color );

        else if($v instanceof TextView) ((TextView)$v).setTextColor($color);

        else if($v instanceof ViewGroup) {
            ViewGroup t0 = (ViewGroup) $v;
            for( int i = 0, j = t0.getChildCount() ; i < j ; i++ )
                textColor( t0.getChildAt(i), $color );
        }
    }

    private void init() {
        textColor(this, ContextCompat.getColor(getContext(), R.color.colorFF2F2F2F));
        /*LinearLayout linearLayout = (LinearLayout)getChildAt(0);
        if (linearLayout != null) {
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                Log.d("LumiTimpicker", "count1: "+linearLayout.getChildCount());
                if (i != 1) {
                    NumberPicker numberPicker = (NumberPicker)linearLayout.getChildAt(i);
                    if (numberPicker != null) {
                        for (int j = 0; j < numberPicker.getChildCount(); j++) {
                            Log.d("LumiTimpicker", "count2: "+numberPicker.getChildCount());
                            EditText editText = (EditText)numberPicker.getChildAt(j);
                            editText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFF2F2F2F));
                            editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                        }
                    }
                }
            }
        }*/
    }

    @Override
    public void setOnDragListener(OnDragListener l) {
        super.setOnDragListener(l);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            ViewParent p = getParent();
            if (p != null)
                p.requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }
}
