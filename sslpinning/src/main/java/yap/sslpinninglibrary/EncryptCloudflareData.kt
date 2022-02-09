package yap.sslpinninglibrary

import android.util.Base64
import encryption.*
import json.GsonJsonEngine
import yap.utils.EncryptionUtils
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException

const val DATA = "data"
const val DOLLAR_DATA = "$.data"
const val DOLLAR_DECRYPTED = "$.decrypted"
const val CLOUDFLARE_DECRYPTED = "decrypted"
const val SHA_256 = "SHA-256"
const val PUBLIC_KEY_FINGER_PRINT = "publicKeyFingerprint"
const val ENCRYPTED_DATA = "encryptedData"
const val ENCRYPTED_KEY = "encryptedKey"
const val IV = "iv"
const val OAEP_HASHING_ALGORITHM = "oaepHashingAlgorithm"
const val TOKENIZATION_AUTHENTICATE_VALUE = "tokenizationAuthenticationValue"

/***
 * Wrapper class on Encryption
 * The basic purpose to interact with Encryption process by taking inputs from client and perform required
 * operation according to need and return Encrypted data.
 * @author Munir Ahmad
 */

class EncryptCloudflareData {
    /***
     * Encrypt Data/File
     * @param fileByteArray
     * @param certificateInputStream
     * @param success lambda
     * @param failure lambda
     *
     */
    fun encryptFileData(
        fileByteArray: ByteArray,
        certificateInputStream: InputStream,
        success: (String) -> Unit,
        failure: (String) -> Unit
    ) {
        try {
            val fileData =
                "{\"$DATA\": \"${Base64.encodeToString(fileByteArray, Base64.NO_WRAP)}\"}"
            val encryptionCertificate =
                EncryptionUtils.loadEncryptionCertificate(certificateInputStream)
            val config =
                FieldLevelEncryptionConfigBuilder.aFieldLevelEncryptionConfig()
                    .withEncryptionCertificate(encryptionCertificate)
                    .withEncryptionPath(DOLLAR_DATA, DOLLAR_DATA)
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

            val finalEncryptedData = FieldLevelEncryption.encryptPayload(
                fileData, config
            )
            success.invoke(finalEncryptedData)
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

