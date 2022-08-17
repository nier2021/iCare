package com.docter.icare.data.repositories

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.docter.icare.R
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.device.air.AirBleDataManager
import com.docter.icare.data.bleUtil.device.radar.RadarBleDataManager
import com.docter.icare.data.entities.device.DeviceInfoEntity
import com.docter.icare.data.entities.view.ExpandableListEntity
import com.docter.icare.data.network.SafeApiRequest
import com.docter.icare.data.network.api.ApiService
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
                preference.getString(SID),
                0,
                if (deviceType == "Radar") {
                    Log.i("MainRepository","deviceUnBindingRequest unDevice Radar=>${deviceInfoEntity.radarDeviceName}")
                    deviceInfoEntity.radarDeviceName
                }else{
                    deviceInfoEntity.airDeviceName
                },

                if (deviceType == "Radar") {
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
}