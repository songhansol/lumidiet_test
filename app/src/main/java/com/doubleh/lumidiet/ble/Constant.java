package com.doubleh.lumidiet.ble;

/**
 * Created by steve on 2016. 7. 31..
 */
public class Constant {
	static public enum EVENT_TYPE {
		BOOK_SUMMARY,
		BOOK_TOC,
		SMARTGUID,
		BLE_STATE_CONNECTED,
		BLE_STATE_DISCONNECTED,
		BLE_STATE_OTHER,
		GATT_SERVICES_DISCOVERED,
		DATA_AVAILABLE,
		GATT_CAN_READ,
     	BATTERY,
	}

	;

}
