package com.sslcf

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sslcf.api.ApiService
import datastore.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import yap.sslpinning.*
import yap.utils.decryptFileData
import yap.utils.encryptFileData
import java.io.File
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private val secureEncodedKey = "pdqxnxEE2U3hPCsKk/nFhA=="


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        encryptionAsymmetric()


        //getSecureKeyFirebase()

    }

    private fun encryptionAsymmetric() {
        val inputFileByteArray = resources.openRawResource(R.raw.dynamic).readBytes()
        val isCertificate = resources.openRawResource(R.raw.ca_cert)
        this.encryptFileData(inputFileByteArray, isCertificate) {
            Log.i("Encryption", "SUCCESS $it")
            decryptAsymmetric(it)
        }
    }

    private fun decryptAsymmetric(encryptedFileData: String) {

        val isCertificate = resources.openRawResource(R.raw.ca_cert)
        val privateKey =
            yap.utils.EncryptionUtils.loadDecryptionKey(resources.openRawResource(R.raw.private_unencrypted))
        this.decryptFileData(encryptedFileData, isCertificate, privateKey) { output ->
            lifecycleScope.launch(Dispatchers.IO) {

                val jsonObject = JSONObject(output)
                val jsonStr: String = jsonObject.getString("DecryptedData")
                buildHttpClient(secureEncodedKey, Base64.decode(jsonStr, Base64.NO_WRAP))
            }
        }
    }


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

    private fun buildHttpClient(secureEncodedKey: String, decryptedFile: ByteArray) {
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

    private fun getSecureKeyFirebase() {
        lifecycleScope.launch(Dispatchers.IO) {
            DataStoreManager().getSecureEncodedKey(this@MainActivity).catch { e ->
                e.printStackTrace()
            }.collect {
                /*if (it.isNullOrEmpty())
                    firebaseDatabase()
                else
                    buildHttpClient(it, "")*/
            }
        }
    }

    private fun encryptFile() {
        val cloudflareCertificates = "cloudflare_cer.cer"
        val filesDirectory: String = filesDir.absolutePath
        val filePath = filesDirectory + File.separator + cloudflareCertificates
        val file = File(filePath)
        //val fileData = FileUtils.readFile(File(filePath))

        val keyStream = resources.openRawResource(R.raw.dynamic).readBytes()
        EncryptFile.encryptFileAndSaveMemory(
            EncryptionUtils.generateSecretKey(secureEncodedKey),
            file,
            keyStream
        )
    }

    private fun firebaseDatabase() {

        // Read from the database
        Firebase.database.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                lifecycleScope.launch(Dispatchers.IO) {
                    val value = dataSnapshot.getValue<HashMap<String, String>>()
                    Log.d(TAG, "Value is: $value")
                    value?.get("secure_encoded_key").let {
                        DataStoreManager().saveSecureEncodedKey(this@MainActivity, it!!)
                        //buildHttpClient(it, "")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }
}