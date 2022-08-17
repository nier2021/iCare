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
import com.docter.icare.databinding.FragmentDeviceScanBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.ui.main.MainActivity
import com.docter.icare.ui.main.MainViewModel
import com.docter.icare.utils.Coroutines
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.utils.clearStack
import com.docter.icare.utils.snackbar
import com.docter.icare.utils.toast
import com.docter.icare.view.RecyclerDivider
import com.docter.icare.view.dialog.CustomAlertDialog
import com.docter.icare.view.dialog.CustomProgressDialog
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val connectProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity(), R.string.connecting) }

    private val confirmBindDeviceDialog: CustomAlertDialog by lazy {
        CustomAlertDialog(requireActivity())
            .setTitle(R.string.device_setting)
            .setMessage(R.string.confirm_bind_device_message)
            .setPositiveButton(R.string.yes_text)
            .setNegativeButton(R.string.no_text)
    }

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
                        setPositiveButton(R.string.yes_text){ v->
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
            binding.layoutBtnDeviceScan.root -> viewModel.startScan()
        }
    }

    private suspend fun deviceBindingRequest(device: BluetoothDevice?){
//        Log.i("DeviceScanViewModel","deviceBindingRequest")
        withContext(Dispatchers.IO) {
            runCatching {
                viewModel.deviceBindingRequest(requireContext(), device, 1)
            }.onSuccess {
//                Log.i("DeviceScanViewModel","deviceBindingRequest bind_success")
                viewModel.isConnect()
                setAppendDistance()
//                main {
//                    viewModel.bleDisconnect()
//                    requireActivity().onBackPressed()
//                }
            }.onFailure {
//                Log.i("DeviceScanViewModel","deviceBindingRequest bind_Failure")
                connectProgressDialog.dismiss()
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
            connectProgressDialog.dismiss()
        }
        with(viewModel){
            stopScan()
//            bleDisconnect()
        }
    }

    private fun setAppendDistance(){
        runCatching {
            viewModel.setAppendDistance()
        }.onSuccess {
            main {
//                viewModel.bleDisconnect()
                connectProgressDialog.dismiss()
                binding.root.snackbar(R.string.bind_success)
                requireActivity().onBackPressed()
            }
        }.onFailure {

//            requireContext().toast("床型設定發生錯誤")
            it.printStackTrace()
            main {
                connectProgressDialog.dismiss()
//                viewModel.bleDisconnect()
                binding.root.snackbar(it)
                requireActivity().onBackPressed()
            }
        }
    }

}