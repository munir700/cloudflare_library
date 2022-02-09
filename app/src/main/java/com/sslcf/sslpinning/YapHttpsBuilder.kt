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


    fun buildHttpClient(passwordKey: String, decryptedFile: ByteArray) {
        val okHttpClientBuilder = OkHttpClient.Builder()

        CerOkHttpClient().setupSSLSocket(
            decryptedFile,
            passwordKey,
            { sslSocketFactory, x509TrustManager ->
                run {
                    okHttpClientBuilder.sslSocketFactory(sslSocketFactory, x509TrustManager)
                }
            }, { failure ->
                Log.e("failure", "$failure")
                //TODO Developer will handle by revisit the Encryption/Decryption process
                // 3 reason may be there 1. corrupted file, 2. wrong password, 3. Trust manager not available on current device
            }
        )
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