package com.docter.icare.ui.main

import android.content.Context
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination
import com.docter.icare.R
import com.docter.icare.data.entities.device.DeviceInfoEntity
import com.docter.icare.data.entities.view.MainEntity
import com.docter.icare.data.repositories.MainRepository
import com.docter.icare.data.repositories.RadarRepository
import com.docter.icare.ui.main.expandableList.ExpandableListAdapter
import com.docter.icare.utils.Coroutines.main
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.concurrent.fixedRateTimer

class MainViewModel(
    private val mainRepository: MainRepository,
    private val radarRepository: RadarRepository,
) : ViewModel() {
    val entity = MainEntity().apply {
        mainRepository.getName().let { getName ->
            if (getName.isNotEmpty()){
                name.value = getName
            }else{
                name.value ="遊客"
            }
        }
    }

    var toolbarClickListener: ToolbarClickListener? = null

    var adapter: ExpandableListAdapter? = null

//    val isRadarDeviceBind :MutableLiveData<Boolean> = MutableLiveData(false)

    fun getExpandableListData(context: Context ) {
        adapter = ExpandableListAdapter(context,mainRepository.getExpandableListData())
    }

    fun destinationChange(
        destination: NavDestination
    ) {

        toolbarClickListener = null

        main {
            entity.nowFragment.value = when (destination.id) {
                R.id.bedsideMonitorFragment -> MainEntity.BEDSIDE_MONITOR
                R.id.airQualityIndexFragment -> MainEntity.AIR_QUALITY_INDEX
                R.id.activityMonitoringFragment -> MainEntity.ACTIVITY_MONITORING
                R.id.exceptionFragment -> MainEntity.EXCEPTION
                R.id.deviceScanFragment ->  MainEntity.DEVICE_SCAN
                R.id.deviceFragment -> MainEntity.DEVICE
                R.id.personInfoContentFragment ->  MainEntity.PERSON_INFO_CONTENT
                R.id.personInfoEditFragment -> MainEntity.PERSON_INFO_EDIT
                else -> -1
            }

            entity.isDrawerBottomViewShow.value = when (destination.id) {
                R.id.bedsideMonitorFragment, R.id.airQualityIndexFragment,R.id.activityMonitoringFragment -> true
                else -> false
            }
        }
    }

    //設置右上角功能
    fun menuClick(
        item: MenuItem
    ) = when (item.itemId) {

        R.id.exception -> {
            Log.i("MainViewModel","menuClick exception")
            toolbarClickListener?.onExceptionClick()
            true
        }
//
//        R.id.edit ->{
//            toolbarClickListener?.onEditClick()
//            true
//        }
//
//        R.id.done ->{
//            toolbarClickListener?.onDoneClick()
//            true
//        }
//
//        R.id.share ->{
//            toolbarClickListener?.onShareClick()
//            true
//        }

        else -> false
    }

    fun getShowIcon(): Int = when(entity.nowFragment.value){
        MainEntity.BEDSIDE_MONITOR, MainEntity.AIR_QUALITY_INDEX,MainEntity.ACTIVITY_MONITORING -> 1 //有異常icon
        MainEntity.PERSON_INFO_CONTENT -> 2// Edit icon
        else -> -1 //無icon
    }
//    fun hasException(
//        destination: NavDestination
//    ) = when (destination.id) {
//
//        R.id.bedsideMonitorFragment,
////        R.id.airQualityIndexFragment
//        -> true
//
//        else -> false
//    }

    //取得後台登入帳號裝置狀態
    suspend fun getAccountDeviceInfo() = withContext(Dispatchers.IO) {mainRepository.getAccountDeviceInfo()}

    //以下登出
    fun checkDevice(): DeviceInfoEntity?{
        val deviceInfoEntity: DeviceInfoEntity = mainRepository.checkDevice()
        val isRadarBind: Boolean = deviceInfoEntity.radarDeviceName.isNotEmpty() && deviceInfoEntity.radarDeviceMac.isNotEmpty()
        val isAirBind: Boolean = deviceInfoEntity.airDeviceName.isNotEmpty() && deviceInfoEntity.airDeviceMac.isNotEmpty()
        deviceInfoEntity.let {
            it.isRadarBind = isRadarBind
            it.isAirBind = isAirBind
        }
        return if (isRadarBind || isAirBind )  deviceInfoEntity else null
    }

    fun bleDisconnect(deviceInfoEntity: DeviceInfoEntity) {
        if (deviceInfoEntity.isRadarBind) mainRepository.isConnect("Radar").let { if (it) mainRepository.radarBleDisconnect("Radar") }
        if (deviceInfoEntity.isAirBind)  mainRepository.isConnect("Air").let { if (it) mainRepository.radarBleDisconnect("Air") }
    }

    suspend fun unBindDevice(deviceInfoEntity: DeviceInfoEntity) = withContext(Dispatchers.IO) {
        if (deviceInfoEntity.isRadarBind) {
            mainRepository.deviceUnBindingRequest("Radar" ,deviceInfoEntity)
        }
        if (deviceInfoEntity.isAirBind) {
            mainRepository.deviceUnBindingRequest( "Air" ,deviceInfoEntity)
        }
    }

    fun logout()= mainRepository.logout()

    fun stopSocket() = radarRepository.closeSocket()

}