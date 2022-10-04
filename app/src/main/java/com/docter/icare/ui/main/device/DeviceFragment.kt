package com.docter.icare.ui.main.device

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.net.*
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.PatternMatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.docter.icare.R
import com.docter.icare.data.bleUtil.bleInterface.BleConnectListener
import com.docter.icare.data.entities.device.ToastAlertEntity
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

    private val chooseBedTypeDialog: ChooseBedTypeDialog by lazy { ChooseBedTypeDialog(requireContext()) }

    private val wifiSetDialog: WifiSetDialog by lazy { WifiSetDialog(requireContext()) }

    private val toastAlertDialog: ToastAlertDialog by lazy { ToastAlertDialog(requireContext()) }

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
            getDeviceInfo(requireContext())

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

            bedTypeChangeFlag.observe(viewLifecycleOwner){
//               if (it) viewModel.dataSend(1)
                if (it) viewModel.setAppendDistance(requireContext())

//                   Log.i("DeviceFragment","發送藍芽修改床型=>${bedType.value}")
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

//        override fun onBackClick(activity: Activity) = when {
//            chooseAddDialog.isShowing() -> chooseAddDialog.dismiss()
//            else -> super.onBackClick(activity)
//        }
//
//        override fun onAddClick() = chooseAddDialog.show()
    }

    override fun onClick(view: View) {
        super.onClick(view)
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


            binding.layoutBtnBedType.root ->
                if (viewModel.isConnect()) {
                    doBedType()
                }else {
                    context?.toast(getString(R.string.error_connect_device_state))
                    viewModel.runBleConnect()
                    with(viewModel){
                        runBleConnect()
                        isBleConnectSuccess.observe(viewLifecycleOwner){
                            if (it && viewModel.isConnect())  doBedType()
                        }
                    }
                }


            binding.layoutBtnWifiBind.root -> {
                main{
                    if (!wifiSetDialog.isShowing()) wifiSetDialog.apply {
                        setClickListener(wifiSetDialogClickListener)
                    }.show()
                }

            }
        }
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


    private fun doBedType(){
        if (!chooseBedTypeDialog.isShowing()) chooseBedTypeDialog.apply {
            saveBedType(viewModel.setBedType.value!!)
            setClickListener(chooseBedTypeDialogClickListener)
        }.show()
    }

    private fun unBindDevice(){
        lifecycleScope.launch {
            main{ connectProgressDialog.show() }
            runCatching {
                viewModel.unBindDevice(requireContext())
            }.onSuccess {
                main{ connectProgressDialog.dismiss() }
                Log.i("DeviceFragment","解綁成功")
//                requireContext().toast(requireContext().getString(R.string.unbind_success))
                binding.root.snackbar(getString(R.string.unbind_success))
                viewModel.getDeviceInfo(requireContext())
                viewModel.bleDisconnect()
                with(binding){
                    layoutBtnDeviceBind.title.text = getString(R.string.device_setting)
                    layoutBtnDeviceBind.icon.setImageResource(R.drawable.icon_device_bind)
                    tvDeviceName.text = ""
                    layoutDeviceName.visibility = View.GONE
//                    viewModel.entity.isWifiBind.postValue(false)
                    layoutBtnBedType.root.background = ContextCompat.getDrawable(requireContext(),R.drawable.background_button_grey)
                    layoutBtnBedType.root.isClickable = false
                }
            }.onFailure {
                main{ connectProgressDialog.dismiss() }
                Log.i("DeviceFragment","解綁失敗")
//                requireContext().toast(requireContext().getString(R.string.unbind_failed))
                it.printStackTrace()
                binding.root.snackbar(it)
            }
        }
    }


    private val chooseBedTypeDialogClickListener = object : ChooseBedTypeDialog.OnClickListener{
        override fun onConfirmClick(changeData: Boolean, chooseBedType: Int) {
//            Log.i("DeviceFragment","changeData=>$changeData 取得床型=>$chooseBedType")
            with(viewModel){
                if (changeData) {
                    setBedType.value = chooseBedType
                    bedTypeChangeFlag.value = changeData
                }
//                Log.i("DeviceFragment","選取床型=>${viewModel.bedType.value}")
            }
        }
    }

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

//    private fun showThrowMessage(
//        message: String
//    ) {
//        if (message.isBlank()) return
//
//        binding.root.snackbar(message)
//
//        viewModel.throwMessage.value = ""
//    }


    private fun connectToWifi(ssid: String, password:String){
        main {
            WfiCheckUtils(context = requireContext(),wfiCheckUtilsCallBack = wfiCheckUtilsCallBack).connectToWifi(ssid = ssid, password = password)
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

//    //wifi
//    var connectivityManager: ConnectivityManager ?= null
//
//    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
//        override fun onAvailable(network: Network) {
//            super.onAvailable(network)
//            Log.i("DeviceFragment","networkCallback onAvailable network=>$network")
//            connectivityManager?.bindProcessToNetwork(network)
//
//        }
//
//        override fun onLost(network: Network) {
//            super.onLost(network)
//            Log.i("DeviceFragment","networkCallback onLost network=>$network")
//            unregisterNetWork()
//        }
//
//        override fun onLosing(network: Network, maxMsToLive: Int) {
//            super.onLosing(network, maxMsToLive)
//            Log.i("DeviceFragment","networkCallback onLosing network=>$network \n maxMsToLive=>$maxMsToLive")
//        }
//
//        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
//            super.onLinkPropertiesChanged(network, linkProperties)
//            Log.i("DeviceFragment","networkCallback onLinkPropertiesChanged network=>$network \n linkProperties=>$linkProperties")
//            main {
//                unregisterNetWork()
//            }
//        }
//
//        override fun onCapabilitiesChanged(
//            network: Network,
//            networkCapabilities: NetworkCapabilities
//        ) {
//            super.onCapabilitiesChanged(network, networkCapabilities)
//            Log.i("DeviceFragment","networkCallback onCapabilitiesChanged network=>$network \n networkCapabilities=>$networkCapabilities")
//        }
//
//        override fun onUnavailable() {
//            super.onUnavailable()
//            Log.i("DeviceFragment","networkCallback onUnavailable")
//            //失敗
//            binding.root.snackbar(getString(R.string.error_wifi_acc_pass_incorrect))
//        }
//
//        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
//            super.onBlockedStatusChanged(network, blocked)
//            Log.i("DeviceFragment","networkCallback onBlockedStatusChanged network=>$network \n blocked=>$blocked")
//        }
//
//    }
//
//    private fun connectToWifi(ssid: String, password:String) {
//
//        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
//            .setSsid(ssid)
////            .setSsidPattern(PatternMatcher(ssid,PatternMatcher.PATTERN_LITERAL))
//            .setWpa2Passphrase(password)
//            .build()
//
//        val networkRequest = NetworkRequest.Builder()
//            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
////            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
////            .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
////            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            .setNetworkSpecifier(wifiNetworkSpecifier)
//            .build()
//
//        connectivityManager =
//            context?.applicationContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        connectivityManager?.registerNetworkCallback(networkRequest,networkCallback )
//        connectivityManager?.requestNetwork(networkRequest, networkCallback)
//
//    }
//
//
//
//    fun unregisterNetWork(){
//        Log.e("DeviceFragment","unregisterNetWork")
//        connectivityManager?.bindProcessToNetwork(null)
//        connectivityManager?.unregisterNetworkCallback(networkCallback)
//
//    }


}