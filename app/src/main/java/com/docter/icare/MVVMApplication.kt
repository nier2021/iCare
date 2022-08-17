package com.docter.icare
import android.app.Application
import com.docter.icare.data.bleUtil.device.air.AirBleDataManager
import com.docter.icare.data.bleUtil.device.radar.RadarBleDataManager
import com.docter.icare.data.network.NetworkConnectionInterceptor
import com.docter.icare.data.network.api.ApiService
import com.docter.icare.data.network.api.webSocket.WebServices
import com.docter.icare.data.preferences.PreferenceProvider
import com.docter.icare.data.repositories.*
import com.docter.icare.data.resource.ResourceProvider
import com.docter.icare.ui.main.MainViewModelFactory
import com.docter.icare.ui.main.bedside.BedsideMonitorViewModelFactory
import com.docter.icare.ui.main.device.DeviceViewModelFactory
import com.docter.icare.ui.main.device.scan.DeviceScanViewModelFactory
import com.docter.icare.ui.main.uam.ActivityMonitoringViewModelFactory
import com.docter.icare.ui.start.login.LoginViewModelFactory
import com.docter.icare.ui.start.register.RegisterViewModelFactory
import com.docter.icare.ui.welcome.WelcomeViewModelFactory
import com.docter.icare.utils.PermissionCheckUtils
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule

@Suppress("unused")
class MVVMApplication : Application(), DIAware {

    override val di by DI.lazy {

        import(androidXModule(this@MVVMApplication))

        //Utils
        bind { singleton { ResourceProvider(instance()) } }
        bind { singleton { PermissionCheckUtils(instance()) } }
        bind { singleton { PreferenceProvider(instance()) } }
        bind { singleton { NetworkConnectionInterceptor(instance()) } }
        bind { singleton { ApiService(instance()) } }
        bind { singleton { RadarBleDataManager(instance()) } }
        bind { singleton { AirBleDataManager(instance()) } }
        bind { singleton { WebServices() } }
        //Repository
        bind { singleton { WelcomeRepository(instance(), instance(), instance(), instance()) } }
        bind { singleton { LoginRepository(instance(), instance(), instance()) } }
        bind { singleton { RegisterRepository(instance(), instance(), instance()) } }
        bind { singleton { MainRepository(instance(), instance(), instance(), instance(), instance()) } }
        bind { singleton { DeviceRepository(instance(), instance(), instance(),instance(),instance()) } }
        bind { singleton { RadarRepository(instance(), instance(), instance()) } }

        //Factory
        bind { provider { WelcomeViewModelFactory(instance()) } }
        bind { provider { LoginViewModelFactory(instance()) } }
        bind { provider { RegisterViewModelFactory(instance()) } }
        bind { provider { MainViewModelFactory(instance(),instance()) } }
        bind { provider { DeviceViewModelFactory(instance()) } }
        bind { provider { DeviceScanViewModelFactory(instance()) } }
        bind { provider { BedsideMonitorViewModelFactory(instance()) } }
        bind { provider { ActivityMonitoringViewModelFactory(instance()) } }


    }
}