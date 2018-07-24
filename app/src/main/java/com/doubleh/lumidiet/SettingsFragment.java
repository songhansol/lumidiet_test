package com.doubleh.lumidiet;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.doubleh.lumidiet.pedometer.SensorListenerService;
import com.doubleh.lumidiet.utils.NumberFormatUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		// prev button
		v.findViewById(R.id.btn_prev).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).hideUI();
				((MainActivity) getActivity()).setPrevContentFragmentLayout();
			}
		});
		// set zoom
		v.findViewById(R.id.btn_set_zoom).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_SET_MAGNIFIER);
			}
		});
		// set alarm
		v.findViewById(R.id.btn_set_alarm).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_SET_ALARM);
			}
		});
		// set bluetooth auto connect
		v.findViewById(R.id.btn_bluetooth_set).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_SET_BLUETOOTH);
			}
		});

		if (!((MainActivity) getActivity()).getLanguage().equalsIgnoreCase("ko")) {
			v.findViewById(R.id.btn_bluetooth_set).setVisibility(View.GONE);
		}

		if (getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).getBoolean(BaseActivity.KEY_STORAGE, true)) {
			v.findViewById(R.id.btn_permission_gallery).setActivated(true);
		} else {
			v.findViewById(R.id.btn_permission_gallery).setActivated(false);
		}

		if (getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).getBoolean(BaseActivity.KEY_CAMERA, true)) {
			v.findViewById(R.id.btn_permission_camera).setActivated(true);
		} else {
			v.findViewById(R.id.btn_permission_camera).setActivated(false);
		}

		// permission storage
		v.findViewById(R.id.btn_permission_gallery_set).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean now = v.findViewById(R.id.btn_permission_gallery).isActivated();
				now = !now;
				v.findViewById(R.id.btn_permission_gallery).setActivated(now);
				getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).edit().putBoolean(BaseActivity.KEY_STORAGE, now).commit();
			}
		});
		// permission camera
		v.findViewById(R.id.btn_permission_camera_set).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean now = v.findViewById(R.id.btn_permission_camera).isActivated();
				now = !now;
				v.findViewById(R.id.btn_permission_camera).setActivated(now);
				getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).edit().putBoolean(BaseActivity.KEY_CAMERA, now).commit();
			}
		});

		// permission storage
		v.findViewById(R.id.btn_permission_gallery).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean now = v.isActivated();
				now = !now;
				v.setActivated(now);
				getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).edit().putBoolean(BaseActivity.KEY_STORAGE, now).commit();
			}
		});
		// permission camera
		v.findViewById(R.id.btn_permission_camera).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean now = v.isActivated();
				now = !now;
				v.setActivated(now);
				getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).edit().putBoolean(BaseActivity.KEY_CAMERA, now).commit();
			}
		});

        final View btnPedometer = v.findViewById(R.id.btnPedometer);

        final SharedPreferences prefs = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        boolean isEnabled = prefs.getBoolean("isEnabled", true);
        btnPedometer.setActivated(isEnabled);
        int goal = prefs.getInt("goal", 5000);
        final TextView tvGoalSteps = v.findViewById(R.id.tvGoalSteps);
        tvGoalSteps.setTextColor(Color.parseColor(isEnabled ? "#10a0e3":"#a7a8a9"));
        tvGoalSteps.setText(NumberFormatUtil.commaedNumber(goal));

        final View btnGoalSteps = v.findViewById(R.id.btnGoalSteps);

        btnGoalSteps.setEnabled(isEnabled);
        tvGoalSteps.setEnabled(isEnabled);

        tvGoalSteps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				long currentTimeMillis = System.currentTimeMillis();
				if (currentTimeMillis - prevOnClickMillis > IGNORE_TIME) {
					prevOnClickMillis = currentTimeMillis;
					showNumberPickerForGoalSteps(tvGoalSteps, prefs);
				}
			}
		});

        btnPedometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				togglePedometer(btnPedometer, btnGoalSteps, tvGoalSteps, prefs);
            }
        });

        btnGoalSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				long currentTimeMillis = System.currentTimeMillis();
				if (currentTimeMillis - prevOnClickMillis > IGNORE_TIME) {
					prevOnClickMillis = currentTimeMillis;
					showNumberPickerForGoalSteps(tvGoalSteps, prefs);
				}
            }
        });

		v.findViewById(R.id.rlPedometerContainer).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				togglePedometer(btnPedometer, btnGoalSteps, tvGoalSteps, prefs);
			}
		});

		return v;
	}

    private final int IGNORE_TIME = 500;
	private long prevOnClickMillis;

	private void togglePedometer(View btnPedometer, View btnGoalSteps, TextView tvGoalSteps, SharedPreferences prefs) {
		boolean isEnabled = btnPedometer.isActivated();

		if (!isEnabled) {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (sensor == null) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
                        .setMessage(R.string.no_sensor_explain)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(final DialogInterface dialogInterface) {
                            }
                        }).setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
                return;
            }
        }

		isEnabled = !isEnabled;
		btnPedometer.setActivated(isEnabled);
		btnGoalSteps.setEnabled(isEnabled);
		tvGoalSteps.setEnabled(isEnabled);
		tvGoalSteps.setTextColor(Color.parseColor(isEnabled ? "#10a0e3":"#a7a8a9"));
		prefs.edit().putBoolean("isEnabled", isEnabled).apply();
		((MainActivity) getActivity()).setPedometerOffUI(isEnabled);
        Intent sensorListenerServiceIntent = new Intent(getActivity(), SensorListenerService.class);
        if(!isEnabled) {
			getActivity().stopService(sensorListenerServiceIntent);
		} else {
            getActivity().startService(sensorListenerServiceIntent);
        }
	}

	private void showNumberPickerForGoalSteps(final TextView tvGoalSteps, final SharedPreferences prefs) {
        Context context = getActivity();
        final NumberPicker picker = new NumberPicker(context);
        picker.setWrapSelectorWheel(false);
        picker.setMinValue(0);
        picker.setMaxValue(29);

        int step = (int) NumberFormatUtil.integerFromCommaedNumber(tvGoalSteps.getText().toString());
        step = step/1000-1;
        picker.setValue(step);
        String [] displayValues = new String[30];
        for(int i = 0; i < 30; ++i) {
            displayValues[i] = NumberFormatUtil.commaedNumber((i+1) * 1000);
        }
        picker.setDisplayedValues(displayValues);

        FrameLayout layout = new FrameLayout(context);
        layout.addView(picker, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        new AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String goalSteps = NumberFormatUtil.commaedNumber((picker.getValue()+1) * 1000);
                        tvGoalSteps.setText(goalSteps);
                        int goal = (int) NumberFormatUtil.integerFromCommaedNumber(goalSteps);
                        if (goal > 0) {
                            prefs.edit().putInt("goal", goal).apply();
                            ((MainActivity)getActivity()).refreshLDIUsageFragment();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

}
