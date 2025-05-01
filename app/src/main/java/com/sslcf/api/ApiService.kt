package com.sslcf.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("auth/login")
    fun loginUser(): Call<ResponseBody>

    @GET("messages/api/otp/sign-up/mobile-no")
    fun signupUser(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("connect/login")
    fun connectLogin(@FieldMap names: Map<String, String>): Call<ResponseBody>
}