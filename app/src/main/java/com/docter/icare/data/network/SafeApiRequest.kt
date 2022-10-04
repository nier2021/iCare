package com.docter.icare.data.network

import android.util.Log
import com.docter.icare.R
import com.docter.icare.data.resource.ResourceProvider
import com.docter.icare.utils.ApiConnectFailException
import com.docter.icare.utils.SidException
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Call

@Suppress("BlockingMethodInNonBlockingContext")
abstract class SafeApiRequest(
    private val resource: ResourceProvider
) {

    suspend fun<T: Any> apiRequest(call: suspend () -> Call<T>): T = call.invoke().execute().run {
        Log.i("SafeApiRequest","res=>${this.isSuccessful}")
        if (!isSuccessful) {
            Log.i("SafeApiRequest","!isSuccessful")
            throw ApiConnectFailException(resource.getString(R.string.api_connect_fail))
        }//除了成功以外其他取得皆是失敗,未來會改

        if (body() == null) {
            Log.i("SafeApiRequest","body() == null")
            throw ApiConnectFailException(resource.getString(R.string.api_connect_fail))
        }

        val json = JSONObject(Gson().toJson(body()))
        Log.i("SafeApiRequest","body() json=>$json")
        // body() json=>{"message":"帳號密碼錯誤"}
//        if (json.has("message") && json.getString("message").equals("帳號密碼錯誤")) {
//            throw SidException("${resource.getString(R.string.error_login_acc_pass_incorrect)}，${resource.getString(R.string.please_login_again)}")
//        }
//        if (json.has("success") && json.getInt("success") == 0) {
//
//            throw when (val message = json.getJSONObject("message").getString("ch")) {
//                "安全碼有誤" -> SidException("${message}，${resource.getString(R.string.please_login_again)}")
//                else -> ApiConnectFailException(message)
//            }
//        }
        if (json.has("success") && json.getInt("success") == 0) {
            throw if (json.has("message")) ApiConnectFailException(json.getString("message")) else ApiConnectFailException(resource.getString(R.string.unknown_error_occurred))
        }

        body()!!
    }
}