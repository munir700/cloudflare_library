package com.sslcf.sslpinning

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import yap.sslpinninglibrary.CLOUDFLARE_DECRYPTED
import yap.sslpinninglibrary.DecryptCloudflareData
import java.security.PrivateKey

class DataDecryption {
    fun decryptAsymmetric(
        coroutineScope: CoroutineScope,
        passwordKey: String,
        encryptedFile: String,
        privateKey: PrivateKey
    ) {
        DecryptCloudflareData.decryptFileData(encryptedFile, privateKey, { decryptedData ->
            coroutineScope.launch(Dispatchers.IO) {

                try {
                    val jsonDecryptedData = JSONObject(decryptedData)
                    val jsonDecryptedDataStr: String =
                        jsonDecryptedData.getString(CLOUDFLARE_DECRYPTED)
                    YapHttpsBuilder().buildHttpClient(
                        passwordKey,
                        Base64.decode(jsonDecryptedDataStr, Base64.NO_WRAP)
                    )
                } catch (e: JSONException) {
                    Log.e("JSONException", "ex ${e.message}")
                }
            }
        }, { failureMessage ->
            Log.i("Decryption", "Failed $failureMessage")
        })
    }
}