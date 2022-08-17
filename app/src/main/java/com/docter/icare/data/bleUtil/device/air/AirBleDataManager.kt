package com.docter.icare.data.bleUtil.device.air

import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.docter.icare.data.bleUtil.bleInterface.BleDataReceiveListener
import com.docter.icare.data.bleUtil.bleInterface.BleScanCallback
import com.docter.icare.data.bleUtil.model.AirData

class AirBleDataManager (context: Context) : BleConnectListener {

    var isConnect = false
    private var privateContext: Context? = null
    private var airBleManager: AirBleManager? = null
    private var bleScanCallback: BleScanCallback? = null
    private var bleConnectListener: BleConnectListener? = null
    private var bleAirDataListener: ((radarData: AirData) -> Unit)? = null

    companion object {
        private var instance : AirBleDataManager? = null

        @Synchronized
        fun getInstance(context: Context): AirBleDataManager {
            if (instance == null)  // NOT thread safe!
                instance = AirBleDataManager(context)

            return instance!!
        }
    }

    init {
        privateContext = context
        airBleManager = AirBleManager(context)
        airBleManager?.bleConnectListener = this
    }

    fun startScan(scanCallback: BleScanCallback) {
        this.bleScanCallback = scanCallback
        airBleManager?.bleScanCallback = this.bleScanCallback
        airBleManager?.startScan()
    }

    fun stopScan() {
        airBleManager?.stopScan()
    }

    fun bleConnectListener(connectListener: BleConnectListener) {
        this.bleConnectListener = connectListener
    }

    fun connectDevice(mac: String) {
        airBleManager?.connectDevice(mac)
        setDataReceiverListener()
    }

    fun disconnect() {
        airBleManager?.disconnect()
    }

    fun setAirDataListener(bleAirDataListener: ((AirData) -> Unit)?){
        this.bleAirDataListener = bleAirDataListener
    }

    private fun setDataReceiverListener() {
        airBleManager?.bleDataReceiveListener = dataReceiveListener
    }

    fun writeValue(string: String) {
        airBleManager?.writeValue(string.toByteArray())
    }

    private val dataReceiveListener = object : BleDataReceiveListener {
        override fun onReceive(data: ByteArray) {
            parsePacket(data)
        }
    }

    private fun parsePacket(receivePacket: ByteArray) {

        val co2 = (receivePacket[2].toInt() * 256) + receivePacket[3].toInt()
        val ch2o = (receivePacket[4].toInt() * 256) + receivePacket[5].toInt()
        val tvoc = (receivePacket[6].toInt() * 256) + receivePacket[7].toInt()
        val pm25 = (receivePacket[8].toInt() * 256) + receivePacket[9].toInt()
        val pm10 = (receivePacket[10].toInt() * 256) + receivePacket[11].toInt()
        val temperature = if (receivePacket[12].toInt() >= 128) {
            String.format("%.2f", ((128 - receivePacket[12].toInt()) - receivePacket[13].toDouble()) / 10).toDouble()
        } else {
            String.format("%.2f", (receivePacket[12].toDouble() + receivePacket[13].toDouble()) / 10).toDouble()
        }
        val humidity = String.format("%.1f", (receivePacket[14].toDouble() + receivePacket[15].toDouble()) / 10).toDouble()

        val airData = AirData(co2, ch2o, tvoc, pm25, pm10, temperature, humidity)
        this.bleAirDataListener?.invoke(airData)

    }

    /**
     *  Bluetooth Connection Listener
     */
    override fun onConnectSuccess(device: BluetoothDevice) {
        isConnect = true
        this.bleConnectListener?.onConnectSuccess(device)
    }

    override fun onConnectFinished() {
        isConnect = true
        this.bleConnectListener?.onConnectFinished()
    }

    override fun onConnectFailed() {
        isConnect = false
    }

}