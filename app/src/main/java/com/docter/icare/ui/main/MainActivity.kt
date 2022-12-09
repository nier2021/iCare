package com.docter.icare.ui.main

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import com.docter.icare.R
import com.docter.icare.databinding.ActivityMainBinding
import com.docter.icare.ui.base.BaseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.kodein.di.android.closestDI
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.navigateUp
import com.docter.icare.data.entities.device.DeviceInfoEntity
import com.docter.icare.data.entities.view.ExpandableListEntity
import com.docter.icare.data.entities.view.MainEntity
import com.docter.icare.data.entities.view.MainEntity.Companion.ACTIVITY_MONITORING
import com.docter.icare.data.entities.view.MainEntity.Companion.AIR_QUALITY_INDEX
import com.docter.icare.data.entities.view.MainEntity.Companion.BEDSIDE_MONITOR
import com.docter.icare.data.network.api.apiErrorShow
import com.docter.icare.ui.main.expandableList.ExpandableListAdapter
import com.docter.icare.ui.start.StartActivity
import com.docter.icare.ui.start.login.LoginFragment
import com.docter.icare.utils.*
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.view.dialog.CustomAlertDialog
import com.docter.icare.view.dialog.CustomProgressDialog
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.kodein.di.instance

class MainActivity : BaseActivity() {

    override val di by closestDI()
    private val binding: ActivityMainBinding by lazy { DataBindingUtil.setContentView(this, R.layout.activity_main) }

    private val factory: MainViewModelFactory by instance()
    private val viewModel: MainViewModel by lazy { ViewModelProvider(this, factory)[MainViewModel::class.java] }

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var toolbar: Toolbar

    private val fragment: NavHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment }

    private val progressDialog: CustomProgressDialog by lazy { CustomProgressDialog(this, R.string.logout) }

    private val logoutDialog: CustomAlertDialog by lazy {
        CustomAlertDialog(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.yes_text){
                checkDevice()
            }
            .setNegativeButton(R.string.no_text)
    }

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if (it.resultCode == RESULT_OK) {
            Log.i("MainActivity","藍芽打開 背景回傳需要監聽服務可以寫在這")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.welcome_status_bar)

        with(binding){
            lifecycleOwner = this@MainActivity
            click = this@MainActivity
            entity = viewModel.entity
            viewModel.getExpandableListData(this@MainActivity)
            with(expandableListView){
                setAdapter(viewModel.adapter)
                setOnChildClickListener(expandableListViewOnChildClickListener)
//                setOnGroupExpandListener(expandableListViewOnGroupExpandListener)
//                setOnGroupCollapseListener(expandableListViewOnGroupCollapseListener)
            }


        }

        //取得後台登入帳號裝置狀態
        getAccountDeviceInfo()

        fragment.navController.addOnDestinationChangedListener { _, destination, _ ->

            viewModel.destinationChange(destination)

//            with(binding.toolbar.menu) {
//                getItem(0).isVisible = viewModel.hasException(destination)
//                getItem(1).isVisible = viewModel.hasDone(destination)
////                getItem(2).isVisible = viewModel.hasDone(destination)
////                getItem(3).isVisible = viewModel.hasShare(destination)
//            }
        }


        //底部導航與標題
        bottomNavView = binding.bottomNavigation
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.bedsideMonitorFragment, R.id.airQualityIndexFragment, R.id.activityMonitoringFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
        with(bottomNavView){
            setupWithNavController(navController)
            setOnItemSelectedListener (bottomNavListener)
        }
        //開啟側欄
        with(toolbar) {
            setNavigationOnClickListener {
                Log.i("MainActivity","toolbar setNavigationOnClickListener")
                navigationClick()
            }
            setOnMenuItemClickListener {
                Log.i("MainActivity","toolbar setOnMenuItemClickListener")
                viewModel.menuClick(it)
            }
        }


        //webSocket
        viewModel.appContext.value = this.applicationContext
        //監測是否有改變
        viewModel.isDeviceChange.observe(this){
            Log.i("MainActivity","isDeviceChange observe")
            if (it) getDeviceAccountId()
        }
        //檢查藍芽
        checkBluetooth()
        //重心連接
        viewModel.isRestConnect.observe(this){
            if (it != -1 ) connectionWebSocket(it)
        }

        viewModel.tokenFailLogout.observe(this){
            if (it) checkDevice()
        }
    }

    private var bottomNavListener = NavigationBarView.OnItemSelectedListener {
//    private var bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener {//已被棄用
//        Log.i("MainActivity","itemId=>${it.itemId}")
        when (it.itemId) {
//之後有做功能再開以下(還有bottom_nav_menu)
//            R.id.nav_air_quality_index ->{
//                Log.i("MainActivity","airQualityIndexFragment")
////                navController.navigate(R.id.airQualityIndexFragment)//等做完再開啟
//                this@MainActivity.toast("尚未開放")
////                true//等做完再開啟
//                false
//
//            }
//            R.id.nav_activity_monitoring -> {
////                Log.i("MainActivity","activityMonitoringFragment")
////                navController.navigate(R.id.activityMonitoringFragment)//等做完再開啟
//                this@MainActivity.toast("尚未開放")
////                true//等做完再開啟
//                false
//            }
//以上等之後有做功能再開啟
            else -> {
                //以上等做完再開啟
//                 R.id.nav_bedside_monitor
//                Log.i("MainActivity","bedsideMonitorFragment")
                navController.navigate(R.id.bedsideMonitorFragment)
                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.welcome_status_bar))
                true

            }
        }
//        false
//        true //等做完再開啟成 true
    }

    override fun onClick(view: View) {
        super.onClick(view)
        binding.drawerLayout.close()
//        Log.i("MainActivity","onClick=>$view")
        when(view){
            binding.person.root ->{
//                this@MainActivity.toast("個人資訊")
//                navController.navigate(R.id.personInfoContentFragment,bundleOf("deviceType" to "Radar"))
                this@MainActivity.toast("尚未開放")
            }
            binding.set.root -> this@MainActivity.toast("尚未開放")
//                this@MainActivity.toast("功能設定")
        }
    }

    private fun navigationClick() {

        window.hideSoftKeyboard()
//        Log.i("MainActivity","navigationClick")
//        when (viewModel.entity.nowFragment.value) {
        when (viewModel.entity.isDrawerBottomViewShow.value) {

            true ->{
//                Log.i("MainActivity","navigationClick open")
                binding.drawerLayout.open()
            }

            else -> when {
                viewModel.toolbarClickListener != null -> {
//                    Log.i("MainActivity","navigationClick onBackClick")
                    viewModel.toolbarClickListener?.onBackClick(this)
                }
                else ->{
//                    Log.i("MainActivity","navigationClick onBackPressed")
                    onBackPressed()
                }
            }
        }
    }

    private val expandableListViewOnChildClickListener =
        ExpandableListView.OnChildClickListener { _, _, groupPosition, childPosition, _ ->
            binding.drawerLayout.close()
            Log.i("MainActivity","expandableListViewOnChildClickListener groupPosition=>$groupPosition \nchildPosition=>$childPosition")
            when{
                groupPosition == 0 && childPosition == 0 -> {
                    // this@MainActivity.toast("睡眠感知")
                    navController.navigate(R.id.device_navigation,bundleOf("deviceType" to "Radar"))
                }
//以下等做完再開啟
//                groupPosition == 0 && childPosition == 1 ->  navController.navigate(R.id.device_navigation,bundleOf("deviceType" to "Air"))
//                groupPosition == 0 && childPosition == 2 -> this@MainActivity.toast("姿態感知")//除了姿態感知 睡眠感知與空氣品質裝置都寫在一起
//                groupPosition == 1 && childPosition == 0 -> this@MainActivity.toast("關於")
//                groupPosition == 1 && childPosition == 1 -> this@MainActivity.toast("隱私權政策")
//                groupPosition == 1 && childPosition == 2 -> if (!logoutDialog.isShowing()) logoutDialog.show()
                groupPosition == 1 && childPosition == 0 -> if (!logoutDialog.isShowing()) logoutDialog.show()//之後依開放改回上式
//                    this@MainActivity.toast("登出")
                else -> this@MainActivity.toast("尚未開放")
            }
            false
        }

    //list展開
    private val expandableListViewOnGroupExpandListener = ExpandableListView.OnGroupExpandListener { groupPosition->  if (groupPosition==0){ this@MainActivity.toast("設備管理展開")}else{this@MainActivity.toast("關於展開")} }
    //list關閉
    private val expandableListViewOnGroupCollapseListener = ExpandableListView.OnGroupCollapseListener { groupPosition -> if (groupPosition==0){ this@MainActivity.toast("設備管理闔上")}else{this@MainActivity.toast("關於展闔上")}}


    private fun checkDevice(){
        runCatching {
            viewModel.checkDevice()
        }.onSuccess {
            if (it != null){
                bleDisconnect(it)
            }else logoutServer()
        }.onFailure {
            it.printStackTrace()
            this@MainActivity.toast("檢索裝置失敗,登出失敗")
        }
    }

    private fun bleDisconnect(deviceInfoEntity: DeviceInfoEntity){
        runCatching {
            viewModel.bleDisconnect(deviceInfoEntity)
        }.onSuccess {
            onClose(deviceInfoEntity)
        }.onFailure {
            it.printStackTrace()
            this@MainActivity.toast("裝置連線關閉失敗,登出失敗")
        }
    }

    private fun onClose(deviceInfoEntity: DeviceInfoEntity){
        Log.i("MainActivity","onClose")
        lifecycleScope.launch {
            runCatching {
                viewModel.closeSocket()
            }.onSuccess {
//                unBindDevice(deviceInfoEntity)
                Log.i("MainActivity","onClose  onSuccess")
                logoutServer()
            }.onFailure {
                Log.i("MainActivity","onClose onFailure e=>${it.message}")
                it.printStackTrace()
                binding.root.snackbar(it)
            }
        }
    }

//    private fun unBindDevice(deviceInfoEntity: DeviceInfoEntity){
//        main{progressDialog.show()}
//        lifecycleScope.launch {
//            runCatching {
//                viewModel.unBindDevice(deviceInfoEntity)
//            }.onSuccess {
//                logoutServer()
//            }.onFailure {
//                main{progressDialog.dismiss()}
//                it.printStackTrace()
//                binding.root.snackbar(it)
//            }
//
//        }
//    }


    private fun logoutServer(){
        lifecycleScope.launch {
            runCatching {
                viewModel.logoutServer()
            }.onSuccess {
                logout()
            }.onFailure {
                it.printStackTrace()
                logout()
                if (it.message.isNullOrBlank()) Log.i("MainActivity", "logoutServer onFailure it.message.isNullOrBlank()")
                else {
                    val getMessage: Pair<Boolean, String> = it.message!!.apiErrorShow(this@MainActivity)
                    Log.i("MainActivity", "logout onFailure getMessage first=>${getMessage.first}")
                }
            }
        }
    }


    private fun logout(){
        this@MainActivity.toast("登出")
        runCatching {
            viewModel.logout()
        }.onSuccess {
            main {
                progressDialog.dismiss()
                val intent = Intent()
                intent.setClass(this, StartActivity::class.java).clearStack()
                startActivity(
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
                )
            }

        }.onFailure {
            main{progressDialog.dismiss()}
            it.printStackTrace()
            binding.root.snackbar(it)
        }

    }


    //取得後台登入帳號裝置狀態
    private fun getAccountDeviceInfo(){
//        Log.i("MainActivity","getAccountDeviceInfo")
        lifecycleScope.launch {
            runCatching {
                viewModel.getAccountDeviceInfo()
            }.onSuccess {
//                Log.i("MainActivity","getAccountDeviceInfo onSuccess getDeviceList=>$it")
                if (it.success == 1){
                        viewModel.saveAccountDeviceInfo(it)
                    if (it.data.isNotEmpty()){
                        getDeviceAccountId()
                        viewModel.isHasTemperature()
                    }else{
                        viewModel.closeSocket()
                    }
                }
            }.onFailure {
//                Log.i("MainActivity","getAccountDeviceInfo onFailure e=>${it.message}")
                it.printStackTrace()
                if (it.message.isNullOrBlank()) binding.root.snackbar(this@MainActivity.getString(R.string.unknown_error_occurred))
                else {
                    val getMessage: Pair<Boolean, String> = it.message!!.apiErrorShow(this@MainActivity)
//                    Log.i("MainActivity", "logout onFailure getMessage first=>${getMessage.first}")
                    binding.root.snackbar(getMessage.second)
                    if (getMessage.first){
                        //logout
                        viewModel.tokenFailLogout.value = true
                        this@MainActivity.toast(R.string.logout)
                    }
                }
            }
        }
    }

//    private fun getAccountDeviceInfo(){
//        Log.i("MainActivity","getAccountDeviceInfo")
//        lifecycleScope.launch {
//            runCatching {
//                viewModel.getAccountDeviceInfo(this@MainActivity)
//            }.onSuccess {
//                Log.i("MainActivity","getAccountDeviceInfo onSuccess getDeviceList=>$it")
//                //is device bind?
//                if (it.isNotEmpty()){
//                    getDeviceAccountId()
//                    viewModel.isHasTemperature()
//                }else viewModel.closeSocket()
//            }.onFailure {
//                Log.i("MainActivity","getAccountDeviceInfo onFailure e=>${it.message}")
//                it.printStackTrace()
//                binding.root.snackbar(it)
//            }
//        }
//    }

    // if (it.success == 1){
    //                    Log.i("DeviceRepository","getAccountDeviceInfo success data=>${res.data}")
    //                    if (res.data.isNotEmpty()){
    //                        //deviceType 0:ra  1:air
    //                        setDevice(0,res.data.firstOrNull { it.deviceType == 0 })
    //                        setDevice(1,res.data.firstOrNull { it.deviceType == 1 })
    //                        //感知器會有多台 無法使用first 假如deviceType是4?
    //                        val getActivityDeviceList: MutableList<CheckDeviceResponse.DeviceInfo> = mutableListOf()
    //                        res.data.map { if (it.deviceType == 4) getActivityDeviceList.add(it) }
    //                        if (getActivityDeviceList.isNotEmpty()) getActivityDeviceList.map { Log.i("DeviceRepository","看如何新增多台紀錄 db?") } else Log.i("DeviceRepository","看如何移除多台紀錄 db?")
    //                    }else{
    //                        //表示無裝置
    //                        Log.i("DeviceRepository","getAccountDeviceInfo 無裝置")
    //                        allClean()
    //                    }

    //設定右上異常通知顯示
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.top_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        super.onCreateOptionsMenu(menu)
        return when(viewModel.getShowIcon()){
            1 -> {
                menu?.getItem(0)?.isVisible = true
                true
            }
            2 -> {
                menu?.getItem(1)?.isVisible = true
                true
            }
            else -> true
        }
    }

    //webSocket
    private fun getDeviceAccountId() {
        Log.i("MainActivity","getDeviceAccountId")
        viewModel.getDeviceAccountId().let {
            if (it != -1){
                connectionWebSocket(it)
            }else{
                lifecycleScope.launch {
                    viewModel.closeSocket()
                }
            }
        }
    }

    private fun connectionWebSocket(accountId: Int){
        //後端傳送4狀態正躺 側躺 坐在床邊 離床
        //狀態顏色&&圖片 在床&&側躺＆＆ 藍色 圖相同 值：狀態顯示文字不同 ; 離床&&無人：顏色＝>灰色 圖不同 無值--(直接顯示不管是否有收到呼吸等值);
        Log.i("MainActivity","connectionWebSocket")
        lifecycleScope.launch {
            try {
                Log.i("MainActivity","connectionWebSocket try")
                viewModel.startSocket(this@MainActivity.applicationContext, accountId).consumeEach {
//                    Log.i("MainActivity","connectionWebSocket getSocketData=>$it}")
                    main {  viewModel.getSocketData.value = it  }//
                }

            } catch (ex: java.lang.Exception) {
                binding.root.snackbar(ex)

                Log.i("MainActivity","connectionWebSocket catch ex=>${ex.message}")
            }
        }

    }

    private fun checkBluetooth() {
//        if ( viewModel.isBluetoothEnabled()!= null){
//            when {
//                viewModel.isBluetoothEnabled()!! -> Log.i("MainActivity","藍芽打開 需要監聽服務可以寫在這")
//                else -> activityLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
//            }
//        }
        when {
            viewModel.isBluetoothEnabled(this)-> Log.i("MainActivity","藍芽打開 需要監聽服務可以寫在這")
            else -> activityLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

    }

}