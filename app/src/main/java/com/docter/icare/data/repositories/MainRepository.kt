package com.docter.icare.data.repositories

import android.content.Context
import android.util.Log
import com.docter.icare.R
import com.docter.icare.data.bleUtil.device.air.AirBleDataManager
import com.docter.icare.data.bleUtil.device.radar.RadarBleDataManager
import com.docter.icare.data.entities.device.DeviceInfoEntity
import com.docter.icare.data.entities.view.ExpandableListEntity
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.network.api.apiErrorShow
import com.docter.icare.data.network.api.response.CheckDeviceResponse
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.*
import com.docter.icare.utils.SidException
import com.docter.icare.utils.isHasTemperature
import com.docter.icare.utils.snackbar

class MainRepository(
    private val resource: ResourceProvider,
    private val preference: PreferenceProvider,
    private val api: ApiService,
    private val radarBleDataManager: RadarBleDataManager,
    private val airBleDataManager: AirBleDataManager,
): SafeApiRequest(resource){
    fun getName() = preference.getString(NAME)

    fun getExpandableListData(): ExpandableListEntity{
        //textList
        val titleData = HashMap<String, List<String>>()
        val deviceList = ArrayList<String>()
        deviceList.add(resource.getString(R.string.bedside_monitor_text))
//        deviceList.add(resource.getString(R.string.air_quality))//之後有做功能再開
//        deviceList.add(resource.getString(R.string.activity_monitoring))//之後有做功能再開

        val aboutList = ArrayList<String>()
//        aboutList.add(resource.getString(R.string.about))//之後有做功能再開
//        aboutList.add(resource.getString(R.string.privacy_policy))//之後有做功能再開
        aboutList.add(resource.getString(R.string.logout))

        titleData[resource.getString(R.string.device_management_text)] = deviceList
        titleData[resource.getString(R.string.about)] = aboutList

        //icon
        val imgList = arrayListOf(R.drawable.icon_drawer_device,R.drawable.icon_drawer_about)
        return ExpandableListEntity(titleList = ArrayList(titleData.keys.sortedBy { it }), dataList = titleData, imgList = imgList )

    }

    fun checkDevice()=
        DeviceInfoEntity(
            radarDeviceName = preference.getString(RADAR_DEVICE_NAME,""),
            radarDeviceMac =  preference.getString(RADAR_DEVICE_MAC,""),
            airDeviceName = preference.getString(AIR_DEVICE_NAME,""),
            airDeviceMac = preference.getString(AIR_DEVICE_MAC,""),
            wifiAccount = preference.getString(WIFI_NAME),
            wifiPassword = preference.getString(WIFI_PASS),
            isWifiBind = preference.getBoolean(IS_WIFI)
        )

    fun isConnect(deviceType: String) = if (deviceType == "Radar" ) radarBleDataManager.isConnect else airBleDataManager.isConnect
    fun radarBleDisconnect(deviceType: String) = if (deviceType == "Radar" )radarBleDataManager.disconnect() else airBleDataManager.disconnect()

//    suspend fun deviceUnBindingRequest( deviceType: String, deviceInfoEntity: DeviceInfoEntity) =
//        apiRequest{
//            Log.i("MainRepository","deviceBindingRequest SID=>${preference.getString(SID)}")
//            api.bindingDevice(
//                sid = preference.getString(SID),
//                type = 0,
//                serialNumber = if (deviceType == "Radar") {
//                    Log.i("MainRepository","deviceUnBindingRequest unDevice Radar DeviceName=>${deviceInfoEntity.radarDeviceName}")
//                    deviceInfoEntity.radarDeviceName
//                }else{
//                    deviceInfoEntity.airDeviceName
//                },
//                macAddress =  if (deviceType == "Radar") {
//                    Log.i("MainRepository","deviceUnBindingRequest unDevice Radar DeviceMac=>${deviceInfoEntity.radarDeviceMac}")
//                    deviceInfoEntity.radarDeviceMac
//                }else{
//                    deviceInfoEntity.airDeviceMac
//                },
//
//               deviceType =  if (deviceType == "Radar") {
//                    Log.i("DeviceRepository","deviceBindingRequest device Radar")
//                    0
//                } else {
//                    Log.i("DeviceRepository","deviceBindingRequest device Air")
//                    1
//                }
//            )
//        }.let {
//            if (it.success == 1){
//                Log.i("DeviceRepository","deviceBindingRequest bind success type==0")
//                saveDevice(deviceType)
//            }else{
//                if (deviceType == "Radar") {
//                    Log.i("DeviceRepository","deviceBindingRequest device Radar")
//                    throw  SidException(resource.getString(R.string.unbind_failed)+ "=>" +resource.getString(R.string.bioradar))
//                } else {
//                    Log.i("DeviceRepository","deviceBindingRequest device Air")
//                    throw  SidException(resource.getString(R.string.unbind_failed)+ "=>" +resource.getString(R.string.air_box))
//                }
//            }
//        }

    private fun saveDevice(deviceType: String){
        if (deviceType == "Radar") {
            Log.i("DeviceRepository","saveDevice Radar")
            with(preference){
                set(RADAR_DEVICE_MAC, "")
                set(RADAR_DEVICE_NAME, "")
            }
        } else {
            Log.i("DeviceRepository","saveDevice air")
            with(preference){
                set(AIR_DEVICE_MAC,"")
                set(AIR_DEVICE_NAME, "")
            }
        }
        preference.set(WIFI_NAME,"")
        preference.set(WIFI_PASS,"")
        preference.set(IS_WIFI,false)
    }

    fun logout(){
        with(preference){
            set(ACCOUNT, "")
            set(PASSWORD, "")
            set(ACCOUNT_ID,"")
            set(TOKEN, "")
            set(NAME, "")
        }
    }


//    suspend fun getAccountDeviceInfo(context: Context):MutableList<CheckDeviceResponse.DeviceInfo> = apiRequest{
//        Log.i("DeviceRepository","getAccountDeviceInfo")
//        api.checkDevice(preference.getString(TOKEN,""))
//    }.let { res ->
//        if (res.success == 1){
//            Log.i("DeviceRepository","getAccountDeviceInfo success data=>${res.data}")
//            if (res.data.isNotEmpty()){
//                //deviceType 0:ra  1:air
//                setDevice(0,res.data.firstOrNull { it.deviceType == 0 })
//                setDevice(1,res.data.firstOrNull { it.deviceType == 1 })
//                //感知器會有多台 無法使用first 假如deviceType是4?
//                val getActivityDeviceList: MutableList<CheckDeviceResponse.DeviceInfo> = mutableListOf()
//                res.data.map { if (it.deviceType == 4) getActivityDeviceList.add(it) }
//                if (getActivityDeviceList.isNotEmpty()) getActivityDeviceList.map { Log.i("DeviceRepository","看如何新增多台紀錄 db?") } else Log.i("DeviceRepository","看如何移除多台紀錄 db?")
//            }else{
//                //表示無裝置
//                Log.i("DeviceRepository","getAccountDeviceInfo 無裝置")
//                allClean()
//            }
//
////            if (getDeviceInfoList.isNotEmpty()) Log.i("getAccountDeviceInfo","getDeviceInfoList[0] serialNumber=>${getDeviceInfoList[0].serialNumber}\n deviceType=>${getDeviceInfoList[0].deviceType}")
//            return res.data.toMutableList()
//        } else throw if (res.message.isBlank()) SidException(resource.getString(R.string.unknown_error_occurred)) else SidException(res.message.apiErrorShow(context).second)
//
////            throw SidException(res.message)
//
//    }

    suspend fun getAccountDeviceInfo() = apiRequest{ api.checkDevice(preference.getString(TOKEN,"")) }

    fun saveAccountDeviceInfo(data: CheckDeviceResponse){
        if (data.data.isNotEmpty()){
            //deviceType 0:ra  1:air
            setDevice(0,data.data.firstOrNull { it.deviceType == 0 })
            setDevice(1,data.data.firstOrNull { it.deviceType == 1 })
            //感知器會有多台 無法使用first 假如deviceType是4?
            val getActivityDeviceList: MutableList<CheckDeviceResponse.DeviceInfo> = mutableListOf()
            data.data.map { if (it.deviceType == 4) getActivityDeviceList.add(it) }
            if (getActivityDeviceList.isNotEmpty()) getActivityDeviceList.map { Log.i("DeviceRepository","看如何新增多台紀錄 db?") } else Log.i("DeviceRepository","看如何移除多台紀錄 db?")
        }else{
            //表示無裝置
            Log.i("DeviceRepository","getAccountDeviceInfo 無裝置")
            allClean()
        }
    }

    private fun setDevice(deviceType: Int, device: CheckDeviceResponse.DeviceInfo? ){
        //正式測試ok再開
        when (deviceType){
            0 ->{
                if (device != null ){
                    Log.i("DeviceRepository","save radar")
                    Log.i("DeviceRepository","save radar serialNumber=>${device.serialNumber} \n macAddress=>${device.macAddress} \n accountId=>${device.accountId} ")
                    with(preference){
                        set(RADAR_DEVICE_NAME, device.serialNumber)
                        set(RADAR_DEVICE_MAC, device.macAddress)
                    }
                    if(device.serialNumber.isHasTemperature()) preference.set(RADAR_TEMPERATURE,device.temperatureCorrection.toString())
                }else{
                    Log.i("DeviceRepository","remove radar")
                    with(preference){
                        set(RADAR_DEVICE_MAC, "")
                        set(RADAR_DEVICE_NAME, "")
                        set(RADAR_TEMPERATURE,"")
                    }
                }
            }
            1 ->{
                if (device != null ){
                    Log.i("DeviceRepository","save air")
                    with(preference){
                        set(AIR_DEVICE_NAME, device.serialNumber)
                        set(AIR_DEVICE_MAC, device.macAddress)
                    }
                }else{
                    Log.i("DeviceRepository","remove air")
                    with(preference){
                        set(AIR_DEVICE_NAME, "")
                        set(AIR_DEVICE_MAC,"")
                    }
                }
            }
        }
    }

    private fun allClean(){
        Log.i("DeviceRepository","remove all")
        //正式測試ok再開
        with(preference){
            //雷達波
            set(RADAR_DEVICE_MAC, "")
            set(RADAR_DEVICE_NAME, "")
            //空氣盒子
            set(AIR_DEVICE_MAC,"")
            set(AIR_DEVICE_NAME, "")
        }
        //未做多台感知器移除方法
    }

    suspend fun logoutServer() =  apiRequest { api.logout(preference.getString(TOKEN,""))}
}