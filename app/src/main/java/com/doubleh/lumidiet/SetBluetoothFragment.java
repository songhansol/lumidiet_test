package com.doubleh.lumidiet;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static android.content.Context.MODE_PRIVATE;
import static com.doubleh.lumidiet.BaseActivity.KEY_AUTO;
import static com.doubleh.lumidiet.BaseActivity.Preferences_BLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetBluetoothFragment extends Fragment {


	public SetBluetoothFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_set_bluetooth, container, false);

		v.findViewById(R.id.btn_prev).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).setPrevContentFragmentLayout();
			}
		});

		v.findViewById(R.id.btn_ble_auto_activation).setActivated(getActivity().getSharedPreferences(Preferences_BLE, MODE_PRIVATE).getBoolean(KEY_AUTO, true));

		v.findViewById(R.id.btn_ble_auto_activation).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean now = v.isActivated();//getActivity().getSharedPreferences(Preferences_BLE, MODE_PRIVATE).getBoolean(KEY_AUTO, true);
				now = !now;
				getActivity().getSharedPreferences(Preferences_BLE, MODE_PRIVATE).edit().putBoolean(KEY_AUTO, now).commit();
				v.setActivated(now);
			}
		});

		return v;
	}

}
