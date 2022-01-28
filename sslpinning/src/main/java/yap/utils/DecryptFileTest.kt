package yap.utils

import android.content.Context
import encryption.*
import json.GsonJsonEngine
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.PrivateKey

fun Context.decryptFileData(
    fileData: String,
    certificateStream: InputStream,
    privateKey: PrivateKey,
    payload: (String) -> Unit
) {
    try {

        val encryptionCertificate =
            EncryptionUtils.loadEncryptionCertificate(certificateStream)
        val config =
            FieldLevelEncryptionConfigBuilder.aFieldLevelEncryptionConfig()
                .withEncryptionCertificate(encryptionCertificate)
                .withDecryptionKey(privateKey)
                .withDecryptionPath("$.EncryptedData", "$.DecryptedData")
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

        val finalPayload = FieldLevelEncryption.decryptPayload(
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

