package com.doubleh.lumidiet;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuideInfoFragment extends Fragment {

    public GuideInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guide_info, container, false);

        RelativeLayout jump_Btn = (RelativeLayout) view.findViewById(R.id.guide_btn_jump);
        jump_Btn.setEnabled(true);
        jump_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GuideActivity) getActivity()).startMainActivity();
            }
        });

        RelativeLayout read_Btn = (RelativeLayout) view.findViewById(R.id.guide_btn_read);
        read_Btn.setEnabled(true);
        read_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				((GuideActivity) getActivity()).setFragment(1);
            }
        });

        return view;
    }
}
