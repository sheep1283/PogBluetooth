package com.example.pogbluetooth

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.*
import android.content.*
import android.content.ContentValues.TAG
import android.content.Context
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.pogbluetooth.BluetoothLeService
import com.example.pogbluetooth.Database.AccidentDB
import org.w3c.dom.Text
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp

class DeviceControlActivity(): Activity() {
    private var device : BluetoothDevice? = null
    private var bleCharacteristic: BluetoothGattCharacteristic? = null
    var targetDevice: BluetoothDevice? = null
    var socket: BluetoothSocket? = null
    var mOutputStream: OutputStream? = null
    var mInputStream: InputStream? = null
    private var mBluetoothLeService: BluetoothLeService? = null
    private lateinit var mDataField: TextView
    private lateinit var btnNotify: Button
    private lateinit var btnSave: Button
    private lateinit var btnShow: Button
    private var mServiceDiscoveryStatusReceiverRegistered = false
    var connectionState = false
    lateinit var services: List<BluetoothGattService>
    private lateinit var experimentList: ArrayList<Experiment>
    var read = false
    private var accidentDb: AccidentDB? = null


    lateinit var mProgressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gatt_services_characteristics)
        mDataField = findViewById(R.id.data_value)
        btnNotify = findViewById(R.id.btn_notification)
        btnSave = findViewById(R.id.btn_save)
        btnShow = findViewById(R.id.btn_show)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("로딩중입니다")
        mProgressDialog.show()
        accidentDb = AccidentDB.getInstance(this)
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        var exList = listOf<Accident>()
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
        experimentList = arrayListOf()
        btnNotify.setOnClickListener {
            mProgressDialog.show()
            read = true
            mBluetoothLeService!!.readCharacteristic(services!![3].characteristics[0].descriptors[0].characteristic)
        }
        btnSave.setOnClickListener {
            val r = Runnable {
                for (i in experimentList){
                    val accident = Accident(null, i.date, i.impulseX, i.impulseY, i.impulseZ, i.accelerationOfGravityX, i.accelerationOfGravityY, i.accelerationOfGravityZ, i.frontBack, i.leftRight)
                    accidentDb!!.accidentDao().insert(accident)
                }
                exList = accidentDb!!.accidentDao().getAll()
            }
            val thread = Thread(r)
            thread.start()
        }
        btnShow.setOnClickListener {
            Log.d("getFromRoom", exList[0].impulseX)

        }

    }


    override fun onResume() {
        super.onResume()
        if (!connectionState) {// BluetoothLeService에서 STATE_DISCONNECTED는 0으로 함
        }
        else{
            mBluetoothLeService!!.registerBroadcastReceiver(this, mGattUpdateReceiver, BluetoothUtils.makeGattUpdateIntentFilter())
            mServiceDiscoveryStatusReceiverRegistered = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (mServiceDiscoveryStatusReceiverRegistered){
            mBluetoothLeService!!.unregisterBroadcastReceiver(this, mGattUpdateReceiver)
            mServiceDiscoveryStatusReceiverRegistered = false

        }
    }

    //    private var accidentDb: AccidentDB? = null
    private val mServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothLeService.LocalBinder
            mBluetoothLeService = binder.service
            val device = intent.getParcelableExtra<BluetoothDevice>("device")
            mBluetoothLeService!!.connectGatt(device!!)
            connectionState = true
            onResume()
            mProgressDialog.dismiss()

//            mBluetoothLeService!!.readCharacteristic(PogApplication.mGattServices!![3].characteristics[0].descriptors[0].characteristic)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mBluetoothLeService = null
        }
    }

    fun prepareBroadcastDataRead(gattCharacteristic: BluetoothGattCharacteristic){
        mBluetoothLeService!!.readCharacteristic(gattCharacteristic)
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent) {
            val action = p1.action
            val extras = p1.extras
            if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                services = mBluetoothLeService!!.getSupportedGattServices()!!
                mBluetoothLeService!!.readCharacteristic(services!![3].characteristics[0].descriptors[0].characteristic)
            }
            if(Constants.ACTION_DATA_AVAILABLE.equals(action) && read){
                Log.d("DeviceContralActi", extras.toString())
                if (extras!!.containsKey(Constants.EXTRA_BYTE_VALUE)) {
                    val hexString = String(extras.getByteArray(Constants.EXTRA_BYTE_VALUE)!!)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    Log.d("BroadCastRead", String(extras.getByteArray(Constants.EXTRA_BYTE_VALUE)!!))
                    displayData(hexString)
                    val chunckedHex = hexString.split(' ').toMutableList()
                    val experi = Experiment(System.currentTimeMillis().toString()
                    , chunckedHex[0]
                    , chunckedHex[1]
                    , chunckedHex[2]
                    , chunckedHex[3]
                    , chunckedHex[4]
                    , chunckedHex[5]
                    , chunckedHex[6]
                    , chunckedHex[7])
                    experimentList.add(experi)
                    read = false
                    mProgressDialog.dismiss()
                }
            }
        }
    }
    private val mGattConnectionReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                services = mBluetoothLeService!!.getSupportedGattServices()!!
            }
        }
    }


    fun displayData(data: String){
        if (data != null){
            mDataField.setText(data)
        }
    }

//    private val gattCallback : BluetoothGattCallback = object : BluetoothGattCallback(){
//        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
//            super.onConnectionStateChange(gatt, status, newState)
//            when (newState) {
//                BluetoothProfile.STATE_CONNECTED -> {
//                    Log.i(TAG, "Connected to GATT server.")
//                    Log.i(TAG, "Attempting to start service discovery: " +
//                            bluetoothGatt?.discoverServices())
//                }
//                BluetoothProfile.STATE_DISCONNECTED -> {
//                    Log.i(TAG, "Disconnected from GATT server.")
//                    disconnectGattServer()
//                }
//            }
//        }
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//            super.onServicesDiscovered(gatt, status)
//            when (status) {
//                BluetoothGatt.GATT_SUCCESS -> {
//                    Log.i(TAG, "Connected to GATT_SUCCESS.")
//                    broadcastUpdate("Connected "+ device?.name)
//                    Log.d("BluetoothConnectedAddress", device?.address.toString())
//                    Log.d("BluetoothConnectedAlias", device?.alias.toString())
//                    val services = gatt!!.services
//                    bleCharacteristic = services[3].characteristics[0].descriptors[0].characteristic
//                    Log.d("bluetoothservice1", bleCharacteristic!!.service.uuid.toString())
////                    Log.d("bluetoothservice1-1", services[1].characteristics[0].descriptors[1].characteristic.service.uuid.toString())
////                    Log.d("bluetoothservice1-2", services[1].characteristics[0].descriptors[1].characteristic.uuid.toString())
////                    connectToTargetedDevice(device!!)
//                    gatt.readCharacteristic(bleCharacteristic)
//
////                    Log.d("bluetoothservicedes", services[3].characteristics[0].uuid.toString())
//                    Log.d("bluetoothservicesize", services.size.toString())
////                    Log.d("bluetoothservice3", services[1].characteristics[0].descriptors[0].toString())
//                    for (service in services){
//                        val characteristics = service.characteristics
//                        for (characteristic in characteristics){
//                            Log.d("BluetoothCharStringPermission", characteristic.permissions.toString())
//                            Log.d("BluetoothCharWriteType", characteristic.writeType.toString())
//                        }
//                    }
//                }
//                else -> {
//                    Log.w(TAG, "Device service discovery failed, status: $status")
//                    broadcastUpdate("Fail Connect "+device?.name)
//                }
//            }
//        }
//
//
//
//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt?,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int
//        ) {
//            super.onCharacteristicRead(gatt, characteristic, status)
////            accidentDb = AccidentDB.getInstance(this)
//            Log.d("bluetoothReadChar", String(characteristic.value))
//        }
//
//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt?,
//            characteristic: BluetoothGattCharacteristic
//        ) {
//            super.onCharacteristicChanged(gatt, characteristic)
//            Log.d("bluetoothCharMsg", "onCharChanged")
//            readCharactertistic(characteristic)
//        }
//        private fun readCharactertistic(characteristic: BluetoothGattCharacteristic){
//            val msg = characteristic.value
//            Log.d("bluetoothCharMsg", msg.toString())
//        }
//        private fun broadcastUpdate(str: String) {
//            val mHandler : Handler = object : Handler(Looper.getMainLooper()){
//                override fun handleMessage(msg: Message) {
//                    super.handleMessage(msg)
//                    Toast.makeText(context,str,Toast.LENGTH_SHORT).show()
//                }
//            }
//            mHandler.obtainMessage().sendToTarget()
//        }
//        private fun disconnectGattServer() {
//            Log.d(TAG, "Closing Gatt connection")
//            // disconnect and close the gatt
//            if (bluetoothGatt != null) {
//                bluetoothGatt?.disconnect()
//                bluetoothGatt?.close()
//                bluetoothGatt = null
//            }
//        }
//    }

//    private fun connectToTargetedDevice(targetedDevice: BluetoothDevice) {
//        //Progress state text
//        Log.d("bluetoothLoading", targetedDevice!!.name)
//
//        val thread = Thread {
//            //선택된 기기의 이름을 갖는 bluetooth device의 object
//            //SPP_UUID
//            try {
//                Log.d("bluetoothDevice", device!!.name)
//                Log.d("bluetoothDevice", device!!.address)
//                Log.d("bluetoothDevice", device!!.bondState.toString())
//                Log.d("bluetoothUUID", bleCharacteristic!!.uuid.toString())
//                val uuid = UUID.fromString("d973f2e1-b19e-11e2-9e96-0800200c9a66")
//                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//
//                var socket = targetedDevice.createInsecureRfcommSocketToServiceRecord(bleCharacteristic!!.uuid)
//                var clazz = socket.remoteDevice.javaClass
//                var paramTypes = arrayOf<Class<*>>(Integer.TYPE)
//                var m = clazz.getMethod("createInsecureRfcommSocket", *paramTypes)
//                var fallbackSocket = m.invoke(socket.remoteDevice, Integer.valueOf(1)) as BluetoothSocket
//
//                // 소켓 생성
//                var socket1 = targetedDevice?.createInsecureRfcommSocketToServiceRecord(bleCharacteristic!!.uuid)
//                Log.d("bluetoothSocket", socket1.toString())
//
//                mOutputStream = socket?.outputStream
//                mInputStream = socket?.inputStream
//                //Connect
//                fallbackSocket.connect()
//
//                /**
//                 * After Connect Device
//                 */
//                //output, input stream을 열어 송/수신
//
//                // 데이터 수신 시작
//                beginListenForData()
//
//            } catch (e: java.lang.Exception) {
//                // 블루투스 연결 중 오류 발생
//                e.printStackTrace()
//                try {
//                    Log.d("bluetoothError", e.message.toString())
//                    socket?.close()
//
//                }
//                catch (e: IOException) {
//                    e.printStackTrace()
//                    Log.d("bluetoothError2", e.message.toString())
//
//                }
//            }
//        }
//        //연결 thread를 수행한다
//        thread.start()
//    }
//    fun beginListenForData() {
//
//        val mWorkerThread = Thread {
//            while (!Thread.currentThread().isInterrupted) {
//                try {
//                    val bytesAvailable = mInputStream?.available()
//                    if (bytesAvailable != null) {
//                        if (bytesAvailable > 0) { //데이터가 수신된 경우
//                            val packetBytes = ByteArray(bytesAvailable)
//                            mInputStream?.read(packetBytes)
//                            /**
//                             * 한 버퍼 처리
//                             */
//                            // Byte -> String
//                            val s = String(packetBytes,Charsets.UTF_8)
//                            //수신 String 출력
//                            Log.d("bluetoothRead", s)
//
//                            /**
//                             * 한 바이트씩 처리
//                             */
//                            for (i in 0 until bytesAvailable) {
//                                val b = packetBytes[i]
//                                Log.d("inputData", String.format("%02x", b))
//                            }
//                        }
//                    }
//                } catch (e: UnsupportedEncodingException) {
//                    e.printStackTrace()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//        //데이터 수신 thread 시작
//        mWorkerThread.start()
//    }

//
//    fun connectGatt(device:BluetoothDevice):BluetoothGatt?{
//        this.device = device
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            bluetoothGatt = device.connectGatt(context, false, gattCallback,
//                BluetoothDevice.TRANSPORT_LE)
//        }
//        else {
//            bluetoothGatt = device.connectGatt(context, false, gattCallback)
//        }
//        return bluetoothGatt
//    }
}