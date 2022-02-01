package com.sslcf

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sslcf.sslpinning.DataEncryption
import com.sslcf.sslpinning.FirebaseOperation
import datastore.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import yap.sslpinning.*


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private val passwordKey = "pdqxnxEE2U3hPCsKk/nFhA=="


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.IO) {
            DataStoreManager().saveForceFirebaseFetch(this@MainActivity, false)
        }

        DataEncryption().encryptionAsymmetric(lifecycleScope, resources, passwordKey)

        //FirebaseOperation().getEncryptedData(lifecycleScope, this)

    }

}