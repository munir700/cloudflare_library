package yap.sslpinning

import encryption.*
import json.GsonJsonEngine
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.PrivateKey

object DecryptCloudflareData {

    fun decryptFileData(
        fileData: String,
        privateKey: PrivateKey,
        payload: (String) -> Unit
    ) {
        try {

            val config =
                FieldLevelEncryptionConfigBuilder.aFieldLevelEncryptionConfig()
                    .withDecryptionKey(privateKey)
                    .withDecryptionPath("$.$DATA", "$.$CLOUDFLARE_DECRYPTED")
                    .withOaepPaddingDigestAlgorithm(SHA_256)
                    .withEncryptionCertificateFingerprintFieldName(PUBLIC_KEY_FINGER_PRINT)
                    .withEncryptedValueFieldName(ENCRYPTED_DATA)
                    .withEncryptedKeyFieldName(ENCRYPTED_KEY)
                    .withIvFieldName(IV)
                    .withOaepPaddingDigestAlgorithmFieldName(OAEP_HASHING_ALGORITHM)
                    .withTokenizationAuthenticationValueFieldName(TOKENIZATION_AUTHENTICATE_VALUE)
                    .withFieldValueEncoding(FieldLevelEncryptionConfig.FieldValueEncoding.HEX)
                    .build()
            FieldLevelEncryption.withJsonEngine(GsonJsonEngine())

            val finalPayload = FieldLevelEncryption.decryptPayload(
                fileData, config
            )
            //println("SamsungTestPayload Json>>$finalPayload")
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
}

