package com.docter.icare.ui.main.device

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.docter.icare.R
import com.docter.icare.data.entities.device.ToastAlertEntity
import com.docter.icare.data.network.api.apiErrorShow
import com.docter.icare.databinding.FragmentDeviceBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.ui.main.MainViewModel
import com.docter.icare.ui.main.ToolbarClickListener
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.WfiCheckUtils
import com.docter.icare.utils.snackbar
import com.docter.icare.utils.toast
import com.docter.icare.view.dialog.*
import kotlinx.coroutines.launch
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance


class DeviceFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var binding: FragmentDeviceBinding

    private val factory: DeviceViewModelFactory by instance()
    private val viewModel: DeviceViewModel by lazy { ViewModelProvider(this, factory)[DeviceViewModel::class.java] }

    private val activityViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }

    private val confirmUnBindDeviceDialog: CustomAlertDialog by lazy {
        CustomAlertDialog(requireActivity())
            .setTitle(R.string.unbind_device)
            .setMessage(R.string.confirm_unbind_device_message)
            .setPositiveButton(R.string.yes_text)
            .setNegativeButton(R.string.no_text)
    }

    private val connectProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity(), R.string.connecting) }

//    private val chooseBedTypeDialog: ChooseBedTypeDialog by lazy { ChooseBedTypeDialog(requireContext()) }

    private val wifiSetDialog: WifiSetDialog by lazy { WifiSetDialog(requireContext()) }

    private val temperatureSetDialog: TemperatureSetDialog by lazy { TemperatureSetDialog(requireContext()) }

    private val toastAlertDialog: ToastAlertDialog by lazy { ToastAlertDialog(requireContext()) }

    private val setProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog( requireActivity(), R.string.set_up) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            click = this@DeviceFragment
            entity = viewModel.entity

            arguments?.get("deviceType").let {
                Log.i("DeviceFragment","deviceType =>$it")
                if (it != null){
                    it as String
                    with(viewModel){
                        entity.deviceType = it
                        entity.type.value = setType(it)
                    }
                }
            }
        }

        with(viewModel){
            appContext.value = requireContext()
            getDeviceInfo()

            isDataSend.observe(viewLifecycleOwner){
                if (it){
                    main {
                        connectProgressDialog.show()
                    }
                }else{
                    main {
                        connectProgressDialog.dismiss()
                    }
                }
            }
            throwMessage.observe(viewLifecycleOwner) { showThrowMessage(it) }

//            bedTypeChangeFlag.observe(viewLifecycleOwner){
//                if (it) viewModel.setAppendDistance(requireContext())

//                   Log.i("DeviceFragment","發送藍芽修改床型=>${bedType.value}")
//            }

            isSetProgress.observe(viewLifecycleOwner){
                if (it) setProgressDialog.show() else setProgressDialog.dismiss()
            }

            isLogout.observe(viewLifecycleOwner){
                if (it) logout()
            }
        }


        viewModel.setSettingReceiveCallback()

        activityViewModel.toolbarClickListener = toolbarClickListener

        return binding.root
    }

    private val toolbarClickListener = object : ToolbarClickListener {
        override fun onBackClick(activity: Activity) {

            super.onBackClick(activity)
        }

    }

    override fun onClick(view: View) {
        super.onClick(view)
//        if (activityViewModel.isBluetoothEnabled() == true){
        if (activityViewModel.isBluetoothEnabled(context = requireContext().applicationContext)){
            when(view){
                binding.layoutBtnDeviceBind.root ->{
                    if (viewModel.entity.deviceName.isNotEmpty()){
                        if (!confirmUnBindDeviceDialog.isShowing()) confirmUnBindDeviceDialog.apply {
                            setMessage(getString(R.string.confirm_unbind_device_message,viewModel.entity.deviceName))
                            setPositiveButton(R.string.yes_text){ _->
                                unBindDevice()
                            }
                        }.show()
                    }else{
                        findNavController().navigate(R.id.deviceScanFragment, bundleOf("deviceType" to viewModel.entity.deviceType))
                    }
                }

                binding.layoutBtnWifiBind.root -> {
                    main{
                        if (!wifiSetDialog.isShowing()) wifiSetDialog.apply {
                            setClickListener(wifiSetDialogClickListener)
                        }.show()
                    }

                }

//                binding.layoutBtnBedType.root ->
//                    if (viewModel.isConnect()) {
//                        doBedType()
//                    }else {
//                        context?.toast(getString(R.string.error_connect_device_state))
//                        viewModel.runBleConnect()
//                        with(viewModel){
//                            runBleConnect()
//                            isBleConnectSuccess.observe(viewLifecycleOwner){
//                                if (it && viewModel.isConnect())  doBedType()
//                            }
//                        }
//                    }

                binding.layoutBtnTemperatureCalibration.root -> {
                    if (viewModel.isConnect()) {
                        doTemperature()
                    }else {
                        context?.toast(getString(R.string.error_connect_device_state))
                        viewModel.runBleConnect()
                        with(viewModel){
                            runBleConnect()
                            isBleConnectSuccess.observe(viewLifecycleOwner){
                                if (it && viewModel.isConnect())  doTemperature()
                            }
                        }
                    }
                }

            }
        } else binding.root.snackbar(R.string.no_open_bluetooth)
    }

    private val wifiSetDialogClickListener = object : WifiSetDialog.OnClickListener{
        override fun onConfirmClick(wifiAccount: String, wifiPassword: String) {
            Log.i("DeviceFragment","wifiSetDialogClickListener onConfirmClick wifiAccount=>$wifiAccount wifiPassword=>$wifiPassword")
            if (wifiAccount.isNotBlank() && wifiPassword.isNotBlank()){
//                unregisterNetWork()
                connectToWifi(wifiAccount, wifiPassword)
            }

        }

        override fun onCancelClick() {
            Log.i("DeviceFragment","wifiSetDialogClickListener onCancelClick")
        }

    }

    private val temperatureSetDialogClickListener = object : TemperatureSetDialog.OnClickListener{
        override fun onConfirmClick(temperature: String) {
            Log.i("DeviceFragment","temperatureSetDialogClickListener onConfirmClick temperature=>$temperature")
            if (temperature.isNotBlank() && temperature.toFloat() in 35f..42f){
                main {Double
                    viewModel.setTemperatureCalibration(temperature)
                }
            }
        }

        override fun onCancelClick() {
            Log.i("DeviceFragment","temperatureSetDialogClickListener onCancelClick")
        }

    }


//    private fun doBedType(){
//        if (!chooseBedTypeDialog.isShowing()) chooseBedTypeDialog.apply {
////            saveBedType(viewModel.setBedType.value!!)
//            setClickListener(chooseBedTypeDialogClickListener)
//        }.show()
//    }


    private fun doTemperature(){
        main {
            if (!temperatureSetDialog.isShowing()) temperatureSetDialog.apply {
                setClickListener(temperatureSetDialogClickListener)
            }.show()
        }
    }

    private fun unBindDevice(){
        lifecycleScope.launch {
            main{ connectProgressDialog.show() }
            runCatching {
                viewModel.unBindDevice(requireContext())
            }.onSuccess {
                main{ connectProgressDialog.dismiss() }
                if (it.success == 1){
                    viewModel.saveDeviceInfo()
                    Log.i("DeviceFragment","解綁成功")
//                requireContext().toast(requireContext().getString(R.string.unbind_success))
                    binding.root.snackbar(getString(R.string.unbind_success))
                    viewModel.getDeviceInfo()
                    viewModel.bleDisconnect()
                    with(binding){
                        layoutBtnDeviceBind.title.text = getString(R.string.device_setting)
                        layoutBtnDeviceBind.icon.setImageResource(R.drawable.icon_device_bind)
                        tvDeviceName.text = ""
                        layoutDeviceName.visibility = View.GONE
//                    layoutBtnBedType.root.background = ContextCompat.getDrawable(requireContext(),R.drawable.background_button_grey)
//                    layoutBtnBedType.root.isClickable = false
                        layoutBtnTemperatureCalibration.root.background = ContextCompat.getDrawable(requireContext(),R.drawable.background_button_grey)
                        layoutBtnTemperatureCalibration.root.isClickable = false
//                        activityViewModel.isChange(true)//webSocket
                        activityViewModel.isDeviceChange.postValue(true)//webSocket
                    }
                }
            }.onFailure {
                main{ connectProgressDialog.dismiss() }
                viewModel.bleDisconnect()
                Log.i("DeviceFragment","解綁失敗")
//                requireContext().toast(requireContext().getString(R.string.unbind_failed))
                it.printStackTrace()
                if (it.message.isNullOrBlank()) binding.root.snackbar(requireContext().getString(R.string.unknown_error_occurred))
                else {
                    val getMessage: Pair<Boolean, String> = it.message!!.apiErrorShow(requireContext())
                    Log.i("DeviceFragment", "deviceBindingRequest onFailure getMessage first=>${getMessage.first}")
                    binding.root.snackbar(getMessage.second)
                    if (getMessage.first){
                        logout()
                        //logout
//                        activityViewModel.tokenFailLogout.value = true
//                        requireContext().toast(R.string.logout)
                    }

                }
            }
        }
    }


//    private val chooseBedTypeDialogClickListener = object : ChooseBedTypeDialog.OnClickListener{
//        override fun onConfirmClick(changeData: Boolean, chooseBedType: Int) {
////            Log.i("DeviceFragment","changeData=>$changeData 取得床型=>$chooseBedType")
//            with(viewModel){
//                if (changeData) {
////                    setBedType.value = chooseBedType
//                    bedTypeChangeFlag.value = changeData
//                }
////                Log.i("DeviceFragment","選取床型=>${viewModel.bedType.value}")
//            }
//        }
//    }

    private fun showThrowMessage(
        throwMessageData: ToastAlertEntity
    ) {
        if (throwMessageData.message.isBlank()) return
        when(throwMessageData.isToastAlert){
            true ->
                toastAlertDialog.apply {
                    setMessage(throwMessageData.message)
                    setButton(){}
                }.show()
            else ->  binding.root.snackbar(throwMessageData.message)
        }
        viewModel.throwMessage.value = ToastAlertEntity()
    }


    private fun connectToWifi(ssid: String, password:String){
        main {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Log.i("DeviceFragment","connectToWifi < Build.VERSION_CODES.Q")
                binding.root.snackbar(R.string.no_supports_check_wifi)
            } else {
                val wfiCheckUtils = WfiCheckUtils(context = requireContext(),wfiCheckUtilsCallBack = wfiCheckUtilsCallBack)
                if (wfiCheckUtils.isWifiEnabled()){
                    wfiCheckUtils.connectToWifi(ssid = ssid, password = password)
                }else{
                    binding.root.snackbar(R.string.not_open_wifi)
                }
            }

        }
    }

    private val wfiCheckUtilsCallBack= object :WfiCheckUtils.WfiCheckUtilsCallBack{
        override fun onFail() {
            binding.root.snackbar(getString(R.string.error_wifi_acc_pass_incorrect))
        }

        override fun onSuccess(ssid: String, password:String ) {
            main {
                Log.i("DeviceFragment","進行wifi藍芽設定 ssid=>$ssid \n password=>$password")
                if (ssid.isNotBlank() && password.isNotBlank()){
                    with(viewModel){
                        saveWifiData(wifiAccount = ssid, wifiPassword = password)
                        entity.wifiAccount.value = ssid
                        entity.wifiPassword = password
                    }
                }
            }
        }

        override fun onShowProgress(isShow: Boolean) {
            main {
                if (isShow) connectProgressDialog.show() else connectProgressDialog.dismiss()
            }
        }

    }

    private fun logout(){
        main {
            activityViewModel.tokenFailLogout.value = true
            requireContext().toast(R.string.logout)
        }
    }


}