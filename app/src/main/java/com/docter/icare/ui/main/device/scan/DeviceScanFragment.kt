package com.docter.icare.ui.main.device.scan

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.docter.icare.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.bleUtil.bleInterface.BleDataReceiveListener
import com.docter.icare.data.entities.device.ToastAlertEntity
import com.docter.icare.databinding.FragmentDeviceScanBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.ui.main.MainActivity
import com.docter.icare.ui.main.MainViewModel
import com.docter.icare.utils.*
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.view.RecyclerDivider
import com.docter.icare.view.dialog.CustomAlertDialog
import com.docter.icare.view.dialog.CustomProgressDialog
import com.docter.icare.view.dialog.ToastAlertDialog
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

class DeviceScanFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var binding: FragmentDeviceScanBinding

    private val factory: DeviceScanViewModelFactory by instance()
    private val viewModel: DeviceScanViewModel by lazy { ViewModelProvider(this, factory)[DeviceScanViewModel::class.java] }

    private val activityViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }

    private val scanProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity(), R.string.scanning_text) }

    private val connectProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog( requireActivity(), R.string.connecting) }

    private val setProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog( requireActivity(), R.string.set_up) }

    private val confirmBindDeviceDialog: CustomAlertDialog by lazy {
        CustomAlertDialog(requireActivity())
            .setTitle(R.string.device_setting)
            .setMessage(R.string.confirm_bind_device_message)
            .setPositiveButton(R.string.yes_text)
            .setNegativeButton(R.string.no_text)
    }

    private val toastAlertDialog: ToastAlertDialog by lazy { ToastAlertDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceScanBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            click = this@DeviceScanFragment

            arguments?.get("deviceType").let {
                Log.i("DeviceScanFragment","deviceType =>$it")
                viewModel.deviceType.value = it as String
            }

            with(deviceList) {
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                setHasFixedSize(true)
                addItemDecoration(RecyclerDivider(requireContext(), RecyclerDivider.Color.PRIMARY))
                adapter = viewModel.adapter
            }
        }
        with(viewModel){
            getAccountInfo()

            setSettingReceiveCallback(bleSettingReceiveCallback)

            isScan.observe(viewLifecycleOwner){
                if (it){
                    scanProgressDialog.show()
                }else scanProgressDialog.dismiss()
            }
            currentItem.observe(viewLifecycleOwner){ it ->
                if (it.name.isNotEmpty() && it.mac.isNotEmpty()){
                    Log.i("DeviceScanFragment","currentItem name=>${it.name} \n mac=>${it.mac}")
                    if (!confirmBindDeviceDialog.isShowing()) confirmBindDeviceDialog.apply {
                        setMessage(getString(R.string.confirm_bind_device_message,it.name))
                        setPositiveButton(R.string.yes_text){ _->
                            main {  connectProgressDialog.show() }
                            viewModel.bleConnect(it.mac,bleConnectListener)
                        }
                    }.show()
                }
            }
        }

        return binding.root
    }

    private val bleConnectListener = object : BleConnectListener {
        override fun onConnectSuccess(device: BluetoothDevice) {
            Log.i("DeviceScanFragment","bleConnectListener onConnectSuccess 藍芽連結成功...")
//            viewModel.bleDisconnect()
            lifecycleScope.launch{
                deviceBindingRequest(device)
            }
        }

        override fun onConnectFinished() {
            Log.i("DeviceScanFragment", "onConnectFinished 藍芽連結完成")
            viewModel.isConnect()
//            viewModel.bleDisconnect()
//            if (deviceType == DeviceType.Radar) {
//                RadarBleDataManager.getInstance(requireActivity()).isConnect = true
//            } else {
//                AirBleDataManager.getInstance(requireActivity()).isConnect = true
//            }

        }

        override fun onConnectFailed() {
            Log.i("DeviceScanViewModel", "onConnectFinished 藍芽連結失敗")
           main { connectProgressDialog.dismiss() }
//            viewModel.bleDisconnect()
            viewModel.isConnect()
            binding.root.snackbar(requireContext().getString(R.string.unbind_failed))
        }
    }

    override fun onClick(view: View) {
        super.onClick(view)
        when(view){
            binding.layoutBtnDeviceScan.root -> {
//                if (activityViewModel.isBluetoothEnabled() == true){
                if (activityViewModel.isBluetoothEnabled(requireContext().applicationContext)){
                    viewModel.startScan()
                } else binding.root.snackbar(R.string.no_open_bluetooth)
            }
        }
    }

    private suspend fun deviceBindingRequest(device: BluetoothDevice?){
//        Log.i("DeviceScanViewModel","deviceBindingRequest")
        withContext(Dispatchers.IO) {
            runCatching {
                viewModel.deviceBindingRequest(context!!.applicationContext, device, 1)
            }.onSuccess {
//                Log.i("DeviceScanViewModel","deviceBindingRequest bind_success")
               val isConnect: Boolean = viewModel.isConnect()
                if (isConnect) wifiSetData()
                else{
                    main { connectProgressDialog.dismiss() }
                    binding.root.snackbar(R.string.failed_connect_device)
                }
            }.onFailure {
                Log.i("DeviceScanViewModel","deviceBindingRequest bind_Failure")
                viewModel.bleDisconnect()
                main { connectProgressDialog.dismiss() }
                it.printStackTrace()
                binding.root.snackbar(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    private fun stop(){
        main {
            scanProgressDialog.dismiss()
//            connectProgressDialog.dismiss()
        }
        with(viewModel){
            stopScan()
//            bleDisconnect()
        }
    }

    private fun wifiSetData(){
        runCatching {
            viewModel.wifiSetData()
        }.onSuccess {
            if (viewModel.deviceType.value == "Radar") waitRadar("wifiSetData")
            else {
                main { connectProgressDialog.dismiss() }
                //未來看空氣盒子或姿態感知要做啥(沒有=> connectProgressDialog.dismiss()
                //                binding.root.snackbar(R.string.bind_success)
                //                requireActivity().onBackPressed())
            }
        }.onFailure {
            main { connectProgressDialog.dismiss() }
            it.printStackTrace()
            binding.root.snackbar(R.string.bind_failed)
        }
    }

    private fun waitRadar(msg:String){
        Log.i("DeviceScanViewModel","$msg... waitRadar")
    }

    private fun goNext(){
        //找裝置名稱是否有溫感器 没有温度的编号开头是TMOT04V1，有温度编号的是TMOT04V2
        val isHasTemperature = viewModel.isHasTemperature()
        Log.i("DeviceScanViewModel","goNext isHasTemperature=>$isHasTemperature")
        if (isHasTemperature) setTemperatureCalibration()
        else{
            main {
                viewModel.cleanTemperature()//清理綁定時的假預設溫度資料
                connectProgressDialog.dismiss()
                toastAlertDialog.apply {
                    setMessage(getString(R.string.bind_success))
                    setButton(){
                        activityViewModel.isChange(true)//webSocket
                        requireActivity().onBackPressed()
                    }
                }.show()
            }
        }
    }

    //先取得裝置連接server成功回傳在去做下一步
    private val bleSettingReceiveCallback = object : BleDataReceiveListener {
        override fun onRadarData(data: ByteArray) {
            super.onRadarData(data)
            Log.i("DeviceScanViewModel","onRadarData=>${data.toHexStringSpace()}")
            val receiveString = String(data)
            Log.i("DeviceScanViewModel","bleSettingReceiveCallback receiveString=>$receiveString")
            main {
                connectProgressDialog.dismiss()
                when (receiveString) {
                    "CONNECT SUCCESS" -> {
                        requireContext().toast(R.string.device_connect_success)
                        goNext()
                    }

                    "WIFI CONNECT FAIL" -> binding.root.snackbar(R.string.device_connect_wifi_fail)

                    "SERVER CONNECT FAIL" -> binding.root.snackbar(R.string.device_connect_fail)

                    "LOAD SERVER FAIL" -> binding.root.snackbar(R.string.device_connect_fail)

                    "OTHER FAIL" ->binding.root.snackbar(R.string.device_failure)
                }

                //回傳TMP表示成功之後顯示成功按鈕按下就退回上一頁
                if (receiveString.contains("TMP")){
                    Log.i(
                        "DeviceScanViewModel",
                        "bleSettingReceiveCallback ontains(\"TMP\") =>$receiveString"
                    )

                    setProgressDialog.dismiss()
                    toastAlertDialog.apply {
                        setMessage(getString(R.string.bind_success))
                        setButton(){
                            activityViewModel.isChange(true)//webSocket
                            requireActivity().onBackPressed()
                        }
                    }.show()
                }

            }

            //1、CONNECT SUCCESS 2、WIFI CONNECT FAIL 3、SERVER CONNECT FAIL 4、LOAD SERVER FAIL 5、OTHER FAIL
            ////成功连接 WiFi 并登陆上后台 //WiFi 连接失败
            ////对服务端连接失败 //登陆后台失败
            ////其他失败情况
        }
    }

    private fun setTemperatureCalibration(){
        main {  setProgressDialog.show() }
        runCatching {
            viewModel.setTemperatureCalibration()
        }.onSuccess {
            waitRadar("setTemperatureCalibration")
        }.onFailure {
            it.printStackTrace()
            main {
                setProgressDialog.dismiss()
                binding.root.snackbar(R.string.bind_failed)
                requireActivity().onBackPressed()
            }
        }
    }


//    fun setTemperatureCalibration(temperature: String) {
//        deviceRepository.setTemperatureCalibration(temperature)
//        isDataSend.postValue(true)
//        runCatching {
//            deviceRepository.setTemperatureCalibration(temperature)
//        }.onSuccess {
//            isDataSend.postValue(false)
//            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_success))
//            getDeviceInfo(appContext.value!!)
//        }.onFailure {
//            isDataSend.postValue(false)
//            throwMessage.value = ToastAlertEntity(message = appContext.value!!.getString(R.string.calibration_failed))
//        }
//    }

//    private fun setAppendDistance(){
//        runCatching {
//            viewModel.setAppendDistance()
//        }.onSuccess {
//            main {
////                viewModel.bleDisconnect()
//                connectProgressDialog.dismiss()
//                toastAlertDialog.apply {
//                    setMessage(getString(R.string.bind_success))
//                    setButton(){
//                        activityViewModel.isChange(true)//webSocket
//                        requireActivity().onBackPressed()
//                    }
//                }.show()
////                binding.root.snackbar(R.string.bind_success)
////
//            }
//        }.onFailure {
//            it.printStackTrace()
//            main {
//                connectProgressDialog.dismiss()
//                binding.root.snackbar(R.string.bind_failed)
//                requireActivity().onBackPressed()
//            }
//        }
//    }



}