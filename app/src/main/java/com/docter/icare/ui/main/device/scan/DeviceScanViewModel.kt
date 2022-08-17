package com.docter.icare.ui.main.device.scan

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.docter.icare.R
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleScanCallback
import com.docter.icare.data.entities.view.item.DeviceScanItemEntity
import com.docter.icare.data.repositories.DeviceRepository
import com.docter.icare.utils.Coroutines
import com.xwray.groupie.GroupieAdapter
import java.util.concurrent.TimeUnit
import io.reactivex.rxjava3.core.Observable.timer

class DeviceScanViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    private var deviceScanData: MutableList<DeviceScanItemEntity> = mutableListOf()
    val deviceType : MutableLiveData<String> = MutableLiveData("")
    val isScan = MutableLiveData(false)
    val currentItem = MutableLiveData(DeviceScanItemEntity())
    val adapter = GroupieAdapter()

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
            Log.i("DeviceScanViewModel","device Scan onScanFinish")
//            isScan.postValue(false)
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

    //type=1 綁定 , = 0 解綁
    suspend fun deviceBindingRequest(context: Context, device: BluetoothDevice?, type: Int)  = deviceRepository.deviceBindingRequest(context, device, type, deviceType.value!!)

    fun stopScan() = deviceRepository.stopScan(deviceType.value!!)

    fun isConnect() = deviceRepository.isConnect(deviceType.value!!)

    fun setAppendDistance() =  deviceRepository.setAppendDistance(1)



}