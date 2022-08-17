package com.docter.icare.data.network

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.docter.icare.R
import com.docter.icare.utils.NoInternetException
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

//Check network connection
class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {

    private val appContext = context.applicationContext

    /**
     * @throws NoInternetException If there is no Internet.
     */
    override fun intercept(chain: Chain): Response = when {

        !isInternetAvailable() -> throw NoInternetException(appContext.getString(R.string.net_connect_fail))

        else -> chain.proceed(chain.request())
    }

    private fun isInternetAvailable(): Boolean {

        val connectivityManager = appContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?

        when {

            SDK_INT < M -> {
                @Suppress("DEPRECATION")
                connectivityManager?.activeNetworkInfo?.run { return isConnected }
            }

            else -> connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {

                return when {
                    hasTransport(TRANSPORT_WIFI) || hasTransport(TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        }

        return false
    }
}