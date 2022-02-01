package com.sslcf

import android.content.res.Resources
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import datastore.DataStoreManager
import org.json.JSONObject
import yap.sslpinning.EncryptCloudflareData

class DataEncryption {
    fun encryptionAsymmetric(resources: Resources, passwordKey: String) {
        val inputFileByteArray = resources.openRawResource(R.raw.dynamic).readBytes()
        val isCertificate = resources.openRawResource(R.raw.ca_cert)
        EncryptCloudflareData.encryptFileData(inputFileByteArray, isCertificate) { encryptedData ->
            Log.i("Encryption", "SUCCESS $encryptedData")
            val jsonEncryptedData = JSONObject(encryptedData)
            jsonEncryptedData.apply {
                this.getJSONObject("cloudflareEncryptedData").remove("encryptedData")
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
            this.getReference(DataStoreManager.PASS_WORD_KEY).setValue(passwordKey)
            this.getReference(DataStoreManager.RSA_ENCRYPTED_DATA).setValue(rsaEncryptedData)
            this.getReference(DataStoreManager.RSA_PRIVATE_KEY).setValue(rsaPrivateKey)
        }
    }
}