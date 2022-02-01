package com.sslcf.sslpinning

import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
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

                val jsonObject = JSONObject(output)
                val jsonStr: String = jsonObject.getString("decryptedData")
                YapHttpsBuilder().buildHttpClient(
                    passwordKey,
                    Base64.decode(jsonStr, Base64.NO_WRAP)
                )
            }
        }
    }
}