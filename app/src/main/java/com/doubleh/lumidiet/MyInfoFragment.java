package com.doubleh.lumidiet;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doubleh.lumidiet.data.UserData;
import com.doubleh.lumidiet.utils.JSONNetworkManager;
import com.doubleh.lumidiet.utils.OnSingleClickListener;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.doubleh.lumidiet.BaseActivity.KEY_BADGE_COUNT;
import static com.doubleh.lumidiet.BaseActivity.Preferences_LOGIN;
import static com.doubleh.lumidiet.BaseActivity.Preferences_LOGIN_AUTO;
import static com.doubleh.lumidiet.MainActivity.FACEBOOK_PHOTO_POSTFIX;
import static com.doubleh.lumidiet.MainActivity.FACEBOOK_PHOTO_PREFIX;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyInfoFragment extends Fragment implements View.OnClickListener {
    String TAG = "MyInfoFragment";

    private static final int PICK_FROM_CAMERA	= 0;
    private static final int PICK_FROM_ALBUM	= 1;
    private static final int CROP_FROM_CAMERA	= 2;

    private Uri mImageCaptureUri;

	PopupWindow popup;
	RelativeLayout popup_back_Layer;

    //Bitmap profileBitmap;
    ImageView profileView;
    private View mView;
    private Button autoLogin_Btn;
    private boolean isAuto = false;

	enum PopupState {
		NONE, PERMISSION
	};

	PopupState state = PopupState.NONE;

    public MyInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_my_info, container, false);

		popup_back_Layer = (RelativeLayout) mView.findViewById(R.id.progress_layer);

        // add code
        ImageButton prev_Btn = (ImageButton) mView.findViewById(R.id.myinfo_btn_prev);
        prev_Btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.setContentFrameLayout(MainActivity.CONTENT_HOME);
                mainActivity.setUserProfile();
            }
        });

        RelativeLayout account_Btn = (RelativeLayout) mView.findViewById(R.id.myinfo_btn_confirm);
        account_Btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (((MainActivity)getActivity()).getUserData().getFacebook()) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.confirm_not_support), Toast.LENGTH_SHORT);
                    ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
                    toast.show();
                    return;
                }

                ((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_MY_INFO_ACCOUNT);
            }
        });

        RelativeLayout info_Btn = (RelativeLayout) mView.findViewById(R.id.myinfo_btn_myinfo);
        info_Btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_MY_INFO_INFO);
            }
        });

        RelativeLayout change_Btn = (RelativeLayout) mView.findViewById(R.id.myinfo_btn_pw_change);
        change_Btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (((MainActivity)getActivity()).getUserData().getFacebook()) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.pw_change_not_support), Toast.LENGTH_SHORT);
                    ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
                    toast.show();
                    return;
                }

                ((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_MY_INFO_PW_CHANGE);
            }
        });

        RelativeLayout leave_Btn = (RelativeLayout) mView.findViewById(R.id.myinfo_btn_leave);
        leave_Btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                ((MainActivity) getActivity()).setContentFrameLayout(MainActivity.CONTENT_MY_INFO_MEMBER_LEAVE);
            }
        });

        Button camera_Btn = (Button) mView.findViewById(R.id.myinfo_btn_camera);
        /*camera_Btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivity(cameraIntent);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        });*/
        camera_Btn.setOnClickListener(this);

        profileView = (ImageView) mView.findViewById(R.id.myinfo_img_profile);

        if (((MainActivity)getActivity()).getUserData().getFacebook()) {
            // facebook 인 경우에는 카메라 버튼 비활성화
            camera_Btn.setVisibility(View.INVISIBLE);

            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(FACEBOOK_PHOTO_PREFIX);
            urlBuilder.append(((MainActivity)getActivity()).getUserData().getFacebookID());
            urlBuilder.append(FACEBOOK_PHOTO_POSTFIX);

            if (BuildConfig.DEBUG) Log.d(TAG, "profile image url: " + urlBuilder.toString());
            new MainActivity.DownloadImageTask(profileView).execute(urlBuilder.toString());
        } else {
            String path = mView.getContext().getSharedPreferences(((MainActivity) getActivity()).getUserData().getMasterKey(),
                    MODE_PRIVATE).getString(MainActivity.Preferences_PROFILE_IMAGE_PATH, null);
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    Bitmap thumbnail = BitmapFactory.decodeFile(file.getAbsolutePath());
                    profileView.setImageBitmap(thumbnail);
                }
            }
        }

        isAuto = getActivity().getSharedPreferences(MainActivity.Preferences_LOGIN, MODE_PRIVATE)
                .getBoolean(MainActivity.Preferences_LOGIN_AUTO, false);

        autoLogin_Btn = (Button) mView.findViewById(R.id.myinfo_btn_auto_login);
        autoLogin_Btn.setActivated(isAuto);

        autoLogin_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MainActivity)getActivity()).getUserData().getFacebook()) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.auto_login_not_support), Toast.LENGTH_SHORT);
                    ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
                    toast.show();
                    return;
                }

                isAuto = !isAuto;

                getActivity().getSharedPreferences(MainActivity.Preferences_LOGIN, MODE_PRIVATE)
                        .edit().putBoolean(MainActivity.Preferences_LOGIN_AUTO, isAuto).commit();
                autoLogin_Btn.setActivated(isAuto);

				sendPushData();

                if (!isAuto) {
                    //getActivity().getSharedPreferences(((MainActivity) getActivity()).getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_BADGE_COUNT, 0).commit();
                    Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
                    intent.putExtra("badge_count", 0);
                    //앱의  패키지 명
                    intent.putExtra("badge_count_package_name", getActivity().getPackageName());
                    // AndroidManifest.xml에 정의된 메인 activity 명
                    intent.putExtra("badge_count_class_name", "com.doubleh.lumidiet.LoginActivity");
                    getActivity().sendBroadcast(intent);
                }
            }
        });

        mView.findViewById(R.id.myinfo_btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MainActivity)getActivity()).getUserData().getFacebook()) {
                    LoginManager.getInstance().logOut();
                } else {
                    // 자동 로그인 해제
                    getActivity().getSharedPreferences(MainActivity.Preferences_LOGIN, MODE_PRIVATE)
                            .edit().putString(MainActivity.Preferences_PW, "").commit();
					getActivity().getSharedPreferences(MainActivity.Preferences_LOGIN, MODE_PRIVATE)
							.edit().putBoolean(MainActivity.Preferences_LOGIN_AUTO, false).commit();
                    // badge count reset
                    getActivity().getSharedPreferences(((MainActivity) getActivity()).getUserData().getMasterKey(), MODE_PRIVATE).edit().putInt(KEY_BADGE_COUNT, 0).commit();
                    Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
                    intent.putExtra("badge_count", 0);
                    //앱의  패키지 명
                    intent.putExtra("badge_count_package_name", getActivity().getPackageName());
                    // AndroidManifest.xml에 정의된 메인 activity 명
                    intent.putExtra("badge_count_class_name", "com.doubleh.lumidiet.LoginActivity");
                    getActivity().sendBroadcast(intent);
                }
				isAuto = false;
				sendPushData();

                ((MainActivity)getActivity()).resetUserData();
                getActivity().finish();
            }
        });

        return mView;
    }

	private void sendPushData() {
		try {
			JSONObject json = new JSONObject();
			json.put("masterkey",  ((MainActivity) getActivity()).getUserData().getMasterKey());
			json.put("push_token", FirebaseInstanceId.getInstance().getToken());
			json.put("uuid", Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));
			json.put("login_status", isAuto ? "on" : "off");
			json.put("os_info", "android");
            if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko"))
                json.put("kind", "kr");
            else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ja"))
                json.put("kind", "jp");
            else if (Locale.getDefault().getLanguage().equalsIgnoreCase("zh"))
                json.put("kind", "cn");
            else
                json.put("kind", "us");

			new JSONNetworkManager(JSONNetworkManager.PUSH_INFO, json) {
                @Override
                public void errorCallback(int status) {
                    super.errorCallback(status);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finishAffinity();
                                    System.runFinalization();
                                    System.exit(0);
                                    dialog.dismiss();
                                }
                            };

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.network_err_msg))
                                    .setPositiveButton(getString(R.string.ok), exitListener)
                                    .setCancelable(false)
                                    .show();
                        }
                    });
                }

				@Override
				public void responseCallback(JSONObject responseJson) {
					try {
						if (responseJson.getInt("result") == 0) {
							if (BuildConfig.DEBUG) Log.d(TAG, "Push Data send failed");
						} else {
							if (BuildConfig.DEBUG) Log.d(TAG, "Push Data send success");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}.sendJson();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
     * 카메라에서 이미지 가져오기
     */
    private void doTakePhotoAction() {
    /**
     * 참고 해볼곳
     * http://2009.hfoss.org/Tutorial:Camera_and_Gallery_Demo
     * http://stackoverflow.com/questions/1050297/how-to-get-the-url-of-the-captured-image
     * http://www.damonkohler.com/2009/02/android-recipes.html
     * http://www.firstclown.us/tag/android/
     */

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        //intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    /**
     * 앨범에서 이미지 가져오기
     */
    private void doTakeAlbumAction() {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onClick(View v) {
		if (getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).getBoolean(BaseActivity.KEY_STORAGE, true)
				&&getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).getBoolean(BaseActivity.KEY_CAMERA, true)) {
			showGetProfilePopup();
		}
		else {
			// permission popup
			showPermissionPopup();
		}
    }

    void showGetProfilePopup() {
		DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doTakePhotoAction();
			}
		};
		DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				doTakeAlbumAction();
			}
		};
		DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		};

		new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.camera_dialog_title))
				.setPositiveButton(getString(R.string.photo_shoot), cameraListener)
				.setNeutralButton(getString(R.string.get_from_album), albumListener)
				.setNegativeButton(getString(R.string.cancel), cancelListener)
				.show();
	}

    void showPermissionPopup() {
		state = PopupState.PERMISSION;
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.layout_popup_permission_request, null);
		popup_back_Layer.setVisibility(View.VISIBLE);
		popup = new PopupWindow(layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
		popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

		popup.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				getActivity().dispatchTouchEvent(event);
				return false;
			}
		});

		layout.findViewById(R.id.popup_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				state = PopupState.NONE;
			}
		});

		layout.findViewById(R.id.popup_ok).setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				popup.dismiss();
				popup = null;
				popup_back_Layer.setVisibility(View.INVISIBLE);
				getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).edit().putBoolean(BaseActivity.KEY_STORAGE, true).commit();
				getActivity().getSharedPreferences(BaseActivity.Preferences_PERMISSION, Context.MODE_PRIVATE).edit().putBoolean(BaseActivity.KEY_CAMERA, true).commit();
				showGetProfilePopup();
				state = PopupState.NONE;
			}
		});
	}

	public void reshowPopup() {
		switch (state) {
			case NONE:
				break;
			case PERMISSION:
				showPermissionPopup();
				break;
		}
	}

	public void hidePopup() {
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
	}

	public View getMagView() {
		if (popup != null) {
			return popup.getContentView();
		} else {
			return getView();
		}
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (BuildConfig.DEBUG) Log.d(TAG, "camera result");

        if(resultCode != RESULT_OK)
        {
            return;
        }

        switch(requestCode)
        {
            case CROP_FROM_CAMERA:
            {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
                // 임시 파일을 삭제합니다. - 파일 자체를 옮김
                /*final Bundle extras = data.getExtras();

                if(extras != null)
                {
                    Bitmap photo = extras.getParcelable("data");
                    profileView.setImageBitmap(photo);
                }*/

                // JPEG 으로 변경하여 저장 및 이미지 설정
                if (data != null) {
                    onCaptureImageResult(data);
                }

                // 임시 파일 삭제
                /*File f = new File(mImageCaptureUri.getPath());
                if(f.exists())
                {
                    f.delete();
                }*/

                break;
            }

            case PICK_FROM_ALBUM:
            {
                // 이후의 처리가 카메라와 같으므로 일단  break 없이 진행합니다.
                // 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.

				Uri selectedUri = data.getData();

				File f = new File(getPath(getActivity(), selectedUri));

				String url = Environment.getExternalStorageDirectory() + "/" + "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
				copyFile(f, url);

				//mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
				mImageCaptureUri = Uri.fromFile(new File(url));
            }

            case PICK_FROM_CAMERA:
            {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
                // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

				if (BuildConfig.DEBUG) Log.d(TAG, "mImageCaptureUri: " + mImageCaptureUri.toString());

                intent.putExtra("outputX", 250);
                intent.putExtra("outputY", 250);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
				// 원본을 crop image 로 change
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_CAMERA);

                break;
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
		/*if (data.getExtras().get("data") == null) {
			// message
			Toast toast = Toast.makeText(getActivity(), getString(R.string.memory_err), Toast.LENGTH_SHORT);
			((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
			toast.show();
			return;
		}*/

		if (mImageCaptureUri.getPath() == null) {
			// message
			Toast toast = Toast.makeText(getActivity(), getString(R.string.memory_err), Toast.LENGTH_SHORT);
			((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
			toast.show();
			return;
		}

        String dirPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getActivity().getPackageName() + "/";
        String path = dirPath + System.currentTimeMillis() + ".jpg";

        File dirChk = new File(dirPath);
        if (!dirChk.exists())
            dirChk.mkdirs();

		File destination = new File(mImageCaptureUri.getPath());
		destination.renameTo(new File(path));

		profileView.setImageBitmap(BitmapFactory.decodeFile(path));

        {   // 이전 이미지 삭제
            String prevPath = mView.getContext().getSharedPreferences(((MainActivity) getActivity()).getUserData().getMasterKey(),
                    MODE_PRIVATE).getString(MainActivity.Preferences_PROFILE_IMAGE_PATH, null);
            if (prevPath != null) {
                File file = new File(prevPath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        mView.getContext().getSharedPreferences(((MainActivity)getActivity()).getUserData().getMasterKey(), MODE_PRIVATE)
                .edit().putString(((MainActivity)getActivity()).Preferences_PROFILE_IMAGE_PATH, path).commit();
    }

	private boolean copyFile(File file , String save_file){
		if (file != null && file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				FileOutputStream newfos = new FileOutputStream(save_file);
				int readCount = 0;
				byte[] buffer = new byte[1024];
				while((readCount = fis.read(buffer, 0, 1024)) != -1) {
					newfos.write(buffer, 0, readCount);
				}
				newfos.close();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else {
			if (BuildConfig.DEBUG) Log.d(TAG, "copy failed");
			return false;
		}
	}

	// reference : http://stackoverflow.com/questions/19985286/convert-content-uri-to-actual-path-in-android-4-4
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}
