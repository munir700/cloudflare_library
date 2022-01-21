package com.sslcf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.sslcf.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import yap.sslpinning.*
import java.io.File
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    private val secureEncodedKey = "pdqxnxEE2U3hPCsKk/nFhA=="
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //encryptFile()

        lifecycleScope.launch(Dispatchers.IO) {
            buildHttpClient()
        }

    }

    private fun getHttpBuilder(): OkHttpClient.Builder {
        val okHttpClientBuilder = OkHttpClient.Builder()
        val keyStream = resources.openRawResource(R.raw.cloudflare_cer).readBytes()
        val decryptedFile = DecryptFile.decryptEncryptedFile(
            EncryptionUtils.generateSecretKey(secureEncodedKey),
            keyStream
        )
        CerOkHttpClient.setupOkHttpClientBuilderSSLSocket(
            okHttpClientBuilder,
            decryptedFile,
            secureEncodedKey
        )

        return okHttpClientBuilder
    }

    private fun buildHttpClient() {
        val okHttpClientBuilder = getHttpBuilder()
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


    private fun encryptFile() {
        val cloudflareCertificates = "cloudflare_cer.cer"
        val filesDirectory: String = filesDir.absolutePath
        val filePath = filesDirectory + File.separator + cloudflareCertificates
        val file = File(filePath)
        //val fileData = FileUtils.readFile(File(filePath))

        val keyStream = resources.openRawResource(R.raw.dynamic).readBytes()
        EncryptFile.encryptDownloadedFile(this, secureEncodedKey, file, keyStream)
    }
}