package com.docter.icare.ui.main.bedside

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.docter.icare.R
import com.docter.icare.databinding.FragmentBedsideMonitorBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.ui.main.MainViewModel
import com.docter.icare.utils.snackbar
import com.docter.icare.utils.toast
import com.docter.icare.view.dialog.CustomAlertDialog
import com.docter.icare.view.dialog.CustomProgressDialog
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class BedsideMonitorFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var binding: FragmentBedsideMonitorBinding

    private val factory: BedsideMonitorViewModelFactory by instance()
    private val viewModel: BedsideMonitorViewModel by lazy { ViewModelProvider(this, factory)[BedsideMonitorViewModel::class.java] }

    private val activityViewModel: MainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }

    private val restConnectWebServicesDialog: CustomAlertDialog by lazy {
        CustomAlertDialog(requireActivity())
            .setTitle(R.string.rest_connect_title)
            .setMessage(R.string.rest_connect_message)
            .setPositiveButton(R.string.yes_text)
            .setNegativeButton(R.string.no_text)
    }

    private val connectProgressDialog: CustomProgressDialog by lazy { CustomProgressDialog(requireActivity(), R.string.connecting) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBedsideMonitorBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            click = this@BedsideMonitorFragment
            entity = viewModel.entity
            viewModel.getDeviceAccountId().let {
                Log.i("BedsideMonitorFragment","getDeviceAccountId=>$it")
                if (it.isNotEmpty()){
                    viewModel.bindDeviceShow(requireContext())
                    connectionWebSocket(it)
                }else{
                    onClose()
                }
            }
        }


        return binding.root
    }

//    private fun connectionWebSocket(accountId: String){
////        //後端傳送4狀態正躺 側躺 坐在床邊 離床
////        //狀態顏色&&圖片 在床&&側躺＆＆ 藍色 圖相同 值：狀態顯示文字不同 ; 離床&&無人：顏色＝>灰色 圖不同 無值--(直接顯示不管是否有收到呼吸等值);
//        lifecycleScope.launch {
//            try {
//                viewModel.startSocket(accountId).consumeEach {
//                    if (it.exception == null && it.error == null && it.bioRadar != null) {
//                        binding.contentTv.text = "會員id:${it.bioRadar!!.accountId}\n 數據時間:${it.bioRadar!!.time}\n 雷達狀態：${it.bioRadar!!.radar_state}\n" +
//                                "距離:${it.bioRadar!!.distance}\n 心率:${it.bioRadar!!.heart_rate}\n 在床情況：${it.bioRadar!!.bed_state}\n 呼吸頻率：${it.bioRadar!!.breath_state}\n"
//                    } else {
//                        with(binding.root){
//                            when{
//                                it.exception != null -> snackbar(it.exception.message!!)
//                                it.error != null -> snackbar(it.error)
//                                else -> snackbar("資料存取錯誤...")
//                            }
//                        }
//                    }
//                }
//
//            } catch (ex: java.lang.Exception) {
////                    onSocketError(ex)
//                binding.root.snackbar(ex.message!!)
//            }
//        }
//
//    }


    override fun onClick(view: View) {
        super.onClick(view)
        Log.i("BedsideMonitorFragment","v->$view")
        when(view){
            binding.ivRefresh -> {
                Log.i("BedsideMonitorFragment"," binding.ivRefresh")
                restConnect()
            }
        }
    }

    private fun connectionWebSocket(accountId: String){
        //後端傳送4狀態正躺 側躺 坐在床邊 離床
        //狀態顏色&&圖片 在床&&側躺＆＆ 藍色 圖相同 值：狀態顯示文字不同 ; 離床&&無人：顏色＝>灰色 圖不同 無值--(直接顯示不管是否有收到呼吸等值);
        Log.i("BedsideMonitorFragment","connectionWebSocket")
        lifecycleScope.launch {
            try {
                Log.i("BedsideMonitorFragment","connectionWebSocket try")
                viewModel.startSocket(requireContext(),accountId).consumeEach {
                    if (it.exception == null && it.error == null && it.bioRadar != null && it.bioRadar.bed_state != "斷線") {//未來看小張那是否有指斷線判斷(目前有)
                        viewModel.socketData(requireContext(),it)
                    } else {
                        with(binding.root){
                            when{
                                it.exception != null -> snackbar(it.exception.message!!)
                                it.error != null -> snackbar(it.error)
                                else -> {
                                    snackbar(requireContext().getString(R.string.error_text))
                                }
                            }
                            Log.i("BedsideMonitorFragment","connectionWebSocket SocketUpdate exception ex=>${it.exception?.message}")
                            viewModel.socketError(requireContext())
                            restConnect()
                        }
                    }
                }

            } catch (ex: java.lang.Exception) {
//                    onSocketError(ex)
                viewModel.socketError(requireContext())
                binding.root.snackbar(ex)
                restConnect()
                Log.i("BedsideMonitorFragment","connectionWebSocket catch ex=>${ex.message}")
            }
        }

    }

    private fun onClose(){
        Log.i("BedsideMonitorFragment","onClose")
        lifecycleScope.launch {
            runCatching {
                viewModel.stopSocket()
            }.onSuccess {
//                binding.contentTv.text ="已關閉"
                Log.i("BedsideMonitorFragment","onClose  onSuccess")
                viewModel.closeSocket(requireContext())
            }.onFailure {
                Log.i("BedsideMonitorFragment","onClose onFailure e=>${it.message}")
                it.printStackTrace()
                binding.root.snackbar(it)
            }
        }
    }


    private fun restConnect(){
        Log.i("BedsideMonitorFragment","restConnect")
        if (!restConnectWebServicesDialog.isShowing()) restConnectWebServicesDialog.apply {
            setPositiveButton(R.string.yes_text){ v->
                connectProgressDialog.show()
                viewModel.getDeviceAccountId().let {
                    if (it.isNotEmpty()){
                        connectProgressDialog.dismiss()
                        connectionWebSocket(it)
                    }else{
                        connectProgressDialog.dismiss()
                        onClose()
                    }
                }
            }
        }.show()
    }

}