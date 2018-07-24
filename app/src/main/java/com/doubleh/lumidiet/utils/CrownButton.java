package com.doubleh.lumidiet.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.R;

/**
 * Created by byj05 on 2016-12-12.
 */

public class CrownButton extends Button {
    private static final int[] STATE_ONE = {R.attr.crown_state_one};
    private static final int[] STATE_TWO = {R.attr.crown_state_two};
    public static final int GOOD = 0;
    public static final int GREAT = 1;
    public static final int EXCELLENT = 2;
    //private int mCrownState = 0;
    private boolean mStateOne = false;
    private boolean mStateTwo = false;

    public CrownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCrownState(int crownState) {
        switch (crownState) {
            case GOOD:
                mStateOne = false;
                mStateTwo = false;
                break;
            case GREAT:
                mStateOne = true;
                mStateTwo = false;
                break;
            case EXCELLENT:
                mStateOne = true;
                mStateTwo = true;
                break;
        }
        invalidate();
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (BuildConfig.DEBUG) Log.e("CrownButton", "onCreateDrawableState(int extraSpace)");

        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);

        if (mStateOne) {
            mergeDrawableStates(drawableState, STATE_ONE);
        }
        if (mStateTwo) {
            mergeDrawableStates(drawableState, STATE_TWO);
        }

        return drawableState;
        //return super.onCreateDrawableState(extraSpace);
    }
}
