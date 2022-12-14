package com.docter.icare.data.bleUtil.device.radar

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleDataReceiveListener
import com.docter.icare.data.bleUtil.bleInterface.BleScanCallback
import com.docter.icare.data.bleUtil.model.RadarData
import com.docter.icare.utils.toHexStringSpace
import java.text.SimpleDateFormat
import java.util.*

class RadarBleDataManager(context: Context) : BleConnectListener {
    var isConnect = false
    private var privateContext: Context
    private var radarBleManager: RadarBleManager? = null
    private var bleScanCallback: BleScanCallback? = null
    private var bleConnectListener: BleConnectListener? = null
    //    private var bleBioradarDataListener: ((rawHexString: String, asciiString: String) -> Unit)? = null
    private var bleBioradarDataListener: ((radarData: RadarData) -> Unit)? = null
    private var bleBioradarDistanceListener: ((Int) -> Unit)? = null
//    private var bleRadarDataListener: BleDataReceiveListener? = null

    companion object {
        private var instance : RadarBleDataManager? = null
        private const val BUFFER_MAX_LENGTH = 8 * 1024

        @Synchronized
        fun getInstance(context: Context): RadarBleDataManager {
            if (instance == null)  // NOT thread safe!
                instance = RadarBleDataManager(context)

            return instance!!
        }
    }

    init {
        radarBleManager = RadarBleManager(context)
        radarBleManager?.bleConnectListener = this
        privateContext = context
    }

    fun startScan(scanCallback: BleScanCallback) {
        this.bleScanCallback = scanCallback
        radarBleManager?.bleScanCallback = this.bleScanCallback
        radarBleManager?.startScan()
    }

    fun stopScan() {
        radarBleManager?.stopScan()
    }

    fun bleConnectListener(connectListener: BleConnectListener) {
        Log.i("RadarBleDataManager","bleConnectListener")
        this.bleConnectListener = connectListener
    }

    fun connectDevice(mac: String) {
        Log.i("RadarBleDataManager","connectDevice")
        radarBleManager?.connectDevice(mac)
        setDataReceiverListener()
    }

    fun disconnect() {
        Log.i("RadarBleDataManager","disconnect")
        radarBleManager?.disconnect()
    }

    fun setBioradarDataListener(bleBioradarDataListener: ((RadarData) -> Unit)?){
        Log.i("RadarBleDataManager","setBioradarDataListener")
        this.bleBioradarDataListener = bleBioradarDataListener
    }

    fun setBioradarDistanceListener(bleBioradarDistanceListener: ((Int) -> Unit)?){
        Log.i("RadarBleDataManager","setBioradarDistanceListener")
        this.bleBioradarDistanceListener = bleBioradarDistanceListener
    }

    private fun setDataReceiverListener() {
        Log.i("RadarBleDataManager","setDataReceiverListener")
        radarBleManager?.bleDataReceiveListener = dataReceiveListener
    }

    fun writeValue(string: String) {
        Log.i("RadarBleDataManager","writeValue=>${string.toByteArray().toHexStringSpace()}")
        radarBleManager?.writeValue(string.toByteArray())
    }

    fun writeRegulateValue(regulateData: ByteArray) {
        Log.i("RadarBleDataManager","writeRegulateValue=>${regulateData.toHexStringSpace()}")
        radarBleManager?.writeValue(regulateData)
    }

    fun writeTemperatureCalibration(TemperatureData: ByteArray) {
        Log.i("RadarBleDataManager","writeTemperatureCalibration TemperatureData=>${TemperatureData.toHexStringSpace()}")
        radarBleManager?.writeValue(TemperatureData)
    }

    var bleRadarDataListener: BleDataReceiveListener? = null

    private val dataReceiveListener = object : BleDataReceiveListener {
        override fun onRadarData(data: ByteArray) {
            super.onRadarData(data)
            Log.i("RadarBleDataManager","dataReceiveListener onReceive")
            parsePacket(data)
            bleRadarDataListener?.onRadarData(data)
        }
    }

    private fun parsePacket(receivePacket: ByteArray) {
        Log.i("RadarBleDataManager","parsePacket receivePacket=>${receivePacket.toHexStringSpace()}")
        val receiveString = String(receivePacket)
        Log.i("RadarBleDataManager","parsePacket receivePacket receiveString=>$receiveString")
        //CONNECT SUCCESS ????????????wifi??????????????????
        //WIFI CONNECT FAIL wifi????????????
        //SERVER CONNECT FAIL server????????????
        //LOAD SERVER FAIL ????????????????????????
        //OTHER FAIL ????????????

    }

    //???????????????
//    private fun parsePacket(receivePacket: ByteArray) {
//        Log.i("RadarBleDataManager","parsePacket")
//        val receiveString = String(receivePacket)
//
////        if ("SYTC" in receiveString) {
//        val radarValue = receivePacket[4].toInt()
//        var radarState = ""
//        when (radarValue) {
//            1 -> radarState = "??????"
//            2 -> radarState = "??????"
//            3 -> radarState = "??????"
//        }
//
//        val distance = String.format("%.1f", receivePacket[5].toInt() * 0.1)
//        val bedState = if (receivePacket[6].toInt() == 0) "??????" else "?????????" //20220303 ?????? ??? 0?????? 1????????? ?????????=> 0?????? 1?????? 2?????? 3????????????
//        val breathState = receivePacket[7].toInt()
//        val heartRate = receivePacket[8].toInt()
//        val now = DateUtils.getNowTime("yyyy-MM-dd HH:mm:ss")
//
////        if (heartRate < 0) {
////            val date = DateUtils.getNowTime("yyyy-MM-dd")
////            val time = DateUtils.getNowTime("HH:mm:ss")
////            AsyncTask.execute {
////                DBManager.getInstance(privateContext).abnormalRadarDao().insert(AbnormalRadarData(radarState, distance.toDouble(), heartRate, bedState, breathState, receiveString, date, time))
////            }
////        } else {
//            val radarData = RadarData(radarState, distance, bedState, breathState, heartRate, now)
//        Log.i("RadarBleDataManager","radarData=>$radarData")
//           this.bleBioradarDataListener?.invoke(radarData)
//            this.bleBioradarDistanceListener?.invoke(receivePacket[5].t oInt())
////        }
//
////        } else {
////            this.bleBioradarDataListener?.invoke("????????????", receiveString)
////        }
//
//
//    }

    object DateUtils {
        fun getNowTime(withFormat: String): String {
            val sdf = SimpleDateFormat(withFormat)
            return sdf.format(Date())
        }
    }
    //???????????????


    /**
     *  Bluetooth Connection Listener
     */
    override fun onConnectSuccess(device: BluetoothDevice) {
        Log.i("RadarBleDataManager","onConnectSuccess")
        isConnect = true
        this.bleConnectListener?.onConnectSuccess(device)
    }

    override fun onConnectFinished() {
        Log.i("RadarBleDataManager","onConnectFinished")
        isConnect = true
        this.bleConnectListener?.onConnectFinished()
    }

    override fun onConnectFailed() {
        Log.i("RadarBleDataManager","onConnectFailed")
        isConnect = false
    }



    fun isBluetoothEnabled() = radarBleManager?.isBluetoothEnabled()


}

//interface BleSettingReceiveCallback {
//    fun onB10HRVReceive(data: ByteArray){}
//    private fun parseB10HRVReceiveData(
//        data: ByteArray
//    ){
//        Log.i("BleDataReceive","parseB10HRVReceiveData}")
//        bleSettingReceiveCallback?.onB10HRVReceive(data)
//    }
//    fun getBleData() = fatigueRepository.getB10SynHRVData(bleSettingReceiveCallback)
//    val bleSettingReceiveCallback = object : BleSettingReceiveCallback {
//        override fun onB10HRVReceive(data: ByteArray) {
//            super.onB10HRVReceive(data)
////            Log.i("FatigueViewModel","onB10HRVReceive data=>$data")
//            Log.i("FatigueViewModel","onB10HRVReceive data hex=>${data.toHexStringSpace()}")
//            main{
//                refreshData()
//            }
//        }
//    }

//  setBioradarDataListener
//        setUpdateRadarData()
//        RadarBleDataManager.getInstance(requireActivity())
//            .setBioradarDataListener { radarData ->
//                if (!isOnline) {
//                    heartRateTextView.text = "${radarData.heartRate}"
//                    breathFrequencyTextView.text = "${radarData.breathState}"
//                    dataTimeTextView.text = "${radarData.createDate}"
//                    bedStateTextView.text = if (radarData.bedState == "??????") getString(R.string.out_of_bed) else getString(R.string.on_bed)
//                }