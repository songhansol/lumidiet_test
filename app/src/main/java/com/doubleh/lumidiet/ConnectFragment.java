package com.doubleh.lumidiet;


import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.doubleh.lumidiet.ble.DeviceConnectActivity;
import com.doubleh.lumidiet.utils.OnSingleClickListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectFragment extends Fragment {

    ImageView connect_Img;
    AnimationDrawable mAnimationDrawable;
    String TAG = "ConnectFragment";


    public ConnectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        connect_Img = (ImageView) view.findViewById(R.id.connect_img_connect);
        connect_Img.setBackgroundResource(R.drawable.connect_animation_list);
        mAnimationDrawable = (AnimationDrawable) connect_Img.getBackground();

        if(mAnimationDrawable != null)
        {
            mAnimationDrawable.start();
        }
        else
        {
            if (BuildConfig.DEBUG) Log.d(TAG, "mAnimationDrawable is null pointer");
        }

        view.findViewById(R.id.connect_cancel).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ((DeviceConnectActivity) getActivity()).connectCancel();
            }
        });

        return view;
    }
}
