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
import com.google.firebase.ktx.Firebase
import com.sslcf.api.ApiService
import datastore.DataStoreManager
import datastore.DataStoreManager.Companion.PASS_WORD_KEY
import datastore.DataStoreManager.Companion.RSA_ENCRYPTED_DATA
import datastore.DataStoreManager.Companion.RSA_PRIVATE_KEY
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
import yap.utils.EncodingUtils
import yap.utils.EncryptionUtils
import java.security.PrivateKey
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private val passwordKey = "pdqxnxEE2U3hPCsKk/nFhA=="


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.IO) {
            DataStoreManager().saveForceFirebaseFetch(this@MainActivity, false)
        }

        //DataEncryption().encryptionAsymmetric(resources, passwordKey)

        FirebaseOperation().getEncryptedData(lifecycleScope, this)

    }

}