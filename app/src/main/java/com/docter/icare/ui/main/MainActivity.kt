package com.docter.icare.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.docter.icare.data.entities.device.DeviceInfoEntity
import com.docter.icare.data.entities.view.ExpandableListEntity
import com.docter.icare.data.entities.view.MainEntity
import com.docter.icare.data.entities.view.MainEntity.Companion.ACTIVITY_MONITORING
import com.docter.icare.data.entities.view.MainEntity.Companion.AIR_QUALITY_INDEX
import com.docter.icare.data.entities.view.MainEntity.Companion.BEDSIDE_MONITOR
import com.docter.icare.ui.main.expandableList.ExpandableListAdapter
import com.docter.icare.ui.start.StartActivity
import com.docter.icare.ui.start.login.LoginFragment
import com.docter.icare.utils.*
import com.docter.icare.utils.Coroutines.main
import com.docter.icare.view.dialog.CustomAlertDialog
import com.docter.icare.view.dialog.CustomProgressDialog
import com.google.android.material.navigation.NavigationBarView
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

        fragment.navController.addOnDestinationChangedListener { _, destination, bundle ->

            viewModel.destinationChange(destination)

//            with(binding.toolbar.menu) {
//                getItem(0).isVisible = viewModel.hasAdd(destination)
//                getItem(1).isVisible = viewModel.hasEdit(destination)
//                getItem(2).isVisible = viewModel.hasDone(destination)
//                getItem(3).isVisible = viewModel.hasShare(destination)
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
//                Log.i("MainActivity","toolbar setNavigationOnClickListener")
                navigationClick()
            }
            setOnMenuItemClickListener {
//                Log.i("MainActivity","toolbar setOnMenuItemClickListener")
                viewModel.menuClick(it)
            }
        }
    }

    private var bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener {
//        Log.i("MainActivity","itemId=>${it.itemId}")
        when (it.itemId) {

            R.id.nav_air_quality_index ->{
//                Log.i("MainActivity","airQualityIndexFragment")
//                navController.navigate(R.id.airQualityIndexFragment)//等做完再開啟
                this@MainActivity.toast("尚未開放")
                false
            }
            R.id.nav_activity_monitoring -> {
                R.id.nav_activity_monitoring
//                Log.i("MainActivity","activityMonitoringFragment")
                navController.navigate(R.id.activityMonitoringFragment)//等做完再開啟
                this@MainActivity.toast("尚未開放")
                true
            }
            else -> {
                //以上等做完再開啟
//                 R.id.nav_bedside_monitor
//                Log.i("MainActivity","bedsideMonitorFragment")
                navController.navigate(R.id.bedsideMonitorFragment)
//                binding.toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.welcome_status_bar))
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
                this@MainActivity.toast("尚未開放")
            }
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
            when{
                groupPosition == 0 && childPosition == 0 -> {
                    // this@MainActivity.toast("睡眠感知")
                    navController.navigate(R.id.device_navigation,bundleOf("deviceType" to "Radar"))
                }
//以下等做完再開啟
//                groupPosition == 0 && childPosition == 1 -> this@MainActivity.toast("空氣品質")
//                groupPosition == 0 && childPosition == 2 -> this@MainActivity.toast("姿態感知")//除了姿態感知 睡眠感知與空氣品質裝置都寫在一起
//                groupPosition == 1 && childPosition == 0 -> this@MainActivity.toast("關於")
//                groupPosition == 1 && childPosition == 1 -> this@MainActivity.toast("隱私權政策")
                groupPosition == 1 && childPosition == 2 -> if (!logoutDialog.isShowing()) logoutDialog.show()
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
            }else logout()
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
                viewModel.stopSocket()
            }.onSuccess {
                unBindDevice(deviceInfoEntity)
                Log.i("MainActivity","onClose  onSuccess")
            }.onFailure {
                Log.i("MainActivity","onClose onFailure e=>${it.message}")
                it.printStackTrace()
                binding.root.snackbar(it)
            }
        }
    }

    private fun unBindDevice(deviceInfoEntity: DeviceInfoEntity){
        main{progressDialog.show()}
        lifecycleScope.launch {
            runCatching {
                viewModel.unBindDevice(deviceInfoEntity)
            }.onSuccess {
                logout()
            }.onFailure {
                main{progressDialog.dismiss()}
                it.printStackTrace()
                binding.root.snackbar(it)
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

}