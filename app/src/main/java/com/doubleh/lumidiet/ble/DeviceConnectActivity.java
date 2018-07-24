package com.doubleh.lumidiet.ble;


import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doubleh.lumidiet.BaseActivity;
import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.ConnectFragment;
import com.doubleh.lumidiet.MainActivity;
import com.doubleh.lumidiet.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceConnectActivity extends BaseActivity {
    String TAG = "DeviceConnectActivity";

    String LUMI_IDENTIFIER = "Lumi";
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 4000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private UUID[] uuids = null;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    ListView mLeDeviceListView;

    /*private View decorView;
    private int uiOption;
    private int currentApiVersion;*/
    private RelativeLayout connect_Btn;
    private Fragment nowContentFragment;

    private boolean mScanning;

    private ScanCallback mScanCallback;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_connect);

        connectActivity = this;

        RelativeLayout mRLayout = (RelativeLayout) findViewById(R.id.device_connect_activity);
        mRLayout.setScaleX(getScale());
        mRLayout.setScaleY(getScale());

        if (BuildConfig.DEBUG) Log.d(TAG, "scale: " + getScale());

        if (Build.VERSION.SDK_INT >= 21) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (BuildConfig.DEBUG) Log.i("callbackType", String.valueOf(callbackType));
                    if (BuildConfig.DEBUG) Log.i("result", result.toString());
                    BluetoothDevice btDevice = result.getDevice();

                    mLeDeviceListAdapter.addDevice(btDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        if (BuildConfig.DEBUG) Log.i("ScanResult - Results", sr.toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    if (BuildConfig.DEBUG) Log.e("Scan Failed", "Error Code: " + errorCode);
                }
            };
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (BuildConfig.DEBUG) Log.i("onLeScan", device.toString());

                            if (device.getName() == null || !device.getName().contains(LUMI_IDENTIFIER))
                                return;

                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
        }

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        findViewById(R.id.device_connect_rlayout).setVisibility(View.INVISIBLE);

        setNowContentFragment();
        hideUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //setDisplay(R.id.device_connect_activity);
    }

    public void connectCancel() {
        ((MainActivity) MainActivity.mainContext).disconnectBelt(true);
        //finish();
    }

    void setNowContentFragment() {
        nowContentFragment = new ConnectFragment();
        getFragmentManager().beginTransaction().replace(R.id.device_connect_flayout, nowContentFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        {
            mLeDeviceListView = (ListView) findViewById(R.id.device_connect_listview);

            // Initializes list view adapter.
            mLeDeviceListAdapter = new LeDeviceListAdapter();
            //setListAdapter(mLeDeviceListAdapter);
            mLeDeviceListView.setAdapter(mLeDeviceListAdapter);
            mLeDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "setOnItemClickListener");
                    mLeDeviceListAdapter.setSelectedHolder(position);
                }
            });

            connect_Btn = (RelativeLayout) findViewById(R.id.device_connect_btn_connect);
            connect_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNowContentFragment();
                    BluetoothDevice device = mLeDeviceListAdapter.getSelectedDevice();
                    ((MainActivity) MainActivity.mainContext).setDevice(device.getName(), device.getAddress());
                    //finish();
                }
            });

            if (Build.VERSION.SDK_INT >= 21) {

                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BLEDefine.RBL_SERVICE_UUID)).build();
                filters.add(filter);
            }
            else{
                List<UUID> list = new ArrayList<UUID>();
                list.add(UUID.fromString(BLEDefine.RBL_SERVICE_UUID));
                uuids =  new UUID[list.size()];
                /*for (int i = 0; i < list.size(); i++) {
                    uuids[i] = list.get(i);
                }*/
                list.toArray(uuids);
            }
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                    invalidateOptionsMenu();
                    // 3초가 지난 후에 행할 행동
                    if (mLeDeviceListAdapter.getCount() <= 0) {
                        // 검색된 디바이스가 0개다.. 맞나
                        if (BuildConfig.DEBUG) Log.d(TAG, "cannot find bluetooth device");
                        ((MainActivity) MainActivity.mainContext).setDevice("", "");
                        //getFragmentManager().beginTransaction().remove(nowContentFragment).commit();
                        //nowContentFragment = null;
                        finish();
                    }
                    else if (mLeDeviceListAdapter.getCount() == 1) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "find just 1ea");
                        BluetoothDevice device = mLeDeviceListAdapter.getDevice(0);
                        ((MainActivity) MainActivity.mainContext).setDevice(device.getName(), device.getAddress());
                        //finish();
                    }
                    else {
                        if (BuildConfig.DEBUG) Log.d(TAG, "find 2ea more");
                        getFragmentManager().beginTransaction().remove(nowContentFragment).commit();
                        nowContentFragment = null;
                        findViewById(R.id.device_connect_rlayout).setVisibility(View.VISIBLE);
                    }
                }
            }, SCAN_PERIOD);
            mScanning = true;
            if (Build.VERSION.SDK_INT < 21) {
                //mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);    // error, cannot scan
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            mScanning = false;
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<ViewHolder> mItems = new ArrayList<>();
        //private LayoutInflater mInflator;
        private ViewHolder selectedHolder = null;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            //mInflator = DeviceConnectActivity.this.getLayoutInflater();
        }
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                ViewHolder item = new ViewHolder();
                item.isSelected = false;
                mItems.add(item);
            }
        }
        public void setSelectedHolder(int position) {
            if (selectedHolder == null) {
                selectedHolder = mItems.get(position);
                selectedHolder.isSelected = true;
                selectedHolder.checkImageView.setActivated(selectedHolder.isSelected);
            } else {
                ViewHolder nowSelectedHolder = mItems.get(position);
                if (selectedHolder == nowSelectedHolder)
                    return;
                selectedHolder.isSelected = false;
                selectedHolder.checkImageView.setActivated(selectedHolder.isSelected);
                nowSelectedHolder.isSelected = true;
                nowSelectedHolder.checkImageView.setActivated(nowSelectedHolder.isSelected);
                selectedHolder = nowSelectedHolder;
            }
            notifyDataSetChanged();
        }
        public BluetoothDevice getSelectedDevice() {
            for (int i = 0; i < mItems.size(); i++) {
                if (mItems.get(i).isSelected) {
                    return mLeDevices.get(i);
                }
            }
            return null;
        }
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        public void clear() {
            mItems.clear();
            mLeDevices.clear();
        }
        @Override
        public int getCount() {
            return mItems.size();
        }
        @Override
        public Object getItem(int i) {
            return mItems.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = mItems.get(i);
            // General ListView optimization code.
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_listitem_device, null);
                //viewHolder = new ViewHolder();
                //viewHolder.deviceAddress = (TextView) view.findViewById(R.id.list_item_de);
                //view.setTag(viewHolder);
                //mItems.add(viewHolder);
            } else {
                //viewHolder = (ViewHolder) view.getTag();
                //viewHolder = mItems.get(i);
            }
            viewHolder.deviceName = (TextView) view.findViewById(R.id.list_item_device_name);
            viewHolder.checkImageView = (ImageView) view.findViewById(R.id.list_item_check);

            BluetoothDevice device = mLeDevices.get(i);

            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            }
            else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            //viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.checkImageView.setActivated(viewHolder.isSelected);
            return view;
        }
    }
    static class ViewHolder {
        TextView deviceName;
        ImageView checkImageView;
        //TextView deviceAddress;
        boolean isSelected;
    }
}
