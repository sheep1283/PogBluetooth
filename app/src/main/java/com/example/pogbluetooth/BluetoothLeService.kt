package com.example.pogbluetooth

import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.content.*
import android.content.ContentValues.TAG
import android.os.*
import android.provider.SyncStateContract
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.*

class BluetoothLeService : Service() {

    private lateinit var mBluetoothGatt: BluetoothGatt
    private val TAG: String = "BluetoothLeServiceTAG"

    val STATE_DISCONNECTED = 0
    val STATE_CONNECTING = 1
    val STATE_CONNECTED = 2
    var mConnectionState = STATE_DISCONNECTED


    private val mBinder: IBinder = LocalBinder()


    private val gattCallback : BluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            var intentAction: String? = null
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = Constants.ACTION_GATT_CONNECTED
                    mConnectionState = STATE_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(
                        TAG, "Attempting to start service discovery: " +
                            mBluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = Constants.ACTION_GATT_DISCONNECTED
                    mConnectionState = STATE_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "Connected to GATT_SUCCESS.")
                    PogApplication.mGattServices = getSupportedGattServices()!!
                    val services = getSupportedGattServices()
//                    Log.d("BluetoothLeService1", services!!.size.toString())
//                    Log.d("BluetoothLeService2", services!![3].characteristics.size.toString())
//                    Log.d("BluetoothLeService3", services!![3].characteristics[0].descriptors.size.toString())
//                    Log.d("BluetoothLeService4", services!![3].characteristics[0].descriptors[0].characteristic.toString())
//                    Log.d("BluetoothLeService", String(services!![3].characteristics[0].descriptors[0].characteristic.value))
//                    readCharacteristic(services!![3].characteristics[0].descriptors[0].characteristic)
                    broadcastUpdate(Constants.ACTION_GATT_SERVICES_DISCOVERED)
                }
                else -> {
                    Log.w(TAG, "Device service discovery failed, status: $status")
                }
            }
        }


        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS){
                broadcastNotifyUpdate(characteristic)
            }
        }

//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt?,
//            characteristic: BluetoothGattCharacteristic
//        ) {
//            super.onCharacteristicChanged(gatt, characteristic)
//            broadcastUpdate(Constants.ACTION_DATA_AVAILABLE)
//            broadcastNotifyUpdate(characteristic)
//        }
    }
    fun getConnectionState(): Int{
        synchronized(gattCallback){
            return mConnectionState
        }
    }
    fun getSupportedGattServices(): List<BluetoothGattService>? {
        if(mBluetoothGatt == null)
            return null
        return mBluetoothGatt.services
    }
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic){
        mBluetoothGatt.readCharacteristic(characteristic)
    }

    private fun broadcastUpdate(str: String) {
        val intent = Intent(str)
        sendBroadcast(intent)
    }

    private fun broadcastNotifyUpdate(characteristic: BluetoothGattCharacteristic){
        val dataAvailableIntent = Intent(Constants.ACTION_DATA_AVAILABLE)
        val bundle = Bundle()
        bundle.putByteArray(Constants.EXTRA_BYTE_VALUE, characteristic.value)
        bundle.putString(Constants.EXTRA_BYTE_UUID_VALUE, characteristic.uuid.toString())
        bundle.putInt(Constants.EXTRA_BYTE_INSTANCE_VALUE, characteristic.instanceId)
        bundle.putString(Constants.EXTRA_BYTE_SERVICE_UUID_VALUE, characteristic.service.uuid.toString())
        bundle.putInt(Constants.EXTRA_BYTE_SERVICE_INSTANCE_VALUE, characteristic.service.instanceId)

        dataAvailableIntent.putExtras(bundle)
        sendLocalBroadcastIntent(this, dataAvailableIntent)
    }
    fun registerBroadcastReceiver(context: Context, receiver: BroadcastReceiver, filter: IntentFilter){
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
        context.registerReceiver(receiver, filter)
    }
    fun unregisterBroadcastReceiver(context: Context, receiver: BroadcastReceiver){
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        context.unregisterReceiver(receiver)
    }
    fun sendLocalBroadcastIntent(context: Context, intent: Intent){
        intent.setPackage(Constants.PACKAGE_NAME)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    inner class LocalBinder: Binder(){
        val service: BluetoothLeService
        get() = this@BluetoothLeService
    }


    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    fun connectGatt(device:BluetoothDevice):BluetoothGatt?{

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(this, false, gattCallback,
                BluetoothDevice.TRANSPORT_LE) }
        else {
            mBluetoothGatt = device.connectGatt(this, false, gattCallback)
        }
        return mBluetoothGatt
    }
}