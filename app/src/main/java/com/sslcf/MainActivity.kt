package com.sslcf

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sslcf.mohre.MobileSSOHttpsBuilder
import com.sslcf.sslpinning.DataDecryption
import com.sslcf.sslpinning.DataOperation
import datastorelibrary.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import yap.utils.EncryptionUtils


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private val passwordKey = "pdqxnxEE2U3hPCsKk/nFhA=="


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.IO) {
            DataStoreManager().saveForceFirebaseFetch(this@MainActivity, true)

            delay(2000)


        }

        //DataEncryption().encryptionAsymmetric(resources, passwordKey)

        findViewById<Button>(R.id.login).setOnClickListener {
            setText("Start Logging")
            enableSSLPinning()
        }
    }

    private fun enableSSLPinning() {
        DataOperation().getEncryptedData(lifecycleScope, this@MainActivity, { dataStore ->
            val encryptedData =
                DataOperation().buildEncryptedData(dataStore = dataStore)
            if (encryptedData == null) {
                Log.e(TAG, "Data encrypted retrieval failed")
                return@getEncryptedData
            }
            val privateKey =
                EncryptionUtils.loadDecryptionKey(dataStore.rsaPrivateKey?.byteInputStream())
            DataDecryption().decryptAsymmetric(
                lifecycleScope,
                encryptedData,
                privateKey,
                { decryptedFile ->
                    MobileSSOHttpsBuilder().buildHttpClient(
                        dataStore.passwordKey!!,
                        Base64.decode(decryptedFile, Base64.NO_WRAP)
                    ) { result ->
                        setText(result = result)
                    }
                }, { decryptDataFailure ->
                    Log.e(TAG, "Data encryption process $decryptDataFailure")
                }
            )
        }, { dataRetrievalFailure ->
            Log.e(TAG, "Data retrieval process $dataRetrievalFailure")
        })
    }

    private fun setText(result: String) {
        this.runOnUiThread {
            findViewById<TextView>(R.id.show_login_content).text = result
        }
    }
}