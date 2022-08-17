package com.docter.icare.data.bleUtil.device.air

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleDataReceiveListener
import com.docter.icare.data.bleUtil.bleInterface.BleScanCallback
import com.docter.icare.data.entities.view.item.DeviceScanItemEntity
import java.util.*

class AirBleManager constructor(private val context: Context) {

    companion object {
        private val SCAN_SERVICE = UUID.fromString("0000A002-0000-1000-8000-00805F9B34FB")
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

//        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
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
            val mac = result?.device?.address ?: return

            bleScanCallback?.onDeviceFound(
                DeviceScanItemEntity(name, mac)
            )
        }
    }

    fun stopScan() {

        if (isScanning) {
            isScanning = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            handler.removeCallbacks(runnable)
        }

        bleScanCallback?.onScanFinish()
    }

    fun connectDevice(mac: String) {
        if (isConnected) return

        if (bluetoothAdapter == null) return

        bluetoothGatt = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
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
        if (bluetoothAdapter == null) return

        if (bluetoothGatt == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
    }

    fun writeValue(bytes: ByteArray) {
        if (bluetoothWriteCharacteristic != null) {
            bluetoothWriteCharacteristic?.value = bytes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothGatt?.writeCharacteristic(bluetoothWriteCharacteristic)
        }
    }

    // 設備資訊
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            isConnecting = false
            try {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        when (status) {
                            BluetoothGatt.GATT_SUCCESS -> {
                                isConnected = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                    ActivityCompat.checkSelfPermission(context,
                                        Manifest.permission.BLUETOOTH_SCAN
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    return
                                }
                                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
                                gatt.discoverServices()
                            }
                            else -> {
                                isConnected = false
                            }
                        }
                    }

                    BluetoothGatt.STATE_DISCONNECTED -> {
                        isConnected = false
                        if (bluetoothGatt != null) {
                            bluetoothGatt?.close()
                            bleConnectListener?.onConnectFailed()
                        }
                    }
                }
            } catch (e: Exception) {
//                Toast.makeText(context, "onConnectionStateChange:\n${e.message}", Toast.LENGTH_LONG).show()
            }


        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)

            try {
                gatt.services.forEach {
                    if (it != null && status == BluetoothGatt.GATT_SUCCESS) {
                        Handler(Looper.getMainLooper()).postDelayed({ setMainNotify(it) }, 500)
                        Handler(Looper.getMainLooper()).postDelayed({ getWriteCharacteristic(it) }, 500)
                    }
                }
//                gatt.getService(MAIN_SERVICE).let {
//
//                    if (it != null && status == BluetoothGatt.GATT_SUCCESS) {
//                        Handler(Looper.getMainLooper()).postDelayed({ setMainNotify(it) }, 500)
//                        Handler(Looper.getMainLooper()).postDelayed({ getWriteCharacteristic(it) }, 500)
//                    }
//                }

                Handler(Looper.getMainLooper()).postDelayed({
                    bleConnectListener?.onConnectSuccess(gatt.device)
                }, 1500)
            } catch (e: Exception) {
//                Toast.makeText(context, "onServicesDiscovered:\n${e.message}", Toast.LENGTH_LONG).show()
            }

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            bleDataReceiveListener?.onReceive(characteristic.value)

        }
    }

    private fun setMainNotify(service: BluetoothGattService) {
        try {
            service.characteristics.forEach { notifyCharacteristic ->
                if (notifyCharacteristic.uuid == MAIN_NOTIFY_CHARACTERISTIC) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    bluetoothGatt?.setCharacteristicNotification(notifyCharacteristic, true)
                    notifyCharacteristic.descriptors.let {
                        if (it.isNotEmpty()) {
                            for (descriptor in it) {
                                bluetoothGatt?.writeDescriptor(descriptor.apply {
                                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                })
                            }
                        }
                    }
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
//            }

        } catch (e: Exception) {
            e.printStackTrace()
//            Toast.makeText(context, "setMainNotify:\n${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getWriteCharacteristic(service: BluetoothGattService) {
        try {

            service.characteristics.forEach {
                if (it.uuid == MAIN_WRITE_CHARACTERISTIC) {
                    bluetoothWriteCharacteristic = it
//                    writeValue("MOSAIP114.25.219.208:8001".toByteArray())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
//            Toast.makeText(context, "getWriteCharacteristic:\n${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
