package yap.utils

import android.content.Context
import android.util.Base64
import encryption.*
import json.GsonJsonEngine
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException

fun Context.encryptFileData(
    fileData: ByteArray,
    certificateStream: InputStream,
    payload: (String) -> Unit
) {
    try {
        val fileData =
            "{\"fileData\": \"${Base64.encodeToString(fileData, Base64.NO_WRAP)}\"}"
        val encryptionCertificate =
            EncryptionUtils.loadEncryptionCertificate(certificateStream)
        val config =
            FieldLevelEncryptionConfigBuilder.aFieldLevelEncryptionConfig()
                .withEncryptionCertificate(encryptionCertificate)
                .withEncryptionPath("$.fileData", "$.EncryptedData")
                .withOaepPaddingDigestAlgorithm("SHA-256")
                .withEncryptionCertificateFingerprintFieldName("publicKeyFingerprint")
                //.withEncryptionKeyFingerprintFieldName("publicKeyFingerprint")
                .withEncryptedValueFieldName("encryptedData")
                .withEncryptedKeyFieldName("encryptedKey")
                .withIvFieldName("iv")
                .withOaepPaddingDigestAlgorithmFieldName("oaepHashingAlgorithm")
                .withTokenizationAuthenticationValueFieldName("tokenizationAuthenticationValue")
                .withFieldValueEncoding(FieldLevelEncryptionConfig.FieldValueEncoding.HEX)
                .build()
        FieldLevelEncryption.withJsonEngine(GsonJsonEngine())

        val finalPayload = FieldLevelEncryption.encryptPayload(
            fileData, config

        )

        println("SamsungTestPayload Json>>$finalPayload")
        payload.invoke(finalPayload)
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: GeneralSecurityException) {
        e.printStackTrace()
    } catch (e: EncryptionException) {
        e.printStackTrace()
    } catch (e: InvalidSignatureException) {
        e.printStackTrace()
    }

}

