package com.sslcf.sslpinning

import android.content.res.Resources
import android.util.Base64
import android.util.Log
import com.sslcf.R
import org.json.JSONObject
import yap.sslpinninglibrary.DATA
import yap.sslpinninglibrary.ENCRYPTED_DATA
import yap.sslpinninglibrary.EncryptCloudflareData
import yap.sslpinninglibrary.OAEP_HASHING_ALGORITHM

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

        EncryptCloudflareData().encryptFileData(
            inputFileByteArray,
            isCertificate,
            { encryptedData ->
                Log.i("Encryption", "SUCCESS $encryptedData")
                var jsonEncryptedData: JSONObject? = null
                try {
                    jsonEncryptedData = JSONObject(encryptedData)
                    jsonEncryptedData.getJSONObject(DATA).remove(ENCRYPTED_DATA)
                    jsonEncryptedData.getJSONObject(DATA).remove(OAEP_HASHING_ALGORITHM)

                } catch (e: Exception) {
                    Log.i("Encryption", "Exception ${e.message}")
                }

                FirebaseHelper().setFirebaseDatabase(
                    Base64.encodeToString(passwordKey.toByteArray(), Base64.NO_WRAP),
                    
                    Base64.encodeToString(
                        jsonEncryptedData.toString().toByteArray(),
                        Base64.NO_WRAP
                    ),

                    Base64.encodeToString(
                        (resources.openRawResource(R.raw.decryption_private_key).readBytes()),
                        Base64.NO_WRAP
                    )
                )
            },
            { failureMessage ->
                Log.i("Encryption", "Failed $failureMessage")
            })
    }


}