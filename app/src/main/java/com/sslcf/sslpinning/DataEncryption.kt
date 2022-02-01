package com.sslcf.sslpinning

import android.content.res.Resources
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sslcf.R
import datastore.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject
import yap.sslpinning.DATA
import yap.sslpinning.ENCRYPTED_DATA
import yap.sslpinning.EncryptCloudflareData

class DataEncryption {
    fun encryptionAsymmetric(
        coroutineScope: CoroutineScope,
        resources: Resources,
        passwordKey: String
    ) {
        val inputFileByteArray = resources.openRawResource(R.raw.dynamic).readBytes()
        val isCertificate = resources.openRawResource(R.raw.ca_cert)
        EncryptCloudflareData.encryptFileData(inputFileByteArray, isCertificate) { encryptedData ->
            Log.i("Encryption", "SUCCESS $encryptedData")
            val jsonEncryptedData = JSONObject(encryptedData)
            //val temp = jsonEncryptedData.getJSONObject(DATA).getString(ENCRYPTED_DATA)
            jsonEncryptedData.getJSONObject(DATA).apply {
                this.remove(ENCRYPTED_DATA)
            }
            setFirebaseDatabase(
                passwordKey,
                jsonEncryptedData.toString(),
                resources.openRawResource(R.raw.private_key).bufferedReader().use { it.readText() },
            )

            /* jsonEncryptedData.getJSONObject(DATA).apply {
                 this.put(ENCRYPTED_DATA, temp)
             }

             FirebaseOperation().test(
                 coroutineScope,
                 resources.openRawResource(R.raw.private_key).buffered(),
                 jsonEncryptedData.toString()
             )*/
        }
    }

    private fun setFirebaseDatabase(
        passwordKey: String,
        rsaEncryptedData: String,
        rsaPrivateKey: String
    ) {
        Firebase.database.apply {
            this.getReference(DataStoreManager.PASS_WORD_KEY).setValue(passwordKey)
            this.getReference(DataStoreManager.RSA_ENCRYPTED_DATA).setValue(rsaEncryptedData)
            this.getReference(DataStoreManager.RSA_PRIVATE_KEY).setValue(rsaPrivateKey)
        }
    }
}