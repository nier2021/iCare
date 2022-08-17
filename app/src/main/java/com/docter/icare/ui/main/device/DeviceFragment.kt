package com.docter.icare.ui.main.device

import android.app.Activity
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
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
import com.docter.icare.databinding.FragmentDeviceBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.ui.main.MainViewModel
import com.docter.icare.ui.main.ToolbarClickListener
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.snackbar
import com.docter.icare.utils.toast
import com.docter.icare.view.dialog.ChooseBedTypeDialog
import com.docter.icare.view.dialog.CustomAlertDialog
import com.docter.icare.view.dialog.CustomProgressDialog
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

    private val wifiBindDeviceDialog: CustomAlertDialog by lazy {
        CustomAlertDialog(requireActivity())
            .setTitle(R.string.wifi_setting)
            .setMessage(R.string.check_wifi_set)
            .setPositiveButton(R.string.yes_text)
            .setNegativeButton(R.string.no_text)
    }

    private val connectProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity(), R.string.connecting) }

    private val chooseBedTypeDialog: ChooseBedTypeDialog by lazy { ChooseBedTypeDialog(requireContext()) }

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
                    }
                }
//                else entity.deviceType = "Radar"
            }
        }

        with(viewModel){
            appContext.value = requireContext()
            getDeviceInfo(requireContext())
            getAccountInfo()
            with(entity){
//                Log.i("DeviceFragment","viewModel btnWifiClose off")
                wifiAccount.observe(viewLifecycleOwner){ getWifiAccount->
//                    Log.i("DeviceFragment","viewModel getWifiAccount=>$getWifiAccount")
                    wifiPassword.observe(viewLifecycleOwner){ getWifiPassword->
//                        Log.i("DeviceFragment","viewModel getWifiPassword=>$getWifiPassword")
                        if (entity.deviceName.isNotEmpty() && getWifiAccount.isNotEmpty() && getWifiPassword.isNotEmpty()){
//                            Log.i("DeviceFragment","viewModel btnWifiClose open")
                            viewModel.entity.isWifiBind.postValue(true)
                        }else viewModel.entity.isWifiBind.postValue(false)
                    }
                }
            }

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
                        setPositiveButton(R.string.yes_text){ v->
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


            binding.btnWifi -> {
                    if (viewModel.isConnect()) {
                        wifiSetData()
                }else {
                    context?.toast(getString(R.string.error_connect_device_state))
                    with(viewModel){
                        runBleConnect()
                        isBleConnectSuccess.observe(viewLifecycleOwner){
                            if (it && viewModel.isConnect())  wifiSetData()
                        }
                    }
                }
            }
        }
    }

    private fun wifiSetData(){
        if (!wifiBindDeviceDialog.isShowing()) wifiBindDeviceDialog.apply {
            setTitle(R.string.wifi_setting)
            setMessage(getString(R.string.check_wifi_set, if (viewModel.entity.deviceType =="Radar") getString(R.string.bioradar) else getString(R.string.air_box)))
            setPositiveButton(R.string.yes_text){ _->
                if (viewModel.entity.wifiAccount.value?.isEmpty() == true || viewModel.entity.wifiPassword.value?.isEmpty() == true)
                    requireContext().toast(getString(R.string.check_wifi_info)) else
                    checkInput()
//                                    viewModel.dataSend(0)
//                                viewModel.wifiDataSend()
            }
        }.show()
    }

    private fun checkInput(){
        runCatching {
            viewModel.checkInput()
        }.onSuccess {
            viewModel.wifiSetData()
        }.onFailure {
            it.printStackTrace()
            binding.root.snackbar(it)
        }
    }

    private fun doBedType(){
        if (!chooseBedTypeDialog.isShowing()) chooseBedTypeDialog.apply {
            saveBedType(viewModel.bedType.value!!)
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
                requireContext().toast(requireContext().getString(R.string.unbind_success))
                viewModel.getDeviceInfo(requireContext())
                viewModel.bleDisconnect()
                with(binding){
                    layoutBtnDeviceBind.title.text = getString(R.string.device_setting)
                    layoutBtnDeviceBind.icon.setImageResource(R.drawable.icon_device_bind)
                    tvDeviceName.text = ""
                    viewModel.entity.isWifiBind.postValue(false)
                    layoutBtnBedType.root.background =ContextCompat.getDrawable(requireContext(),R.drawable.background_button_grey)
                    layoutBtnBedType.root.isClickable = false
                }
            }.onFailure {
                main{ connectProgressDialog.dismiss() }
                Log.i("DeviceFragment","解綁失敗")
                requireContext().toast(requireContext().getString(R.string.unbind_failed))
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
                    bedType.value = chooseBedType
                    bedTypeChangeFlag.value = changeData
                }
//                Log.i("DeviceFragment","選取床型=>${viewModel.bedType.value}")
            }
        }
    }

    private fun showThrowMessage(
        message: String
    ) {
        if (message.isBlank()) return

        binding.root.snackbar(message)

        viewModel.throwMessage.value = ""
    }

}