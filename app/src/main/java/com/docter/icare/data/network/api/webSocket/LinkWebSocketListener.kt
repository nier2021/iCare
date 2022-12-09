package com.docter.icare.data.network.api.webSocket

import android.content.Context
import android.util.Log
import com.docter.icare.R
import com.docter.icare.data.entities.view.BioRadarEntity
import com.docter.icare.data.entities.webSocket.SocketUpdate
import com.docter.icare.data.network.api.webSocket.WebServices.Companion.NORMAL_CLOSURE_STATUS
import com.docter.icare.data.network.api.webSocket.response.WebSocketJson
import com.docter.icare.utils.Coroutines
import okhttp3.WebSocketListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString

//@ExperimentalCoroutinesApi
class LinkWebSocketListener(context: Context, accountId: Int) : WebSocketListener() {
    val socketEventChannel: Channel<SocketUpdate> = Channel(10)
    private var linkFlag: Int = -1
    private var pushId = accountId
    private var appContext = context

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
//        Log.i("LinkWebSocketListener","onOpen webSocket=>$webSocket \n response=>$response")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
//        Log.i("LinkWebSocketListener","onMessage 1 webSocket=>$webSocket \n text=>$text")

        if (linkFlag == -1 && text.isNotEmpty()){
            val getJsonData = WebSocketJson().pusherSubscribeJson(text)
//            Log.i("LinkWebSocketListener","onMessage getJsonData=>$getJsonData")
//            Log.i("LinkWebSocketListener","onMessage getJsonData event=>${getJsonData.event}")
//            Log.i("LinkWebSocketListener","onMessage getJsonData event=>${getJsonData.data}")
//            Log.i("LinkWebSocketListener","onMessage getJsonData data socket_id=>${getJsonData.data.socket_id}")
            if (getJsonData.data.socket_id.isNotEmpty()){
//                webSocket.send("{\"data\": { \"auth\": \"\",\"channel\": \"status-channel\" },\"event\": \"pusher:subscribe\"}")
//                Log.i("LinkWebSocketListener","onMessage pushId=>${pushId}")
                    webSocket.send("{\"data\": {\"auth\": \"\",\"channel\": \"status-member-private-channel.$pushId\" },\"event\": \"pusher:subscribe\"}")
                    linkFlag = 1


            }else{
                linkFlag = -1
                Log.i("LinkWebSocketListener","onMessage 進行連線失敗")
                Coroutines.io {
                    socketEventChannel.send(SocketUpdate(error = appContext.getString(R.string.api_connect_fail)))
                    socketEventChannel.close()
                }
            }
        }else{
//            Log.i("LinkWebSocketListener","onMessage text2=>$text")
            if (linkFlag == 1 && text.isNotEmpty()) {
                WebSocketJson().sendResponse(text).run {
                    Log.i("LinkWebSocketListener","onMessage linkFlag == 1 event=>${this.event}")
                    if (event == "pusher_internal:subscription_succeeded")  linkFlag = 2
                    else {
                        Log.i("LinkWebSocketListener","onMessage 連線準備載入數值失敗...")
                        linkFlag = -1
                        Coroutines.io {
                            socketEventChannel.send(SocketUpdate(error =  appContext.getString(R.string.error_text)))
                            socketEventChannel.close()
                        }
                    }
                }
            }else{
                if (linkFlag == 2 && text.isNotEmpty()) {
                    val getRadarJsonData = WebSocketJson().responseJson(text)
//                    Log.i("LinkWebSocketListener","onMessage getRadarJsonData=>$getRadarJsonData")
//                    Log.i("LinkWebSocketListener","onMessage getRadarJsonData event=>${getRadarJsonData.event}")
//                    Log.i("LinkWebSocketListener","onMessage getRadarJsonData event=>${getRadarJsonData.data}")
//                    Log.i("LinkWebSocketListener","onMessage getRadarJsonData data time=>${getRadarJsonData.data.time}")
//                    Log.i("LinkWebSocketListener","onMessage getRadarJsonData data breath_state=>${getRadarJsonData.data.status.breath_state}")
                    Coroutines.io {
//                        socketEventChannel.send(SocketUpdate(text = "breath_state=>$${getRadarJsonData.data.status.breath_state}"))
                        try {
                            with(getRadarJsonData.data){
                                socketEventChannel.send(SocketUpdate(bioRadar = BioRadarEntity(accountId = accountId, time = time, distance = status.distance, heart_rate = status.heart_rate, bed_state = status.bed_state, breath_state = status.breath_state, temperature = status.temperature  )))
                            }
                        }catch (e:Exception){
                            Log.i("LinkWebSocketListener","onMessage Exception=>${e.message}")
                        }
                    }
                }else{
                    Log.i("LinkWebSocketListener","onMessage 取得裝置數值失敗...")
                    linkFlag = -1
                    Coroutines.io {
                        socketEventChannel.send(SocketUpdate(error = appContext.getString(R.string.device_send_error)))
                        socketEventChannel.close()
                    }
                }

            }
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        Log.i("LinkWebSocketListener","onMessage 2 webSocket=>$webSocket \n bytes=>$bytes")
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        linkFlag = -1
        socketEventChannel.close()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.i("LinkWebSocketListener","onClosing webSocket=>$webSocket \n code=>$code \n reason=>$reason")
//        Coroutines.io {
//            socketEventChannel.send(SocketUpdate(exception = SocketAbortedException()))
//        }
//        webSocket.close(NORMAL_CLOSURE_STATUS, null)
//        socketEventChannel.close()
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        linkFlag = -1
        socketEventChannel.close()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
//        webSocket.close(NORMAL_CLOSURE_STATUS, null)
//        linkFlag = -1
//        socketEventChannel.close()
        Log.i("LinkWebSocketListener","onClosed webSocket=>$webSocket \n code=>$code \n reason=>$reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.i("LinkWebSocketListener","onFailure webSocket=>$webSocket \n Throwable=>$t \n response=>$response")
        Coroutines.io {
            if (t.message?.contains("Failed to connect to") == true) socketEventChannel.send(SocketUpdate(error = appContext.getString(R.string.server_error))) else socketEventChannel.send(SocketUpdate(exception = t))
        }
    }


}