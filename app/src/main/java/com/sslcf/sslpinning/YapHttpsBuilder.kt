package com.sslcf.sslpinning

import android.util.Log
import com.sslcf.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import yap.sslpinninglibrary.CerOkHttpClient
import javax.net.ssl.HttpsURLConnection

class YapHttpsBuilder {

    private fun getHttpBuilder(
        passwordKey: String,
        decryptedFile: ByteArray
    ): OkHttpClient.Builder {
        val okHttpClientBuilder = OkHttpClient.Builder()
        CerOkHttpClient().setupOkHttpClientBuilderSSLSocket(
            okHttpClientBuilder,
            decryptedFile,
            passwordKey
        )

        return okHttpClientBuilder
    }

    fun buildHttpClient(passwordKey: String, decryptedFile: ByteArray) {
        val okHttpClientBuilder = getHttpBuilder(passwordKey, decryptedFile)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://test-mtls.yap.com/")
            .client(okHttpClientBuilder.build())
            .build()


        val service: ApiService = retrofit.create(ApiService::class.java)

        val loginUser: Call<ResponseBody> = service.loginUser()
        val response = loginUser.execute()

        if (response.code() == HttpsURLConnection.HTTP_OK) {
            Log.i("Response", "SUCCESS")
            val responseByte = response.body()?.string()?.toByteArray()
            responseByte?.let {
                Log.i("Response", "Body ${String(it)}")
            }

        } else {
            Log.e("Response", "FAILED")
        }

    }
}