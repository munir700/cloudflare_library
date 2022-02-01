package com.sslcf.sslpinning

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import yap.sslpinning.CLOUDFLARE_DECRYPTED
import yap.sslpinning.DecryptCloudflareData
import java.security.PrivateKey

class DataDecryption {
     fun decryptAsymmetric(
        coroutineScope: CoroutineScope,
        passwordKey: String,
        encryptedFile: String,
        privateKey: PrivateKey
    ) {

        DecryptCloudflareData.decryptFileData(encryptedFile, privateKey) { output ->
            coroutineScope.launch(Dispatchers.IO) {

                try {
                    val jsonObject = JSONObject(output)
                    val jsonStr: String = jsonObject.getString(CLOUDFLARE_DECRYPTED)
                    YapHttpsBuilder().buildHttpClient(
                        passwordKey,
                        Base64.decode(jsonStr, Base64.NO_WRAP)
                    )
                } catch (e: JSONException) {
                    Log.e("JSONException","ex ${e.message}")
                }
            }
        }
    }
}