package com.docter.icare.ui.main.device

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.docter.icare.R
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleDataReceiveListener
import com.docter.icare.data.entities.device.ToastAlertEntity
import com.docter.icare.data.entities.view.DeviceEntity
import com.docter.icare.data.entities.view.LoginEntity
import com.docter.icare.data.network.api.apiErrorShow
import com.docter.icare.data.repositories.DeviceRepository
import com.docter.icare.data.repositories.RadarRepository
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.toBedName
import com.docter.icare.utils.toHexStringSpace
import com.docter.icare.view.dialog.ToastAlertDialog
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DeviceViewModel(
    private val deviceRepository: DeviceRepository,
    private val radarRepository: RadarRepository
) : ViewModel() {
    val entity = DeviceEntity()
    var appContext : MutableLiveData<Context> = MutableLiveData(null)
    val isDataSend: MutableLiveData<Boolean> = MutableLiveData(false)
//    val throwMessage = MutableLiveData("")
    val throwMessage = MutableLiveData(ToastAlertEntity())
//    var accountInfo = AccountInfo()
//    val connectFlag = MutableLiveData(-1)//0:寫入wifi 1:校正距離
//    val bedTypeChangeFlag: MutableLiveData<Boolean> = MutableLiveData(false)
//    val setBedType: MutableLiveData<Int> = MutableLiveData(1)
    val isBleConnectSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSetProgress: MutableLiveData<Boolean> = MutableLiveData(false)

//    init {
//        setSettingReceiveCallback()
//    }

    fun setType(type: String) =  deviceRepository.setType(type)

    fun getDeviceInfo(){
       if (entity.deviceType.isNotEmpty()) deviceRepository.getDeviceInfo(entity).let {
           with(entity){
               deviceName = it.deviceName
               deviceMac = it.deviceMac
               deviceAccountId = it.deviceAccountId
               if (it.wifiAccount.value?.isNotEmpty() == true) wifiAccount.value = it.wifiAccount.value
               wifiPassword = it.wifiPassword
//               if (it.wifiAccount.value?.isNotEmpty() == true) wifiAccount.value = it.wifiAccount.value
//               if (it.wifiPassword.value?.isNotEmpty() == true) wifiPassword.value = it.wifiPassword.value
               Log.i("DeviceViewModel","entity deviceName=>$deviceName")
               Log.i("DeviceViewModel","entity wifiAccount=>${wifiAccount.value}")
               Log.i("DeviceViewModel","entity wifiPassword=>$wifiPassword")
//               Log.i("DeviceViewModel","wifiAccount=>${it.wifiAccount.value}")
//               Log.i("DeviceViewModel","wifiPassword=>${it.wifiPassword.value}")
               temperatureName.value = it.temperatureName.value
//               if (bedType > 0) bedTypeName.postValue(bedType.toBedName(context)) else bedTypeName.postValue("")
//               setBedType.value = bedType
               hasTemperature.postValue(radarRepository.isHasTemperature())
           }
       }
    }

//    fun getAccountInfo(){
//        accountInfo = deviceRepository.getAccountInfo()
//    }

    fun saveWifiData(wifiAccount: String, wifiPassword: String) = deviceRepository.saveWifiData(wifiAccount = wifiAccount, wifiPassword = wifiPassword)

    //type:1->綁定, 0-> 解綁 ; deviceType:0->生物雷達, 1-> 空氣盒子
    suspend fun unBindDevice(context: Context) = withContext(Dispatchers.IO) { deviceRepository.deviceBindingRequest(context,null,0,entity.deviceType)}

    fun saveDeviceInfo()= deviceRepository.saveDeviceInfo(type = 0, deviceType = entity.deviceType, device = null)

    private fun bleConnect(mac: String, bleConnectListener: BleConnectListener)  {
        with(deviceRepository){
            connectDevice(entity.deviceType,mac)
            bleConnect(entity.deviceType,bleConnectListener)
        }
    }

    fun bleDisconnect() = deviceRepository.bleDisconnect(entity.deviceType)

    //type 0=>wifi 1=>bedType
    fun runBleConnect(){
        Log.i("DeviceViewModel","dataSend")
//        connectFlag.value = type
        isDataSend.postValue(true)
        bleConnect(entity.deviceMac, bleConnectListener)
    }

    private val bleConnectListener = object : BleConnectListener {
        override fun onConnectSuccess(device: BluetoothDevice) {
            Log.i("DeviceViewModel","bleConnectListener onConnectSuccess 藍芽連結成功...")
            deviceRepository.isConnect(entity.deviceType)
            isDataSend.postValue(false)
//            throwMessage.value = appContext.value!!.getString(R.string.connect_success)+"\n"+ appContext.value!!.getString(R.string.try_again)
//            throwMessage.value = appContext.value!!.getString(R.string.connect_success)
            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.connect_success))
            isBleConnectSuccess.postValue(true)

        }

        override fun onConnectFinished() {
            Log.i("DeviceViewModel", "onConnectFinished 藍芽連結完成")
//            bleDisconnect()
            isDataSend.postValue(false)
            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.connect_finish))
            isBleConnectSuccess.postValue(false)
        }

        override fun onConnectFailed() {
            Log.i("DeviceViewModel", "onConnectFinished 藍芽連結失敗")
            isDataSend.postValue(false)
//            bleDisconnect()
            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.failed_connect_device))
            isBleConnectSuccess.postValue(false)
        }
    }


    fun isConnect() = deviceRepository.isConnect(entity.deviceType)


//    fun checkInput() = deviceRepository.checkInput(entity)


//    fun wifiSetData(){
//        isDataSend.postValue(true)
//         runCatching {
//             deviceRepository.wifiSetData(entity,accountInfo)
//         }.onSuccess {
//             isDataSend.postValue(false)
//             throwMessage.value = appContext.value!!.getString(R.string.bind_success)
//         }.onFailure {
//             isDataSend.postValue(false)
//             throwMessage.value = appContext.value!!.getString(R.string.bind_failed)
//         }
//
//    }

//    fun setAppendDistance(context: Context) {
//        isDataSend.postValue(true)
//        runCatching {
//            deviceRepository.setAppendDistance(setBedType.value!!)
//        }.onSuccess {
//            isDataSend.postValue(false)
//            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_success))
//            getDeviceInfo(context)
//        }.onFailure {
//            isDataSend.postValue(false)
//            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_failed))
//        }
//    }

    fun setTemperatureCalibration(temperature: String) {
        main { isSetProgress.value = true }
        runCatching {
            deviceRepository.setTemperatureCalibration(temperature)
        }.onSuccess {
            waitDevice("setTemperatureCalibration")
            getTemperature.postValue(temperature)
//            isSetProgress.postValue(false)
//            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_success))
//            getDeviceInfo()
        }.onFailure {
            main { isSetProgress.value = false }
            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_failed))
        }
    }

    fun waitDevice(msg: String) = Log.i("DeviceViewModel","$msg... waitDevice")


    fun setSettingReceiveCallback()=  deviceRepository.setSettingReceiveCallback(bleSettingReceiveCallback)

    private val bleSettingReceiveCallback = object : BleDataReceiveListener{
        override fun onRadarData(data: ByteArray) {
            super.onRadarData(data)
            Log.i("DeviceViewModel","onRadarData=>${data.toHexStringSpace()}")
            val receiveString = String(data)
            Log.i("DeviceViewModel","bleSettingReceiveCallback receiveString=>$receiveString")
            main{
               isSetProgress.value = false
                when(receiveString){
                    "CONNECT SUCCESS" -> throwMessage.value = ToastAlertEntity(isToastAlert = true, message = appContext.value!!.getString(R.string.device_connect_success))
                    "WIFI CONNECT FAIL" -> throwMessage.value = ToastAlertEntity(isToastAlert = true, message = appContext.value!!.getString(R.string.device_connect_wifi_fail))
                    "SERVER CONNECT FAIL" ->throwMessage.value = ToastAlertEntity(isToastAlert = true, message = appContext.value!!.getString(R.string.device_connect_fail))
                    "LOAD SERVER FAIL" -> throwMessage.value = ToastAlertEntity(isToastAlert = true, message = appContext.value!!.getString(R.string.device_connect_fail))
                    "OTHER FAIL" -> throwMessage.value = ToastAlertEntity(isToastAlert = true, message = appContext.value!!.getString(R.string.device_failure))
                }
                //回傳TMP表示成功
//                if (receiveString.contains("TMP")) {
//                    Log.i("DeviceViewModel","bleSettingReceiveCallback ontains(\"TMP\") =>$receiveString")
//                    throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_success))
//                    getDeviceInfo()
//                }
                if (receiveString.contains("TMP")) {
                    Log.i("DeviceViewModel","bleSettingReceiveCallback ontains(\"TMP\") =>$receiveString")
                    if (getTemperature.value?.isNotBlank() == true)temperatureCalibrationSendServer()
                }
            }

           //1、CONNECT SUCCESS 2、WIFI CONNECT FAIL 3、SERVER CONNECT FAIL 4、LOAD SERVER FAIL 5、OTHER FAIL
            ////成功连接 WiFi 并登陆上后台 //WiFi 连接失败
            ////对服务端连接失败 //登陆后台失败
            ////其他失败情况
        }
    }

    val getTemperature: MutableLiveData<String> = MutableLiveData("")
    val isLogout = MutableLiveData(false)
    suspend fun temperatureCalibrationSendServer() = withContext(Dispatchers.IO){
        main { isSetProgress.value = true }
        runCatching {
            deviceRepository.temperatureCalibrationSendServer(
                temperature = getTemperature.value!!.toFloat()
            )
        }.onSuccess {
            main {
                isSetProgress.value = false
                throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_success))
                getDeviceInfo()
            }
        }.onFailure {
            main {
                isSetProgress.value = false
                if (it.message.isNullOrBlank())  throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.unknown_error_occurred))
                else {
                    val getMessage: Pair<Boolean, String> = it.message!!.apiErrorShow(appContext.value!!)
                    Log.i("MainViewModel", "getUserList onFailure getMessage first=>${getMessage.first}")
                    throwMessage.value =  ToastAlertEntity(message = getMessage.second)
                    if (getMessage.first){
                        Log.i("MainViewModel", "getUserList onFailure getMessage first true")
                        //logout
                        isLogout.value = true
                    }
                }
            }
        }
    }

}