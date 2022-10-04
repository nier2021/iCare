package com.docter.icare.data.repositories

import android.util.Log
import com.docter.icare.R
import com.docter.icare.data.bleUtil.device.air.AirBleDataManager
import com.docter.icare.data.bleUtil.device.radar.RadarBleDataManager
import com.docter.icare.data.entities.device.DeviceInfoEntity
import com.docter.icare.data.entities.view.ExpandableListEntity
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.network.api.response.CheckDeviceResponse
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.resource.*
import com.docter.icare.utils.SidException

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
        deviceList.add(resource.getString(R.string.air_quality))
        deviceList.add(resource.getString(R.string.activity_monitoring))

        val aboutList = ArrayList<String>()
        aboutList.add(resource.getString(R.string.about))
        aboutList.add(resource.getString(R.string.privacy_policy))
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
            bedType = preference.getInt(BED_TYPE,-1),
            airDeviceName = preference.getString(AIR_DEVICE_NAME,""),
            airDeviceMac = preference.getString(AIR_DEVICE_MAC,""),
            wifiAccount = preference.getString(WIFI_NAME),
            wifiPassword = preference.getString(WIFI_PASS),
            isWifiBind = preference.getBoolean(IS_WIFI)
        )

    fun isConnect(deviceType: String) = if (deviceType == "Radar" ) radarBleDataManager.isConnect else airBleDataManager.isConnect
    fun radarBleDisconnect(deviceType: String) = if (deviceType == "Radar" )radarBleDataManager.disconnect() else airBleDataManager.disconnect()

    suspend fun deviceUnBindingRequest( deviceType: String, deviceInfoEntity: DeviceInfoEntity) =
        apiRequest{
            Log.i("MainRepository","deviceBindingRequest SID=>${preference.getString(SID)}")
            api.bindingRadarDevice(
                sid = preference.getString(SID),
                type = 0,
                serialNumber = if (deviceType == "Radar") {
                    Log.i("MainRepository","deviceUnBindingRequest unDevice Radar DeviceName=>${deviceInfoEntity.radarDeviceName}")
                    deviceInfoEntity.radarDeviceName
                }else{
                    deviceInfoEntity.airDeviceName
                },
                macAddress =  if (deviceType == "Radar") {
                    Log.i("MainRepository","deviceUnBindingRequest unDevice Radar DeviceMac=>${deviceInfoEntity.radarDeviceMac}")
                    deviceInfoEntity.radarDeviceMac
                }else{
                    deviceInfoEntity.airDeviceMac
                },

               deviceType =  if (deviceType == "Radar") {
                    Log.i("DeviceRepository","deviceBindingRequest device Radar")
                    0
                } else {
                    Log.i("DeviceRepository","deviceBindingRequest device Air")
                    1
                }
            )
        }.let {
            if (it.success == 1){
                Log.i("DeviceRepository","deviceBindingRequest bind success type==0")
                saveDevice(deviceType)
            }else{
                if (deviceType == "Radar") {
                    Log.i("DeviceRepository","deviceBindingRequest device Radar")
                    throw  SidException(resource.getString(R.string.unbind_failed)+ "=>" +resource.getString(R.string.bioradar))
                } else {
                    Log.i("DeviceRepository","deviceBindingRequest device Air")
                    throw  SidException(resource.getString(R.string.unbind_failed)+ "=>" +resource.getString(R.string.air_box))
                }
            }
        }

    private fun saveDevice(deviceType: String){
        if (deviceType == "Radar") {
            Log.i("DeviceRepository","saveDevice Radar")
            with(preference){
                set(RADAR_DEVICE_MAC, "")
                set(RADAR_DEVICE_NAME, "")
                set(RADAR_DEVICE_ACCOUNT_ID,"")
                set(BED_TYPE,-1)
            }
        } else {
            Log.i("DeviceRepository","saveDevice air")
            with(preference){
                set(AIR_DEVICE_MAC,"")
                set(AIR_DEVICE_NAME, "")
                set(AIR_DEVICE_ACCOUNT_ID,"")
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
            set(SID, "")
            set(NAME, "")
        }
    }


    suspend fun getAccountDeviceInfo():MutableList<CheckDeviceResponse.DeviceInfo> = apiRequest{
        Log.i("DeviceRepository","getAccountDeviceInfo")
//        api.checkDevice(preference.getString(SID))
        api.checkDevice("hueuan4")
    }.let { res ->
        if (res.success == 1){
            Log.i("DeviceRepository","getAccountDeviceInfo success data=>${res.data}")
            if (res.data.isNotEmpty()){
//                getDeviceInfoList.forEach { saveDevice(it) }
                setDevice(0,res.data.firstOrNull { it.deviceType == 0 })
                setDevice(1,res.data.firstOrNull { it.deviceType == 1 })
                //感知器會有多台 無法使用first 假如deviceType是4
                val getActivityDeviceList: MutableList<CheckDeviceResponse.DeviceInfo> = mutableListOf()
                res.data.map { if (it.deviceType == 4) getActivityDeviceList.add(it) }
                if (getActivityDeviceList.isNotEmpty()) getActivityDeviceList.map { Log.i("DeviceRepository","看如何新增多台紀錄 db?") } else Log.i("DeviceRepository","看如何移除多台紀錄 db?")
            }else{
                //表示無裝置
                allClean()
            }

//            if (getDeviceInfoList.isNotEmpty()) Log.i("getAccountDeviceInfo","getDeviceInfoList[0] serialNumber=>${getDeviceInfoList[0].serialNumber}\n deviceType=>${getDeviceInfoList[0].deviceType}")
            return res.data.toMutableList()
        }else throw SidException(res.message)

    }

    private fun setDevice(deviceType: Int, device: CheckDeviceResponse.DeviceInfo? ){
        //正式測試ok再開
        when (deviceType){
            0 ->{
                if (device != null ){
                    Log.i("DeviceRepository","save air")
//                    with(preference){
//                        set(AIR_DEVICE_NAME, device.serialNumber)
//                        set(AIR_DEVICE_MAC, device.macAddress)
//                        set(AIR_DEVICE_ACCOUNT_ID, device.accountId)
//                    }
                }else{
                    Log.i("DeviceRepository","remove air")
//                    with(preference){
//                        set(AIR_DEVICE_NAME, "")
//                        set(AIR_DEVICE_MAC,"")
//                        set(AIR_DEVICE_ACCOUNT_ID,"")
//                    }
                }
            }
            1 ->{
                if (device != null ){
                    Log.i("DeviceRepository","save radar")
//                    with(preference){
//                        set(RADAR_DEVICE_NAME, device.serialNumber)
//                        set(RADAR_DEVICE_MAC, device.macAddress)
//                        set(RADAR_DEVICE_ACCOUNT_ID,device.accountId)
//                        set(BED_TYPE,1)
//                    }
                }else{
                    Log.i("DeviceRepository","remove radar")
//                    with(preference){
//                        set(RADAR_DEVICE_MAC, "")
//                        set(RADAR_DEVICE_NAME, "")
//                        set(RADAR_DEVICE_ACCOUNT_ID,"")
//                        set(BED_TYPE,-1)
//                    }
                }
            }
        }
    }

    private fun allClean(){
        Log.i("DeviceRepository","remove all")
        //正式測試ok再開
//        with(preference){
//            //雷達波
//            set(RADAR_DEVICE_MAC, "")
//            set(RADAR_DEVICE_NAME, "")
//            set(RADAR_DEVICE_ACCOUNT_ID,"")
//            set(BED_TYPE,-1)
//            //空氣盒子
//            set(AIR_DEVICE_MAC,"")
//            set(AIR_DEVICE_NAME, "")
//            set(AIR_DEVICE_ACCOUNT_ID,"")
//        }
        //未做多台感知器移除方法
    }
}