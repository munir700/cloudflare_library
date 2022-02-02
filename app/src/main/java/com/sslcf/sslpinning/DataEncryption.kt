package com.sslcf.sslpinning

import android.content.res.Resources
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sslcf.R
import datastore.DataStoreManager
import org.json.JSONObject
import yap.sslpinning.DATA
import yap.sslpinning.ENCRYPTED_DATA
import yap.sslpinning.EncryptCloudflareData

class DataEncryption {
    fun encryptionAsymmetric(
        resources: Resources,
        passwordKey: String
    ) {
        val inputFileByteArray = resources.openRawResource(R.raw.dynamic).readBytes()
        val isCertificate = resources.openRawResource(R.raw.ca_cert)
        EncryptCloudflareData.encryptFileData(inputFileByteArray, isCertificate) { encryptedData ->
            Log.i("Encryption", "SUCCESS $encryptedData")
            var jsonEncryptedData: JSONObject? = null
            try {
                jsonEncryptedData = JSONObject(encryptedData)
                jsonEncryptedData.getJSONObject(DATA).remove(ENCRYPTED_DATA)

            } catch (e: Exception) {
                Log.i("Encryption", "Exception ${e.message}")
            }
            setFirebaseDatabase(
                passwordKey,
                jsonEncryptedData.toString(),
                resources.openRawResource(R.raw.private_key).bufferedReader().use { it.readText() },
            )
        }
    }

    private fun setFirebaseDatabase(
        passwordKey: String,
        rsaEncryptedData: String,
        rsaPrivateKey: String
    ) {
        Firebase.database.apply {
            this.reference.removeValue()
            this.getReference(DataStoreManager.PASS_WORD_KEY).setValue(passwordKey)
            this.getReference(DataStoreManager.RSA_ENCRYPTED_DATA).setValue(rsaEncryptedData)
            this.getReference(DataStoreManager.RSA_PRIVATE_KEY).setValue(rsaPrivateKey)
        }
    }
}