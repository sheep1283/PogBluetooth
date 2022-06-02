package com.example.pogbluetooth

import android.bluetooth.*
import android.content.IntentFilter
import android.location.LocationManager
import java.util.*
import kotlin.collections.ArrayList

class BluetoothUtils {
    companion object {

        fun makeGattUpdateIntentFilter(): IntentFilter{
            val filter = IntentFilter()
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            filter.addAction(Constants.ACTION_PAIRING_CANCEL);
            filter.addAction(Constants.ACTION_OTA_STATUS);//CYACD
            filter.addAction(Constants.ACTION_OTA_STATUS_V1);//CYACD2
            filter.addAction(Constants.ACTION_GATT_CONNECTED);
            filter.addAction(Constants.ACTION_GATT_CONNECTING);
            filter.addAction(Constants.ACTION_GATT_DISCONNECTED);
            filter.addAction(Constants.ACTION_GATT_DISCONNECTING);
            filter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
            filter.addAction(Constants.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL);
            filter.addAction(Constants.ACTION_GATT_CHARACTERISTIC_ERROR);
            filter.addAction(Constants.ACTION_GATT_INSUFFICIENT_ENCRYPTION);
            filter.addAction(Constants.ACTION_DATA_AVAILABLE);
            filter.addAction(Constants.ACTION_WRITE_SUCCESS);
            filter.addAction(Constants.ACTION_WRITE_FAILED);
            filter.addAction(Constants.ACTION_WRITE_COMPLETED);
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            return filter;
        }


    }
}