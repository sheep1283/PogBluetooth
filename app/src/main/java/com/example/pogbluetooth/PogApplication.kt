package com.example.pogbluetooth

import android.app.Application
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

class PogApplication: Application() {
    companion object{
        var mGattServices: List<BluetoothGattService>? = null
    }

    override fun onCreate() {
        super.onCreate()
    }

    fun getCharacteristic(): BluetoothGattCharacteristic {
        return mGattServices!![3].characteristics[0].descriptors[0].characteristic
    }

}