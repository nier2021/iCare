package com.docter.icare.ui.main.device.scan

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.docter.icare.R
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleDataReceiveListener
import com.docter.icare.data.bleUtil.bleInterface.BleScanCallback
import com.docter.icare.data.entities.device.ToastAlertEntity
import com.docter.icare.data.entities.view.AccountInfo
import com.docter.icare.data.entities.view.item.DeviceScanItemEntity
import com.docter.icare.data.repositories.DeviceRepository
import com.docter.icare.data.repositories.RadarRepository
import com.docter.icare.utils.Coroutines
import com.docter.icare.utils.toHexStringSpace
import com.xwray.groupie.GroupieAdapter
import java.util.concurrent.TimeUnit
import io.reactivex.rxjava3.core.Observable.timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceScanViewModel(
    private val deviceRepository: DeviceRepository,
    private val radarRepository: RadarRepository
) : ViewModel() {
    private var deviceScanData: MutableList<DeviceScanItemEntity> = mutableListOf()
    val deviceType : MutableLiveData<String> = MutableLiveData("")
    val isScan = MutableLiveData(false)
    val currentItem = MutableLiveData(DeviceScanItemEntity())
    val adapter = GroupieAdapter()
    var accountInfo = AccountInfo()


    fun getAccountInfo(){
        accountInfo = deviceRepository.getAccountInfo()
    }

    fun startScan()  {
        deviceScanData.clear()
        isScan.postValue(true)
        deviceRepository.startScan(deviceType.value!!, bleScanCallback)
        timer(8000, TimeUnit.MILLISECONDS).subscribe {
            deviceRepository.stopScan(deviceType.value!!)
            isScan.postValue(false)
        }
    }

    private val bleScanCallback = object : BleScanCallback {
        override fun onDeviceFound(entity: DeviceScanItemEntity) {
//            Log.i("DeviceScanViewModel","device Scan onDeviceFound Callback name =>${entity.name} \n mac =>${entity.mac}")

            if (!deviceScanData.any {
                    it.name == entity.name
            }) {
                if (entity.name.contains("TM")) {
                    deviceScanData.add(entity)
                }

            }
        }

        override fun onScanFinish() {
//            Log.i("DeviceScanViewModel","device Scan onScanFinish")
            Coroutines.main {
                with(adapter) {
                    clear()
                    addAll(deviceScanData.map { data -> DeviceScanItem(data) { currentItem.value = it } })
                }
            }
        }
    }

    fun bleConnect(mac: String ,bleConnectListener: BleConnectListener)  {
        with(deviceRepository){
            connectDevice(deviceType.value!!,mac)
            bleConnect(deviceType.value!!,bleConnectListener)
        }
    }

    fun bleDisconnect() = deviceRepository.bleDisconnect(deviceType.value!!)

    //type:1->綁定, 0-> 解綁 ; deviceType:0->生物雷達, 1-> 空氣盒子
    suspend fun deviceBindingRequest(context: Context, device: BluetoothDevice?, type: Int)  = deviceRepository.deviceBindingRequest(context, device, type, deviceType.value!!)

    fun stopScan() = deviceRepository.stopScan(deviceType.value!!)

    fun isConnect() = deviceRepository.isConnect(deviceType.value!!)

    fun wifiSetData() = deviceRepository.wifiSetData(deviceType.value!!,accountInfo)

//    fun setAppendDistance() =  deviceRepository.setAppendDistance(1)

    fun setSettingReceiveCallback(bleSettingReceiveCallback: BleDataReceiveListener)=  deviceRepository.setSettingReceiveCallback(bleSettingReceiveCallback)

    fun isHasTemperature() = radarRepository.isHasTemperature()

    fun setTemperatureCalibration() {
        deviceRepository.setTemperatureCalibration("35")
    }

    fun cleanTemperature() = radarRepository.cleanTemperature()

    fun saveDeviceInfo(type: Int, device: BluetoothDevice?)= deviceRepository.saveDeviceInfo(type = type, deviceType = deviceType.value!!, device = device)

    suspend fun temperatureCalibrationSendServer() = withContext(Dispatchers.IO){ deviceRepository.temperatureCalibrationSendServer(
        temperature = 35f
    ) }

}