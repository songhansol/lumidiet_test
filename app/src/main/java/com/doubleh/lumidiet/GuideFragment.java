package com.doubleh.lumidiet;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuideFragment extends Fragment {

    String TAG = "GuideFragment";
    RelativeLayout layout;
    WebView browser;

    public GuideFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_content, container, false);

        float density = ((MainActivity) getActivity()).getDensity();

        view.findViewById(R.id.guide_btn_ok).setVisibility(View.GONE);

        layout = (RelativeLayout) view.findViewById(R.id.guide_rlayout);
        layout.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorFFFFFFFF));

        {
            // add prev button
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = (int) (35 * density);
            params.leftMargin = (int) (10.25 * density);


            ImageButton prev_Btn = new ImageButton(getActivity());
            prev_Btn.setBackground(getResources().getDrawable(R.drawable.common_prev_btn));
            prev_Btn.setMinimumHeight(0);
            prev_Btn.setMinimumWidth(0);
            prev_Btn.setLayoutParams(params);

            layout.addView(prev_Btn);
            prev_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                }
            });
        }
        {
            // add top title text view
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = (int)(38 * density);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);

            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(params);
            textView.setText(R.string.product_instructions);
            textView.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorFF2F2F2F));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);

            layout.addView(textView);
        }

        browser = (WebView) view.findViewById(R.id.guide_webview);
        browser.setWebViewClient(new WebViewClient());
        browser.getSettings().setJavaScriptEnabled(true);
        browser.clearCache(true);
        browser.loadUrl(getString(R.string.product_info_filename));
        //browser.loadUrl("http://www.dfworkshop.net/webgl/demo/index.html");

        return view;
    }

    @Override
    public void onDestroyView() {
        if (browser != null) {
            ViewGroup viewGroup = (ViewGroup) browser.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(browser);
            }
            browser.clearHistory();
            browser.clearCache(true);
            browser.loadUrl("about:blank");
            browser.removeAllViews();
            browser.destroy();
            browser = null;
        }
        super.onDestroyView();
    }
}
