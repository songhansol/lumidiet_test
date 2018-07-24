package com.doubleh.lumidiet;


import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class DietInfoFragment extends Fragment {
	String TAG = "DietInfoFragment";
    WebView browser;
	//private final int ASSETS_VERSION = 0;
	//private final String KEY_ASSETS = "k_a";
	//private final long MIN_FREE_SPACE = 40 * 1024 * 1024;

    public DietInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diet_info, container, false);

        // add code
        browser = (WebView) view.findViewById(R.id.dietinfo_webview);
        browser.setWebViewClient(new WebViewClient());
        browser.getSettings().setJavaScriptEnabled(true);

        browser.clearCache(true);

		// kit kat 이하 file protocol 미동작에 따른 방법 변화
		/*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			StringBuilder filename = new StringBuilder();

			filename.append("file://");
			filename.append(getActivity().getFilesDir().getAbsolutePath());
			filename.append(File.separator);
			filename.append(getString(R.string.diet_info_filename2));

			browser.loadUrl(filename.toString());

			Log.d(TAG, "filename: "+filename.toString());
		} else {
			browser.loadUrl(getString(R.string.diet_info_filename));
		}*/

		browser.loadUrl(getString(R.string.diet_info_filename));

		browser.addJavascriptInterface(new AndroidBridge(), "Lumidiet");


		// 저장소 공간 부족
		/*if (getInternalMemorySize() < MIN_FREE_SPACE) {

		}*/
		/*if (getPreferences(MODE_PRIVATE).getInt(KEY_ASSETS, -1) < ASSETS_VERSION) {
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_ASSETS, ASSETS_VERSION).commit();
		}*/
		// asset file copy, under lollipop version
		/*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			try {
				AssetManager assetMgr = getActivity().getAssets();
				String[] rootList = assetMgr.list("");

				for(String element : rootList) {
					copyAssetAll(element);
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}*/

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

	private void goHome() {
		getView().playSoundEffect(SoundEffectConstants.CLICK);
        ((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_HOME);
    }

	// javascript 에서 호출하는 방법
	// window.Lumidiet.getJavascriptMessage("exit");
    private class AndroidBridge {
        @JavascriptInterface
        public void getJavascriptMessage (final String msg) {
			if (msg.equalsIgnoreCase("exit")) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						goHome();
					}
				});
			}
        }
    }

	/*public void copyAssetAll(String srcPath) {
		AssetManager assetMgr = getActivity().getAssets();
		String assets[] = null;
		try {
			assets = assetMgr.list(srcPath);
			if (assets.length == 0) {
				copyFile(srcPath);
			} else {
				String destPath = getActivity().getFilesDir().getAbsolutePath() + File.separator + srcPath;

				File dir = new File(destPath);
				if (!dir.exists())
					dir.mkdir();
				for (String element : assets) {
					copyAssetAll(srcPath + File.separator + element);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void copyFile(String srcFile) {
		AssetManager assetMgr = getActivity().getAssets();

		InputStream is = null;
		OutputStream os = null;
		try {
			String destFile = getActivity().getFilesDir().getAbsolutePath() + File.separator + srcFile;

			is = assetMgr.open(srcFile);
			os = new FileOutputStream(destFile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}
			is.close();
			os.flush();
			os.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	//출처: http://evnt-hrzn.tistory.com/23 [사건의 지평선]

	/** 사용가능한 내장 메모리 크기를 가져온다
	 *  return unit is byte */
	/*private long getInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSizeLong();
		long availableBlocks = stat.getAvailableBlocksLong();

		return availableBlocks * blockSize;
	}*/

	/** 사용가능한 외장 메모리 크기를 가져온다 */
	/*private long getExternalMemorySize() {
		if (isStorage(true) == true) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return -1;
		}
	}*/

	/** 외장메모리 sdcard 사용가능한지에 대한 여부 판단 */
	/*private boolean isStorage(boolean requireWriteAccess) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else if (!requireWriteAccess &&
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}*/
	//출처: http://mainia.tistory.com/664 [녹두장군 - 상상을 현실로]
}
