package com.docter.icare.data.bleUtil.device.radar

import android.Manifest.permission.BLUETOOTH_SCAN
import android.content.Context
import android.bluetooth.*
import android.bluetooth.le.*
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleDataReceiveListener
import com.docter.icare.data.bleUtil.bleInterface.BleScanCallback
import java.util.*
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.RADAR_DEVICE_MAC
import com.docter.icare.utils.toast
import android.content.pm.PackageManager.PERMISSION_GRANTED
import com.docter.icare.data.entities.view.item.DeviceScanItemEntity

class RadarBleManager constructor(
    private val context: Context
) {
    companion object {
        private val SCAN_SERVICE = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
        //        private val MAIN_SERVICE = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        private val MAIN_NOTIFY_CHARACTERISTIC =
            UUID.fromString("0000C305-0000-1000-8000-00805F9B34FB")
        private val MAIN_WRITE_CHARACTERISTIC =
            UUID.fromString("0000C304-0000-1000-8000-00805F9B34FB")
    }

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothWriteCharacteristic: BluetoothGattCharacteristic? = null

    private val scanSetting = ScanSettings.Builder().apply {
        setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
    }.build()

    var bleScanCallback: BleScanCallback? = null
    var bleConnectListener: BleConnectListener? = null
    var bleDataReceiveListener: BleDataReceiveListener? = null

    var isConnecting = false
    var isConnected = false

    private var isManualDisconnect = false
    private var isStarting = false
    private var isScanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable { stopScan() }

    init {
        init()
    }

    private fun init(): Boolean {
        if (!isStarting) {
            if (bluetoothManager == null) {
                bluetoothManager =
                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                if (bluetoothManager == null) return false
            }

            if (bluetoothAdapter == null) {
                bluetoothManager?.let { bluetoothAdapter = it.adapter }
                if (bluetoothAdapter == null) return false
            }
        }

        return true
    }

    fun startScan() {
//        mutableListOf<ScanFilter>().apply {
//            add(ScanFilter.Builder().setServiceUuid(ParcelUuid(SCAN_SERVICE)).build())
//        }.let {
//            bluetoothAdapter?.bluetoothLeScanner?.startScan(it, scanSetting, scanCallback)
//        }


        if (SDK_INT >= S &&
            ActivityCompat.checkSelfPermission(context, BLUETOOTH_SCAN) != PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)

        isScanning = true
        handler.postDelayed(runnable, 5000)
    }

    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            val name = result?.scanRecord?.deviceName ?: return
            val mac = result.device?.address ?: return

            bleScanCallback?.onDeviceFound(
                DeviceScanItemEntity(name, mac)
            )
        }
    }

    fun stopScan() {

        if (isScanning) {
            isScanning = false
            if (SDK_INT >= S &&
                ActivityCompat.checkSelfPermission(context, BLUETOOTH_SCAN) != PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            handler.removeCallbacks(runnable)
        }

        bleScanCallback?.onScanFinish()
    }

    fun connectDevice(mac: String) {
        Log.i("RadarBleManager","connectDevice")
        if (isConnected) return

        if (bluetoothAdapter == null) return

        bluetoothGatt = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (SDK_INT >= S &&
                ActivityCompat.checkSelfPermission(context, BLUETOOTH_SCAN) != PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothAdapter?.getRemoteDevice(mac)
                ?.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            bluetoothAdapter?.getRemoteDevice(mac)
                ?.connectGatt(context, true, gattCallback)
        }
    }

    fun disconnect() {
        Log.i("RadarBleManager","disconnect")
        if (bluetoothAdapter == null) return

        if (bluetoothGatt == null) return

        if (SDK_INT >= S &&
            ActivityCompat.checkSelfPermission(context, BLUETOOTH_SCAN) != PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
        isManualDisconnect = true
    }

    fun writeValue(bytes: ByteArray) {
        Log.i("RadarBleManager","writeValue")
        if (bluetoothWriteCharacteristic != null) {
            bluetoothWriteCharacteristic?.value = bytes
            Log.i("RadarBleManager","writeValue bluetoothWriteCharacteristic.value=>${bluetoothWriteCharacteristic?.value}")
            if (SDK_INT >= S &&
                ActivityCompat.checkSelfPermission(context, BLUETOOTH_SCAN) != PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothGatt?.writeCharacteristic(bluetoothWriteCharacteristic)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i("RadarBleManager","gattCallback onConnectionStateChange")
            isConnecting = false
            try {
                Log.i("RadarBleManager","gattCallback onConnectionStateChange status=>$status newState=>$newState")
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        Log.i("RadarBleManager","gattCallback BluetoothGatt.STATE_CONNECTED")
                        when (status) {
                            BluetoothGatt.GATT_SUCCESS -> {
                                Log.i("RadarBleManager","gattCallback BluetoothGatt.GATT_SUCCESS")
                                isConnected = true
                                isManualDisconnect = false
                                if (SDK_INT >= S &&
                                    ActivityCompat.checkSelfPermission(context, BLUETOOTH_SCAN) != PERMISSION_GRANTED
                                ) {
                                    return
                                }
                                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
                                gatt?.discoverServices()
                            }
                            else -> {
                                Log.i("RadarBleManager","gattCallback ! BluetoothGatt.GATT_SUCCESS")
                                isConnected = false
                            }
                        }
                    }

                    BluetoothGatt.STATE_DISCONNECTED -> {
                        Log.i("RadarBleManager","gattCallback BluetoothGatt.STATE_DISCONNECTED")
                        isConnected = false
                        if (bluetoothGatt != null) {
                            Log.i("RadarBleManager","gattCallback BluetoothGatt.STATE_DISCONNECTED luetoothGatt != null")
                            bluetoothGatt?.close()
                            bleConnectListener?.onConnectFailed()

                            if (!isManualDisconnect) {
                                val deviceMac = PreferenceProvider(context).getString(RADAR_DEVICE_MAC)
                                connectDevice(deviceMac)
                            }else Log.i("RadarBleManager","gattCallback BluetoothGatt.STATE_DISCONNECTED luetoothGatt != null isManualDisconnect")
                        }else{
                            Log.i("RadarBleManager","gattCallback BluetoothGatt.STATE_DISCONNECTED bluetoothGatt == null")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.i("RadarBleManager","Exception onConnectionStateChange:\n" +
                        "${e.message}")
                context.toast("Exception onConnectionStateChange:\n ${e.message}")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i("RadarBleManager","gattCallback onServicesDiscovered")
            try {
                gatt?.services?.forEach {
                    if (it != null && status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i("RadarBleManager","gattCallback onServicesDiscovered it != null && status == BluetoothGatt.GATT_SUCCESS")
                        Handler(Looper.getMainLooper()).postDelayed({ setMainNotify(it) }, 500)
                        Handler(Looper.getMainLooper()).postDelayed({ getWriteCharacteristic(it) }, 500)
                    }else Log.i("RadarBleManager","gattCallback onServicesDiscovered it != null && status != BluetoothGatt.GATT_SUCCESS")
                }
//                gatt.getService(MAIN_SERVICE).let {
//
//                    if (it != null && status == BluetoothGatt.GATT_SUCCESS) {
//                        Handler(Looper.getMainLooper()).postDelayed({ setMainNotify(it) }, 500)
//                        Handler(Looper.getMainLooper()).postDelayed({ getWriteCharacteristic(it) }, 500)
//                    }
//                }

                Handler(Looper.getMainLooper()).postDelayed({
                    bleConnectListener?.onConnectSuccess(gatt!!.device)
                }, 1500)
            } catch (e: Exception) {
                Log.i("RadarBleManager","Exception onServicesDiscovered:\n" +
                        "${e.message}")
                context.toast("Exception onServicesDiscovered:\n${e.message}")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            bleDataReceiveListener?.onRadarData(characteristic!!.value)
        }
    }

    private fun setMainNotify(service: BluetoothGattService) {
        try {
            Log.i("RadarBleManager","setMainNotify")
            service.characteristics.forEach { notifyCharacteristic ->
                if (notifyCharacteristic.uuid == MAIN_NOTIFY_CHARACTERISTIC) {
                    Log.i("RadarBleManager","setMainNotify notifyCharacteristic.uuid == MAIN_NOTIFY_CHARACTERISTIC")
                    if (SDK_INT >= S &&
                        ActivityCompat.checkSelfPermission(context, BLUETOOTH_SCAN) != PERMISSION_GRANTED
                    ) {
                        return
                    }
                    bluetoothGatt?.setCharacteristicNotification(notifyCharacteristic, true)
                    notifyCharacteristic.descriptors.let {
                        if (it.isNotEmpty()) {
                            Log.i("RadarBleManager","setMainNotify it.isNotEmpty")
                            for (descriptor in it) {
                                bluetoothGatt?.writeDescriptor(descriptor.apply {
                                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                })
                            }
                        }else{
                            Log.i("RadarBleManager","setMainNotify it.is Empty")
                            Observable.timer(5000, TimeUnit.MILLISECONDS).subscribe {
                                Log.i("RadarBleManager","setMainNotify 無資料超過5s")
                                context.toast("無資料超過5s")
                            }
                        }
                    }
                }
                else{
                    Log.i("RadarBleManager","setMainNotify notifyCharacteristic.uuid != MAIN_NOTIFY_CHARACTERISTIC")
                }
            }
//            service.getCharacteristic(MAIN_NOTIFY_CHARACTERISTIC).let { notifyCharacteristic ->
//                if (notifyCharacteristic != null) {
//                    bluetoothGatt?.setCharacteristicNotification(notifyCharacteristic, true)
//                    notifyCharacteristic.descriptors.let {
//                        if (it.isNotEmpty()) {
//                            for (descriptor in it) {
//                                bluetoothGatt?.writeDescriptor(descriptor.apply {
//                                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                                })
//                            }
//                        }
//                    }
//                }
//
//            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("RadarBleManager","Exception setMainNotify:\n ${e.message}")
            context.toast("Exception setMainNotify:\n${e.message}")
        }
    }

    private fun getWriteCharacteristic(service: BluetoothGattService) {
        Log.i("RadarBleManager","getWriteCharacteristic")
        try {

            service.characteristics.forEach {
                if (it.uuid == MAIN_WRITE_CHARACTERISTIC) {
                    Log.i("RadarBleManager","getWriteCharacteristic it.uuid == MAIN_WRITE_CHARACTERISTIC")
                    bluetoothWriteCharacteristic = it
//                    writeValue("MOSAIP114.25.219.208:8001".toByteArray())
                }else  Log.i("RadarBleManager","getWriteCharacteristic it.uuid != MAIN_WRITE_CHARACTERISTIC")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("RadarBleManager","Exception getWriteCharacteristic:\n${e.message}")
            context.toast("Exception getWriteCharacteristic:\n${e.message}")
        }
    }



}