package com.docter.icare.data.repositories

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.docter.icare.R
import com.docter.icare.data.bleUtil.device.air.AirBleDataManager
import com.docter.icare.data.bleUtil.device.radar.RadarBleDataManager
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleScanCallback
import com.docter.icare.data.entities.view.AccountInfo
import com.docter.icare.data.entities.view.DeviceEntity
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.*
import com.docter.icare.utils.InputException
import com.docter.icare.utils.SidException
import com.docter.icare.utils.isSpecialSymbols
import okhttp3.internal.toHexString

class DeviceRepository (
    private val resource: ResourceProvider,
    private val preference: PreferenceProvider,
    private val radarBleDataManager: RadarBleDataManager,
    private val airBleDataManager: AirBleDataManager,
    private val api: ApiService
) : SafeApiRequest(resource) {

    fun startScan(
        deviceType: String,
        bleScanCallback: BleScanCallback
    ) = if (deviceType == "Radar") radarBleDataManager.startScan(bleScanCallback) else airBleDataManager.startScan(bleScanCallback)

    fun stopScan(deviceType: String) =  if (deviceType == "Radar") radarBleDataManager.stopScan() else airBleDataManager.stopScan()

    fun connectDevice(deviceType: String,mac: String) = if (deviceType == "Radar") radarBleDataManager.connectDevice(mac) else airBleDataManager.connectDevice(mac)

    fun bleConnect(deviceType: String,bleConnectListener: BleConnectListener) = if (deviceType == "Radar") radarBleDataManager.bleConnectListener(bleConnectListener) else airBleDataManager.bleConnectListener(bleConnectListener)
//
    suspend fun deviceBindingRequest(context:Context,device: BluetoothDevice?, type: Int, deviceType: String) =
        apiRequest{
            Log.i("DeviceRepository","deviceBindingRequest device=>$device\n  type=>$type\n deviceType=>$deviceType")
            Log.i("DeviceRepository","deviceBindingRequest SID=>${preference.getString(SID)}")
            api.bindingRadarDevice(
                preference.getString(SID),
                type,
                if (type == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.i("DeviceRepository","deviceBindingRequest ++++++")
                        throw SidException(resource.getString(R.string.ask_permission_again_title))
                    }
                    Log.i("DeviceRepository","deviceBindingRequest --------")
                    device!!.name
                } else {
//                    "Radar"
                    if (deviceType == "Radar") {
                        Log.i("DeviceRepository","deviceBindingRequest device Radar=>${ preference.getString(RADAR_DEVICE_NAME)}")
                        preference.getString(RADAR_DEVICE_NAME)
                    } else {
                        preference.getString(AIR_DEVICE_NAME)
                    }
                },
                if (deviceType ==  "Radar") {
                    Log.i("DeviceRepository","deviceBindingRequest device Radar")
                    0
                } else {
                    Log.i("DeviceRepository","deviceBindingRequest device Air")
                    1
                }
            )
        }.let {
            if (it.success == 1){
//                Log.i("DeviceRepository","deviceBindingRequest bind success=>${it}")
                if (type == 1 && device != null) {
                    Log.i("DeviceRepository","deviceBindingRequest bind success type==1")
                    Log.i("DeviceRepository","deviceBindingRequest bind success  device.name=>${device.name}")
                    Log.i("DeviceRepository","deviceBindingRequest bind success  accountId=>${it.accountId}")
                    saveDevice(type, deviceType,  device.address, device.name,  it.accountId)
//                    preference.set(BED_TYPE,1)
                } else {
                    Log.i("DeviceRepository","deviceBindingRequest bind success type==0")
                    saveDevice(type, deviceType,"","" , "")
//                    preference.set(BED_TYPE,-1)
                }
            }else{
                if (type == 1) {
                    Log.i("DeviceRepository","deviceBindingRequest bind error=>${it.message}")
                    throw  SidException(resource.getString(R.string.bind_failed))
                } else {
                    Log.i("DeviceRepository","deviceBindingRequest  unbind error=>${it.message}")
                    throw  SidException(resource.getString(R.string.unbind_failed))
                }
            }
        }


    fun bleDisconnect(deviceType: String) = if (deviceType == "Radar") radarBleDataManager.disconnect() else airBleDataManager.disconnect()

    private fun saveDevice(type: Int,deviceType: String, deviceAddress: String, deviceName: String, accountId: String){
        if (deviceType == "Radar") {
            Log.i("DeviceRepository","saveDevice deviceName=>$deviceName")
            with(preference){
                set(RADAR_DEVICE_MAC, deviceAddress)
                set(RADAR_DEVICE_NAME, deviceName)
                set(RADAR_DEVICE_ACCOUNT_ID,accountId)
                if (type == 1) set(BED_TYPE,1) else set(BED_TYPE,-1)
            }
        } else {
            Log.i("DeviceRepository","saveDevice ------")
            with(preference){
                set(AIR_DEVICE_MAC,deviceAddress)
                set(AIR_DEVICE_NAME, deviceName)
                set(AIR_DEVICE_ACCOUNT_ID,accountId)
            }
        }
    }

    fun getDeviceInfo(deviceEntity: DeviceEntity):DeviceEntity{
        if (deviceEntity.deviceType == "Radar"){
            with(preference){
                deviceEntity.deviceMac = getString(RADAR_DEVICE_MAC)
                deviceEntity.deviceName = getString(RADAR_DEVICE_NAME,)
//                Log.i("DeviceRepository","getDeviceInfo name=>${deviceEntity.deviceName}")
                deviceEntity.wifiAccount.value = getString(WIFI_NAME)
                deviceEntity.wifiPassword.value = getString(WIFI_PASS)
                deviceEntity.isWifiBind.value = getBoolean(IS_WIFI)
                deviceEntity.bedType = getInt(BED_TYPE,-1)
            }
        }else{
            with(preference){
                deviceEntity.deviceMac =  getString(AIR_DEVICE_MAC)
                deviceEntity.deviceName = getString(AIR_DEVICE_NAME)
//                deviceEntity.deviceAccountId = getString(AIR_DEVICE_ACCOUNT_ID)
                deviceEntity.wifiAccount.value = getString(WIFI_NAME)
                deviceEntity.wifiPassword.value = getString(WIFI_PASS)
                deviceEntity.isWifiBind.value = getBoolean(IS_WIFI)
            }
        }
        return deviceEntity
    }

    fun getAccountInfo() = AccountInfo(preference.getString(ACCOUNT),preference.getString(PASSWORD),preference.getString(SID))

    fun isConnect(deviceType: String)= if (deviceType == "Radar") radarBleDataManager.isConnect else airBleDataManager.isConnect

    fun checkInput(entity: DeviceEntity){
        when {
            entity.wifiAccount.value?.isBlank() == true -> throw InputException(resource.getString(R.string.error_wifi_account_empty))
            entity.wifiAccount.value!!.length > 20 -> throw InputException(resource.getString(R.string.error_wifi_length))

            entity.wifiPassword.value?.isBlank() == true -> throw InputException(resource.getString(R.string.error_wifi_password_empty))
            entity.wifiPassword.value!!.length > 20 -> throw InputException(resource.getString(R.string.error_wifi_length))
            entity.wifiAccount.value!!.isSpecialSymbols() || entity.wifiPassword.value!!.isSpecialSymbols()-> throw InputException(resource.getString(R.string.error_wifi_special_symbols))
        }
    }

    fun wifiSetData(entity: DeviceEntity,accountInfo: AccountInfo){
        if (entity.wifiAccount.value?.isEmpty() == true || entity.wifiPassword.value?.isEmpty() == true){
            Log.i("DeviceRepository","wifiSet wifi帳密為空")
           return
        }
        if (entity.wifiAccount.value!!.length >20 || entity.wifiPassword.value!!.length >20){
            Log.i("DeviceRepository","wifiSet wifi帳密為超過20字元")
            return
        }


        if (accountInfo.account.isEmpty() || accountInfo.password.isEmpty() || accountInfo.sid.isEmpty()){
            Log.i("DeviceRepository","wifiSet user帳密sid為空")
           return
        }

        if (accountInfo.account.length >20 ||  accountInfo.password.length >20  || accountInfo.sid.length >20){
            Log.i("DeviceRepository","wifiSet user帳密sid為超過20字元")
            return
        }

        if (entity.deviceType == "Radar" && !radarBleDataManager.isConnect){
            Log.i("DeviceRepository","wifiSet Radar isConnect false")
            return
        }

        if (entity.deviceType == "Air" && !airBleDataManager.isConnect){
            Log.i("DeviceRepository","wifiSet Air isConnect false")
            return
        }

        val sendData = "MOSA${entity.wifiAccount.value},${entity.wifiPassword.value},${
            accountInfo.account
        },${
            accountInfo.password
        },${
            accountInfo.sid
        }"


        if (entity.deviceType == "Radar") {
            radarBleDataManager.writeValue(sendData)
            Log.i("DeviceRepository","wifiSet Radar writeValue")
        } else {
            airBleDataManager.writeValue(sendData)
            Log.i("DeviceRepository","wifiSet Air writeValue")
//            AirBleDataManager.getInstance(requireActivity()).writeValue(sendData)
        }
        Log.i("DeviceRepository","wifiSetData sendData=>$sendData")
        with(preference){
            Log.i("DeviceRepository","wifiSet save wifi name pass")
            set(WIFI_NAME, entity.wifiAccount.value!!)
            set(WIFI_PASS, entity.wifiPassword.value!!)
            set(IS_WIFI, true)
        }
    }

    //1->a:80 b:100 c:120 d:180 , 2->a:80 b:100 c:150 d:200 , 3->a:80 b:110 c:150 d:200 , 4->a:80 b:110 c:150 d:200
     fun setAppendDistance(bedType: Int){
         var isSend = false
         Log.i("DeviceRepository", "setAppendDistance")
//         val distanceArray = arrayListOf<Int>()
        val distanceArray = arrayListOf<String>()
//        distanceArray.add(8)
//        when(bedType){
//            2 ->  distanceArray.add(10)
//            3 ->  distanceArray.add(11)
//            4 ->  distanceArray.add(11)
//            else ->  distanceArray.add(10)
//        }
//        when(bedType){
//            1 -> distanceArray.add(12)
//            else ->  distanceArray.add(15)
//        }
//        when(bedType){
//            1 -> distanceArray.add(18)
//            else ->   distanceArray.add(20)
//        }
//         distanceArray.add(80)
//         when(bedType){
//             2 ->  distanceArray.add(100)
//             3 ->  distanceArray.add(110)
//             4 ->  distanceArray.add(110)
//             else ->  distanceArray.add(100)
//         }
//        when(bedType){
//            1 -> distanceArray.add(120)
//            else ->  distanceArray.add(150)
//        }
//        when(bedType){
//            1 -> distanceArray.add(180)
//            else ->   distanceArray.add(200)
//        }
        distanceArray.add(80.toHexString())
        when(bedType){
            2 ->  distanceArray.add(100.toHexString())
            3 ->  distanceArray.add(110.toHexString())
            4 ->  distanceArray.add(110.toHexString())
            else ->  distanceArray.add(100.toHexString())
        }
        when(bedType){
            1 -> distanceArray.add(120.toHexString())
            else ->  distanceArray.add(150.toHexString())
        }
        when(bedType){
            1 -> distanceArray.add(180.toHexString())
            else ->   distanceArray.add(200.toHexString())
        }
//        Log.i("RegulateDistanceAutoFragment", "setAppendDistance distanceArray [0]=>${distanceArray[0]},[1]=>${distanceArray[1]},[2]=>${distanceArray[2]},[3]=>${distanceArray[3]}")
        if (!isSend) {
            Log.i("RegulateDistanceAutoFragment", "setAppendDistance !isSend")
            val regulateData = "TMD".toByteArray()
//            Log.i("RegulateDistanceAutoFragment", "setAppendDistance regulateData =>${regulateData}")
//            //30公分誤差
//            val lieFlatValue = distanceArray[0]+3
//            val sideValue = distanceArray[1]+3
//            val bedsideValue = distanceArray[2]+3
//            val endBedValue = distanceArray[3]+3
            //0誤差
            val lieFlatValue = distanceArray[0]
            val sideValue = distanceArray[1]
            val bedsideValue = distanceArray[2]
            val endBedValue = distanceArray[3]
//            val test = "TMD11111111"
//            regulateDistanceResultTextView.text = "lieFlatValue=>$lieFlatValue , sideValue=>$sideValue ,bedsideValue=>$bedsideValue ,endBedValue=>$endBedValue"
            Log.i("RegulateDistanceAutoFragment", "lieFlatValue=>$lieFlatValue , sideValue=>$sideValue ,bedsideValue=>$bedsideValue ,endBedValue=>$endBedValue")
            radarBleDataManager.writeRegulateValue(regulateData + lieFlatValue.toByte() + sideValue.toByte() + bedsideValue.toByte() + endBedValue.toByte())
            Log.i("RegulateDistanceAutoFragment","********")
            preference.set(BED_TYPE,bedType)
            isSend = true

        }
    }

}
