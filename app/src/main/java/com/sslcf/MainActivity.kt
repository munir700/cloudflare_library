package com.sslcf

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sslcf.sslpinning.DataDecryption
import com.sslcf.sslpinning.DataOperation
import com.sslcf.sslpinning.YapHttpsBuilder
import datastorelibrary.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import yap.sslpinning.*
import yap.utils.EncryptionUtils


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
        enableSSLPinning()

    }

    private fun enableSSLPinning() {
        DataOperation().getEncryptedData(lifecycleScope, this@MainActivity) { daraStore ->
            val encryptedData =
                DataOperation().buildEncryptedData(dataStore = daraStore)
            if (encryptedData == null) {
                Log.e(TAG, "Data encrypted retrieval failed")
                return@getEncryptedData
            }
            val privateKey =
                EncryptionUtils.loadDecryptionKey(daraStore.rsaPrivateKey!!.byteInputStream())
            DataDecryption().decryptAsymmetric(
                lifecycleScope,
                encryptedData,
                privateKey,
                { decryptedFile ->
                    YapHttpsBuilder().buildHttpClient(
                        daraStore.passwordKey!!,
                        Base64.decode(decryptedFile, Base64.NO_WRAP)
                    )
                }, { failure ->
                    Log.e(TAG, "Data encryption process $failure")
                }
            )
        }
    }

}