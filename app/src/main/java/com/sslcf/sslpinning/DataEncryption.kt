package com.sslcf.sslpinning

import android.content.res.Resources
import android.util.Log
import com.sslcf.R
import org.json.JSONObject
import yap.sslpinninglibrary.DATA
import yap.sslpinninglibrary.ENCRYPTED_DATA
import yap.sslpinninglibrary.EncryptCloudflareData

/***
 *  Perform data encryption
 *  Simple read file and certificate
 *  In response provide encrypted String
 *  @author Munir Ahmad
 */
class DataEncryption {
    /**
     * Encrypt data by using symmetric and asymmetric techniques
     * This method read user file/data which is going encrypted and certificate which will used to encrypt the file/data
     * @param resources
     * @param passwordKey
     * on Successful Encryption remove ENCRYPTED_DATA from json object and call method to save data in firebase
     * on failure print exception
     */
    fun encryptionAsymmetric(
        resources: Resources,
        passwordKey: String
    ) {
        val inputFileByteArray = resources.openRawResource(R.raw.cf_cert).readBytes()
        val isCertificate = resources.openRawResource(R.raw.encryption_ca_cert)
        EncryptCloudflareData.encryptFileData(inputFileByteArray, isCertificate, { encryptedData ->
            Log.i("Encryption", "SUCCESS $encryptedData")
            var jsonEncryptedData: JSONObject? = null
            try {
                jsonEncryptedData = JSONObject(encryptedData)
                jsonEncryptedData.getJSONObject(DATA).remove(ENCRYPTED_DATA)

            } catch (e: Exception) {
                Log.i("Encryption", "Exception ${e.message}")
            }
            FirebaseHelper().setFirebaseDatabase(
                passwordKey,
                jsonEncryptedData.toString(),
                resources.openRawResource(R.raw.decryption_private_key).bufferedReader().use { it.readText() },
            )
        }, { failureMessage ->
            Log.i("Encryption", "Failed $failureMessage")
        })
    }


}