package com.docter.icare.data.network.api.webSocket

import android.content.Context
import android.util.Log
import com.docter.icare.data.entities.webSocket.SocketUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.Channel
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class WebServices {
    private var _webSocket: WebSocket? = null

    private val socketOkHttpClient = OkHttpClient.Builder()
//        .readTimeout(60, TimeUnit.SECONDS)
//        .connectTimeout(60, TimeUnit.SECONDS)
//        .pingInterval(40, TimeUnit.SECONDS) // 设置 PING 帧发送间隔
        .hostnameVerifier { _, _ -> true }
        .retryOnConnectionFailure(true)//断线重连
//        .addInterceptor(interceptor)
        .build()

    //    @ExperimentalCoroutinesApi
    private var _webSocketListener: LinkWebSocketListener? = null

    //    @ExperimentalCoroutinesApi
    fun startSocket(context: Context, accountId: Int): Channel<SocketUpdate> =
        with(LinkWebSocketListener(context,accountId)) {
            startSocket(this)
            this@with.socketEventChannel
        }


    //    @ExperimentalCoroutinesApi
    private fun startSocket(webSocketListener: LinkWebSocketListener) {
        _webSocketListener = webSocketListener
        _webSocket = socketOkHttpClient.newWebSocket(
            Request.Builder().url("ws://104.43.14.124:6001/app/sleep-plat?protocol=7&client=js&version=7.0.3&flash=false").build(),
            webSocketListener
        )
    }

//    @ExperimentalCoroutinesApi
//    fun stopSocket() {
//        Log.i("WebServices","stopSocket")
//        try {
//            _webSocket?.close(NORMAL_CLOSURE_STATUS, null)
//            _webSocket = null
//            _webSocketListener?.socketEventChannel?.close()
//            _webSocketListener = null
//        } catch (ex: Exception) {
//            Log.i("WebServices","Exception=>$ex")
//        }
//    }

    fun stopSocket()
            = try {
        _webSocket?.close(NORMAL_CLOSURE_STATUS, null)
        _webSocket = null
        _webSocketListener?.socketEventChannel?.close()
        _webSocketListener = null
    } catch (ex: Exception) {
        Log.i("WebServices","Exception=>$ex")
        throw ex
    }


    companion object {
        const val NORMAL_CLOSURE_STATUS = 1000
    }
}