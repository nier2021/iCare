package com.docter.icare.ui.main.bedside

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.docter.icare.R
import com.docter.icare.databinding.FragmentBedsideMonitorBinding
import com.docter.icare.ui.base.BaseFragment
import com.docter.icare.ui.main.MainViewModel
import com.docter.icare.ui.main.ToolbarClickListener
import com.docter.icare.utils.Coroutines.main
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
        }


        //webSocket
        with(activityViewModel){
            //後端傳送4狀態正躺 側躺 坐在床邊 離床
            //狀態顏色&&圖片 在床&&側躺＆＆ 藍色 圖相同 值：狀態顯示文字不同 ; 離床&&無人：顏色＝>灰色 圖不同 無值--(直接顯示不管是否有收到呼吸等值);
            getSocketData.observe(viewLifecycleOwner) {
//                Log.i("BedsideMonitorFragment","activityViewModel getSocketData=>$it")
                main { viewModel.entity.isShowRefresh.value = false }
                if (it != null) {
                    if (it.exception == null && it.error == null && it.bioRadar != null && it.bioRadar.bed_state != "斷線") {//未來看小張那是否有指斷線判斷(目前有)
                        viewModel.socketData(requireContext(),it)
                    } else {
                        with(binding.root){
                            try {
                                when{
                                    it.exception != null -> snackbar(it.exception.message!!)//
                                    it.error != null -> snackbar(it.error)
                                    else -> {
                                        snackbar(requireContext().getString(R.string.error_text))
                                    }
                                }
                            }catch (e:Exception){
                                snackbar(requireContext().getString(R.string.error_text))
                            }

                            Log.i("BedsideMonitorFragment","connectionWebSocket SocketUpdate exception ex=>${it.exception?.message}")
                            viewModel.socketError(requireContext())
                            restConnect()
                        }
                    }
                }
            }

            isDeviceChange.observe(viewLifecycleOwner){
                if (it) activityViewModel.getDeviceAccountId().let { id->
                    if (id != -1) {
                        viewModel.bindDeviceShow(requireContext())
                    }else{
                        viewModel.closeSocket(requireContext())
                    }
                }
            }

        }

        setHasOptionsMenu(true)//設置右上角(Fragment)
        activityViewModel.toolbarClickListener = toolbarClickListener

        return binding.root
    }

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

    private fun onClose(){
        Log.i("BedsideMonitorFragment","onClose")
        lifecycleScope.launch {
            runCatching {
                activityViewModel.closeSocket()
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
        main { viewModel.entity.isShowRefresh.value = true }
        if (!restConnectWebServicesDialog.isShowing()) restConnectWebServicesDialog.apply {
            setPositiveButton(R.string.yes_text){ _->

                main{
                    getAccountDeviceInfo()
                }
//                connectProgressDialog.show()
//                activityViewModel.getDeviceAccountId().let {
//                    if (it != -1) {
//                        connectProgressDialog.dismiss()
//                        activityViewModel.restConnect(it)
//                    }else{
//                        connectProgressDialog.dismiss()
//                        onClose()
//                    }
//                }
            }
        }.show()
    }

    private val toolbarClickListener = object : ToolbarClickListener {
        override fun onExceptionClick() {
            super.onExceptionClick()
            Log.i("BedsideMonitorFragment","onExceptionClick go to Exception")
        }
    }

    private fun getAccountDeviceInfo(){
        main {  connectProgressDialog.show() }
        lifecycleScope.launch {
            runCatching {
                activityViewModel.getAccountDeviceInfo()
            }.onSuccess {
                Log.i("BedsideMonitorFragment","getAccountDeviceInfo onSuccess getDeviceList=>$it")
                //is device bind?
                if (it.isNotEmpty()) getDeviceAccountId() else {
                    main { connectProgressDialog.dismiss()  }
                    onClose()
                }
            }.onFailure {
                Log.i("BedsideMonitorFragment","getAccountDeviceInfo onFailure e=>${it.message}")
                main {  connectProgressDialog.dismiss() }
                it.printStackTrace()
                binding.root.snackbar(it)
            }
        }
    }

    private fun getDeviceAccountId(){
        activityViewModel.getDeviceAccountId().let {
            if (it != -1) {
//                onClose()
//                Thread.sleep(3000)
                main { connectProgressDialog.dismiss()  }
                activityViewModel.restConnect(it)
            }else{
                main { connectProgressDialog.dismiss()  }
                onClose()
            }
        }
    }



}