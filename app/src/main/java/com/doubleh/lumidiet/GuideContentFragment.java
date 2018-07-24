package com.doubleh.lumidiet;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class GuideContentFragment extends Fragment {

    WebView browser;

    public GuideContentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guide_content, container, false);

        // add code
        RelativeLayout ok_Btn = (RelativeLayout) view.findViewById(R.id.guide_btn_ok);
        ok_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GuideActivity) getActivity()).startMainActivity();
            }
        });

        browser = (WebView) view.findViewById(R.id.guide_webview);
        browser.setWebViewClient(new WebViewClient());
        browser.getSettings().setJavaScriptEnabled(true);
        browser.clearCache(true);
        browser.loadUrl(getString(R.string.product_info_filename));

        return view;
    }

}
