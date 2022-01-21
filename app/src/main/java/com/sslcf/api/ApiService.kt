package com.sslcf.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
interface ApiService {

    @GET("auth/login")
    fun loginUser(): Call<ResponseBody>

    @GET("messages/api/otp/sign-up/mobile-no")
    fun signupUser(): Call<ResponseBody>

}