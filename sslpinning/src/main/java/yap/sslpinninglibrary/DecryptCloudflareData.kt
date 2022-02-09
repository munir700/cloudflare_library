package yap.sslpinninglibrary

import encryption.*
import json.GsonJsonEngine
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.PrivateKey

/**
 * Wrapper class on Decryption
 * The basic purpose to interact with Encryption process by taking inputs from client and perform required
 * operation according to need and return Decrypted data.
 * @author Munir Ahmad
 */
class DecryptCloudflareData {
    /**
     * Decrypt data/file
     * To decrypt file/data encrypted data and private key required for decryption
     * @param fileDecryptedData
     * @param privateKey
     * @param success lambda
     * @param failure lambda
     */

    fun decryptFileData(
        fileDecryptedData: String,
        privateKey: PrivateKey,
        success: (String) -> Unit,
        failure: (String) -> Unit
    ) {
        try {

            val config =
                FieldLevelEncryptionConfigBuilder.aFieldLevelEncryptionConfig()
                    .withDecryptionKey(privateKey)
                    .withDecryptionPath(DOLLAR_DATA, DOLLAR_DECRYPTED)
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

            val finalDecryptedData = FieldLevelEncryption.decryptPayload(
                fileDecryptedData, config
            )
            success.invoke(finalDecryptedData)
        } catch (e: IOException) {
            e.printStackTrace()
            failure.invoke(e.stackTraceToString())
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            failure.invoke(e.stackTraceToString())
        } catch (e: EncryptionException) {
            e.printStackTrace()
            failure.invoke(e.stackTraceToString())
        } catch (e: InvalidSignatureException) {
            e.printStackTrace()
            failure.invoke(e.stackTraceToString())
        }
    }
}

