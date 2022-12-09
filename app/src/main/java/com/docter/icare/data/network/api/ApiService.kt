package com.docter.icare.data.network.api

import com.docter.icare.data.network.NetworkConnectionInterceptor
import com.docter.icare.data.network.api.response.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Call

@Suppress("SpellCheckingInspection")
interface ApiService {
    companion object{

        //TODO Change Url
//        private const val baseUrl = "https://www.hncare.cloud/horncloud/new_api/"
        private const val baseUrl = "http://104.43.14.124/api/"//小張電腦雷達波

        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ): ApiService {

            val okkHttpclient = OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .build()

            return Retrofit.Builder()
                .client(okkHttpclient)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }

    //連小張電腦
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("account") account: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    //連小張電腦
//    @FormUrlEncoded
//    @POST("binding-bioradar-device")
//    fun bindingRadarDevice(
//        @Field("sid") sid: String,
//        @Field("type") type: Int,
//        @Field("serialNumber") serialNumber: String,
//        @Field("deviceMac") macAddress: String,
//        @Field("deviceType") deviceType: Int
//    ): Call<BindingDeviceResponse>
//    訂閱和顯示狀態修改,最後再確定登出

    //
    @FormUrlEncoded
    @POST("divce-setting/binding-device")
    fun bindingDevice(
        @Header("Authorization") token: String,
        @Field("serialNumber") serialNumber: String,
        @Field("type") type: Int,
        @Field("deviceType") deviceType: Int,
        @Field("deviceMac") deviceMac: String,
    ):Call<BindingDeviceResponse>

    @GET("divce-setting/check-device")
    fun checkDevice(
        @Header("Authorization") token: String,
    ):Call<CheckDeviceResponse>


    @POST("logout")
    fun logout(
        @Header("Authorization") token: String,
    ):Call<DefaultResponse>

    //溫度設定send serverr紀錄
    @FormUrlEncoded
    @POST("divce-setting/temperature")
    fun setTemperatureCalibration(
        @Header("Authorization") token: String,
        @Field("serialNumber") serialNumber: String,
        @Field("member_id") memberId: Int,
        @Field("temperature") temperature: Float,
    ):Call<DefaultResponse>
}