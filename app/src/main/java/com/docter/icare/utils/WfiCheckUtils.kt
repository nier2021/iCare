package com.docter.icare.utils

import android.content.Context
import android.net.*
import android.net.wifi.WifiNetworkSpecifier
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit


class WfiCheckUtils(
    context: Context,
    wfiCheckUtilsCallBack: WfiCheckUtilsCallBack
) {
    private val appContext = context.applicationContext

    var connectivityManager: ConnectivityManager?= null
    var getSsid: String =""
    var getPassword: String =""

     fun connectToWifi(ssid: String, password:String) {

         getSsid = ssid
         getPassword = password

        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        connectivityManager =
            appContext.applicationContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager?.registerNetworkCallback(networkRequest,networkCallback )
        connectivityManager?.requestNetwork(networkRequest, networkCallback)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i("WfiCheckUtils","networkCallback onAvailable network=>$network")
            connectivityManager?.bindProcessToNetwork(network)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.i("WfiCheckUtils","networkCallback onLost network=>$network")
            unregisterNetWork()
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            Log.i("DeviceFragment","networkCallback onLinkPropertiesChanged network=>$network \n linkProperties=>$linkProperties")
            //連接成功後,切掉連接(因為他不會真正連wifi工作) 使wifi正常工作
            wfiCheckUtilsCallBack.onShowProgress(isShow = true)
            Thread.sleep(2000)
            wfiCheckUtilsCallBack.onShowProgress(isShow = false)
            unregisterNetWork()
            wfiCheckUtilsCallBack.onSuccess(ssid = getSsid, password =  getPassword)

        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Log.i("DeviceFragment","networkCallback onCapabilitiesChanged network=>$network \n networkCapabilities=>$networkCapabilities")
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Log.i("DeviceFragment","networkCallback onUnavailable")
            //失敗
            getSsid = ""
            getPassword = ""
            wfiCheckUtilsCallBack.onFail()
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            Log.i("DeviceFragment","networkCallback onBlockedStatusChanged network=>$network \n blocked=>$blocked")
        }

    }

    fun unregisterNetWork(){
        Log.e("DeviceFragment","unregisterNetWork")
        connectivityManager?.bindProcessToNetwork(null)
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }


    interface WfiCheckUtilsCallBack{
        fun onFail()
        fun onSuccess(ssid: String, password:String)
        fun onShowProgress(isShow: Boolean)
    }

}