package com.sslcf.sslpinning

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import yap.sslpinninglibrary.CLOUDFLARE_DECRYPTED
import yap.sslpinninglibrary.DecryptCloudflareData
import java.security.PrivateKey

/***
 *  Perform data decryption
 *  Simple read decrypted file and private key
 *  In response provide decrypted String
 *  @author Munir Ahmad
 */
class DataDecryption {
    /**
     * Decrypt data by using symmetric and asymmetric techniques
     * This method read decrypted data which is going decrypted and, private key which will used to decrypt the file/data
     * @param coroutineScope
     * @param encryptedFile
     * @param privateKey
     * @param decryptedFile lambda
     * @param failure lambda
     * on Successful decryption get CLOUDFLARE_DECRYPTED from json object and call buildHttpClient()
     * on failure print exception
     */
    fun decryptAsymmetric(
        coroutineScope: CoroutineScope,
        encryptedFile: String,
        privateKey: PrivateKey,
        decryptedFile: (String) -> (Unit),
        failure: (String) -> (Unit)
    ) {
        DecryptCloudflareData().decryptFileData(encryptedFile, privateKey, { decryptedData ->
            coroutineScope.launch(Dispatchers.IO) {

                try {
                    val jsonDecryptedData = JSONObject(decryptedData)
                    decryptedFile.invoke(jsonDecryptedData.getString(CLOUDFLARE_DECRYPTED))
                } catch (e: JSONException) {
                    failure.invoke("Failed ${e.message}")
                }
            }
        }, { failureMessage ->
            failure.invoke("Failed $failureMessage")
        })
    }
}