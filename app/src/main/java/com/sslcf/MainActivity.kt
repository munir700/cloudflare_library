package com.sslcf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
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
    private val TAG: String = "MainActivity"
    private val secureEncodedKey = "pdqxnxEE2U3hPCsKk/nFhA=="


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseDatabase()
        //encryptFile()
        lifecycleScope.launch(Dispatchers.IO) {
            buildHttpClient()
        }
    }


    private fun getHttpBuilder(secureEncodedKey: String): OkHttpClient.Builder {
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
        val okHttpClientBuilder = getHttpBuilder("")
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
        EncryptFile.encryptFileAndSaveMemory(EncryptionUtils.generateSecretKey(secureEncodedKey), file, keyStream)
    }

    private fun firebaseDatabase() {

        // Read from the database
        Firebase.database.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<HashMap<String, String>>()
                Log.d(TAG, "Value is: $value")

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }
}