package com.doubleh.lumidiet.ble;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.doubleh.lumidiet.BuildConfig;
import com.doubleh.lumidiet.utils.HexEditor;
import com.doubleh.lumidiet.utils.LittleEndianByteHandler;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;


/**
 * Created by steve on 2016. 7. 31..
 */
public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	protected BluetoothGattCharacteristic lumiBLEChracteristic = null;
	private int mConnectionState = STATE_DISCONNECTED;


	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (BuildConfig.DEBUG)
				Log.i("onConnectionStateChange", "Status: " + status + " new State : " + newState);
			switch (newState) {
				case BluetoothProfile.STATE_CONNECTED: {
					if (BuildConfig.DEBUG) Log.i("gattCallback", "STATE_CONNECTED");
					gatt.discoverServices();
					mConnectionState = STATE_CONNECTED;
					MessageEvent event = new MessageEvent(Constant.EVENT_TYPE.BLE_STATE_CONNECTED);
					EventBus.getDefault().post(event);
					break;
				}
				case BluetoothProfile.STATE_DISCONNECTED: {
					if (BuildConfig.DEBUG) Log.e("gattCallback", "STATE_DISCONNECTED");
					mConnectionState = STATE_DISCONNECTED;
					MessageEvent event = new MessageEvent(Constant.EVENT_TYPE.BLE_STATE_DISCONNECTED);
					EventBus.getDefault().post(event);
					break;
				}
				default:
					if (BuildConfig.DEBUG) Log.e("gattCallback", "STATE_OTHER");
					MessageEvent event = new MessageEvent(Constant.EVENT_TYPE.BLE_STATE_OTHER);
					EventBus.getDefault().post(event);
			}

		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			List<BluetoothGattService> services = gatt.getServices();
			if (BuildConfig.DEBUG) Log.i("onServicesDiscovered", services.toString());
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "GATT_SUCCESS");
				MessageEvent event = new MessageEvent(Constant.EVENT_TYPE.GATT_SERVICES_DISCOVERED);
				EventBus.getDefault().post(event);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic
												 characteristic, int status) {
			if (BuildConfig.DEBUG) Log.i("onCharacteristicRead", characteristic.toString());

			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(characteristic);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic
												  characteristic, int status) {
			if (BuildConfig.DEBUG) Log.i("onCharacteristicWrite", characteristic.toString());

			if (status == BluetoothGatt.GATT_SUCCESS) {
				MessageEvent event = new MessageEvent(Constant.EVENT_TYPE.GATT_CAN_READ);
				EventBus.getDefault().post(event);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			if (BuildConfig.DEBUG) Log.i("onCharacteristicChanged", characteristic.toString());
			broadcastUpdate(characteristic);
		}
	};

	private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
		if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "broadcastUpdate "+characteristic.getUuid().toString());
		if (characteristic.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_CHAR_RX_UUID)) {
			// For all other profiles, writes the data formatted in HEX.
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for (byte byteChar : data) {
					stringBuilder.append(String.format("%02X", byteChar));
				}
				if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "GATT_SUCCESS");
				MessageEvent event = new MessageEvent(Constant.EVENT_TYPE.DATA_AVAILABLE, stringBuilder.toString());
				EventBus.getDefault().post(event);
			}
		} else if (characteristic.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_BATTERY_LEVEL_UUID)) {
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for (byte byteChar : data) {
					stringBuilder.append(String.format("%02X", byteChar));
				}
				if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "GATT_SUCCESS");
				MessageEvent event = new MessageEvent(Constant.EVENT_TYPE.BATTERY, stringBuilder.toString());
				EventBus.getDefault().post(event);
			}
		}
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that BluetoothGatt.close() is called
		// such that resources are cleaned up properly.  In this particular example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				if (BuildConfig.DEBUG) Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			if (BuildConfig.DEBUG) Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}
		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address The device address of the destination device.
	 * @return Return true if the connection is initiated successfully. The connection result
	 * is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public boolean connect(final String address) {
		//mBluetoothAdapter.startDiscovery();
		if (mBluetoothAdapter == null || address == null || address.equalsIgnoreCase("")) {
			if (BuildConfig.DEBUG)
				Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}
		// Previously connected device.  Try to reconnect.
		if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}
		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			if (BuildConfig.DEBUG) Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		if (BuildConfig.DEBUG) Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The disconnection result
	 * is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			if (BuildConfig.DEBUG) Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
		close();
	}

	private boolean refreshDeviceCache(BluetoothGatt gatt) {
		try {
			BluetoothGatt localBluetoothGatt = gatt;
			Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
			if (localMethod != null) {
				boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
				return bool;
			}
		} catch (Exception localException) {
			if (BuildConfig.DEBUG) Log.e(TAG, "An exception occurred while refreshing device");
		}
		return false;
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure resources are
	 * released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		if (refreshDeviceCache(mBluetoothGatt))
			mBluetoothGatt.close();
		mBluetoothGatt = null;
		//mBluetoothAdapter.cancelDiscovery();
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
	 * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			if (BuildConfig.DEBUG) Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic Characteristic to act on.
	 * @param enabled        If true, enable notification.  False otherwise.
	 */
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
											  boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			if (BuildConfig.DEBUG) Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if (UUID.fromString(BLEDefine.RBL_CHAR_RX_UUID).equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
					UUID.fromString(BLEDefine.RBL_DESCRIPTOR_UUID));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This should be
	 * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null) return null;
		return mBluetoothGatt.getServices();
	}

	/**
	 * 내부 내용 전부 삭제하고
	 * return lumiBLECharacteristic; 만 남기는 것으로 변경 예정
	 *
	 * @return lumiBLEChracteristic
	 */
	@Deprecated
	public BluetoothGattCharacteristic getLumiCharacteristic() {
		if (mBluetoothGatt == null) return null;

		if (lumiBLEChracteristic == null) {

			BluetoothGattCharacteristic characteristic = null;

			for (BluetoothGattService gattService : mBluetoothGatt.getServices()) {
				if (gattService.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_SERVICE_UUID)) {
					for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
						if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_CHAR_RX_UUID)) {
							characteristic = gattCharacteristic;
							break;
						}
					}
					if (characteristic != null) {
						break;
					}
				}
			}
			lumiBLEChracteristic = characteristic;
		}

		return lumiBLEChracteristic;
	}

	// service 5개 모두 있는지 확인하는 내용 추가
	public BluetoothGattCharacteristic getLumiCharacteristicNCheckAllServices() {
		if (mBluetoothGatt == null) return null;

		if (lumiBLEChracteristic == null) {

			BluetoothGattCharacteristic characteristic = null;

			int cnt = 0;

			for (BluetoothGattService gattService : mBluetoothGatt.getServices()) {
				if (gattService.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_GENERIC_ACCESS_UUID) ||
						gattService.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_GENERIC_ATTRIBUTE_UUID) ||
						gattService.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_BATTERY_SERVICE_UUID) ||
						gattService.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_SERVICE_UUID) ||
						gattService.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_DFU_SERVICE_UUID))
					cnt++;
			}

			if (BuildConfig.DEBUG) Log.d(TAG, "find services : " + cnt);
			if (cnt < 5) {
				if (BuildConfig.DEBUG) Log.e(TAG, "cannot find all service");
				return null;
			}

			for (BluetoothGattService gattService : mBluetoothGatt.getServices()) {
				if (gattService.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_SERVICE_UUID)) {
					for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
						if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(BLEDefine.RBL_CHAR_RX_UUID)) {
							characteristic = gattCharacteristic;
							break;
						}
					}
					if (characteristic != null) {
						break;
					}
				}
			}
			lumiBLEChracteristic = characteristic;
		}

		return lumiBLEChracteristic;
	}

	/**
	 * Enables or disables notification on a give characteristic. and Send command
	 *
	 * @param characteristic Characteristic to act on.
	 * @param enabled        If true, enable notification.  False otherwise.
	 */
	/*void writeCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }*/
	public void sendGetFirmwareVersion() {
		if (BuildConfig.DEBUG) Log.d(TAG, "sendGetFirmwareVersion()");

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			return;
		}

		byte[] command = HexEditor.hexToByteArray("01");//=  hexStringToByteArray("01");

		characteristic.setValue(command);
		//this.writeCharacteristicNotification(characteristic, true);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public void sendGetPowerLevel() {
		// do not used
		BluetoothGattCharacteristic characteristic = null;
		for (BluetoothGattService gattService : getSupportedGattServices()) {
			//Log.d(TAG, gattService.getUuid().toString());
			if (gattService.getUuid().toString().equals(BLEDefine.RBL_BATTERY_SERVICE_UUID)) {
				for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
					if (gattCharacteristic.getUuid().toString().equals(BLEDefine.RBL_BATTERY_LEVEL_UUID)) {
						characteristic = gattCharacteristic;
						break;
					}
				}
				break;
			}
		}

		if (characteristic == null) {
			return;
		}

		readCharacteristic(characteristic);
	}

	public void sendGetLDIValue() {

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			return;
		}

		byte[] command = hexStringToByteArray("61");

		characteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public void sendGetUseHistoryData(Integer userNo) {

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			return;
		}

		Long tsLong = System.currentTimeMillis() / 1000;

		byte[] user = LittleEndianByteHandler.intToByte(userNo);

		StringBuilder result = new StringBuilder(HexEditor.byteArrayToHex(user));

		byte[] time = LittleEndianByteHandler.intToByte(tsLong.intValue());

		StringBuilder result2 = new StringBuilder(HexEditor.byteArrayToHex(time));

		StringBuilder last = new StringBuilder("31");
		last.append(result);
		last.append(result2);

		if (BuildConfig.DEBUG)
			Log.i(getClass().getSimpleName(), "=============>" + last.toString());

		//byte [] command = new BigInteger(sb.toString(), 16).toByteArray();
		byte[] command = HexEditor.hexToByteArray(last.toString());

		characteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public void sendGetAtomicUseHistoryData(Integer userNo) {

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			return;
		}

		Long tsLong = System.currentTimeMillis() / 1000;

		byte[] user = LittleEndianByteHandler.intToByte(userNo);

		StringBuilder result = new StringBuilder(HexEditor.byteArrayToHex(user));

		byte[] time = LittleEndianByteHandler.intToByte(tsLong.intValue());

		StringBuilder result2 = new StringBuilder(HexEditor.byteArrayToHex(time));

		StringBuilder last = new StringBuilder("32");
		last.append(result);
		last.append(result2);

		if (BuildConfig.DEBUG)
			Log.i(getClass().getSimpleName(), "=============>" + last.toString());

		//byte [] command = new BigInteger(sb.toString(), 16).toByteArray();
		byte[] command = HexEditor.hexToByteArray(last.toString());

		characteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public void sendGetAtomicUseHistoryEcho(String data) {

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			return;
		}

		/*Long tsLong = System.currentTimeMillis() / 1000;

		byte[] user = LittleEndianByteHandler.intToByte(userNo);

		StringBuilder result = new StringBuilder(HexEditor.byteArrayToHex(user));

		byte[] time = LittleEndianByteHandler.intToByte(tsLong.intValue());

		StringBuilder result2 = new StringBuilder(HexEditor.byteArrayToHex(time));

		StringBuilder last = new StringBuilder("32");
		last.append(result);
		last.append(result2);

		if (BuildConfig.DEBUG)
			Log.i(getClass().getSimpleName(), "=============>" + last.toString());

		//byte [] command = new BigInteger(sb.toString(), 16).toByteArray();
		byte[] command = HexEditor.hexToByteArray(last.toString());*/

		if (BuildConfig.DEBUG)
			Log.i(getClass().getSimpleName(), "=============>" + data);

		byte[] command = HexEditor.hexToByteArray(data);

		characteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public void sendGetDeviceStatus(Integer userNo) {

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			if (BuildConfig.DEBUG) Log.i(TAG, "characteristic is null");
			return;
		}

		Long tsLong = System.currentTimeMillis() / 1000;

		byte[] user = LittleEndianByteHandler.intToByte(userNo);

		StringBuilder result = new StringBuilder(HexEditor.byteArrayToHex(user));

		byte[] time = LittleEndianByteHandler.intToByte(tsLong.intValue());

		StringBuilder result2 = new StringBuilder(HexEditor.byteArrayToHex(time));

		StringBuilder last = new StringBuilder("51");
		last.append(result);
		last.append(result2);

		if (BuildConfig.DEBUG)
			Log.i(getClass().getSimpleName(), "=============>" + last.toString());

		//byte [] command = new BigInteger(sb.toString(), 16).toByteArray();
		byte[] command = HexEditor.hexToByteArray(last.toString());

		characteristic.setValue(command);
		boolean test = mBluetoothGatt.writeCharacteristic(characteristic);
		//if (BuildConfig.DEBUG) Log.d(TAG, "WriteCharacteristic: " + test);
	}

	public void sendClearUseHistory() {

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			return;
		}

		byte[] command = hexStringToByteArray("41");

		characteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public void sendClearUseHistory(long startTime[]) {

		BluetoothGattCharacteristic characteristic = this.getLumiCharacteristic();
		if (characteristic == null) {
			return;
		}

		StringBuilder result = new StringBuilder("42");

		for (int i = 0; i < startTime.length; i++) {
			Long tsLong = startTime[i];
			//if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "start time: " + startTime[i]);
			//if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "start time: " + LittleEndianByteHandler.longToByte(startTime[i]));
			//if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "start time: " + HexEditor.byteArrayToHex(LittleEndianByteHandler.longToByte(startTime[i])));
			//result.append(HexEditor.byteArrayToHex(LittleEndianByteHandler.longToByte(startTime[i])));
			//if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "start time: " + tsLong);
			//if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "start time: " + LittleEndianByteHandler.intToByte(tsLong.intValue()));
			//if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "start time: " + HexEditor.byteArrayToHex(LittleEndianByteHandler.intToByte(tsLong.intValue())));
			//LittleEndianByteHandler.intToByte(tsLong.intValue());
			result.append(HexEditor.byteArrayToHex(LittleEndianByteHandler.intToByte(tsLong.intValue())));
			//if (BuildConfig.DEBUG) Log.i(getClass().getSimpleName(), "result: " + result.toString());
		}

		if (BuildConfig.DEBUG)
			Log.i(getClass().getSimpleName(), "=============>" + result.toString());

		byte[] command = hexStringToByteArray(result.toString());

		characteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
