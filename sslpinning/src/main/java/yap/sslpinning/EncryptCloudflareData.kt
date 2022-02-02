package yap.sslpinning

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


object EncryptCloudflareData {

    fun encryptFileData(
        fileByteArray: ByteArray,
        certificateStream: InputStream,
        payload: (String) -> Unit
    ) {
        try {
            val fileData =
                "{\"$DATA\": \"${Base64.encodeToString(fileByteArray, Base64.NO_WRAP)}\"}"
            val encryptionCertificate =
                EncryptionUtils.loadEncryptionCertificate(certificateStream)
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

            val finalPayload = FieldLevelEncryption.encryptPayload(
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

