package com.sslcf

import android.util.Log
import com.sslcf.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import yap.sslpinning.CerOkHttpClient
import javax.net.ssl.HttpsURLConnection

class YapHttpsBuilder {

    private fun getHttpBuilder(
        secureEncodedKey: String,
        decryptedFile: ByteArray
    ): OkHttpClient.Builder {
        val okHttpClientBuilder = OkHttpClient.Builder()
        CerOkHttpClient.setupOkHttpClientBuilderSSLSocket(
            okHttpClientBuilder,
            decryptedFile,
            secureEncodedKey
        )

        return okHttpClientBuilder
    }

     fun buildHttpClient(secureEncodedKey: String, decryptedFile: ByteArray) {
        val okHttpClientBuilder = getHttpBuilder(secureEncodedKey, decryptedFile)
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