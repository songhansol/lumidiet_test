package com.doubleh.lumidiet;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.doubleh.lumidiet.utils.OnSingleClickListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetMagnifierFragment extends Fragment {


	public SetMagnifierFragment() {
		// Required empty public constructor
	}

	ImageButton magSetting_Btn;
	boolean magMode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_set_magnifier, container, false);

		// add code

		view.findViewById(R.id.btn_prev).setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				MainActivity mainActivity = (MainActivity) getActivity();
				//mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
				mainActivity.setPrevContentFragmentLayout();
			}
		});

		magMode = getActivity().getSharedPreferences(((MainActivity) getActivity()).getUserData().getMasterKey(), Context.MODE_PRIVATE).getBoolean(MainActivity.PREFERENCES_MAGNIFIER, false);
		magSetting_Btn = (ImageButton) view.findViewById(R.id.btn_mag_activation);
		magSetting_Btn.setActivated(magMode);

		magSetting_Btn.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				magMode = !magMode;

				magSetting_Btn.setActivated(magMode);
				((MainActivity) getActivity()).setMagMode(magMode);
			}
		});

		return view;
	}


}
