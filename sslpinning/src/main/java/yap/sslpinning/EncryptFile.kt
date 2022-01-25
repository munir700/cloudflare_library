package yap.sslpinning

import android.util.Log
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptFile {

    /**
     * Encrypt file by using Cipher
     * Cipher provide functionality of a cryptographic encryption
     */
    @Throws(Exception::class)
    private fun encrypt(secretKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = secretKey.encoded
        val secretKeySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKeySpec,
            IvParameterSpec(ByteArray(cipher.blockSize))
        )
        return cipher.doFinal(fileData)
    }


    /**
     * Use encrypt method to encrypt file providing secret key and byte array
     */
    fun encryptFileAndSaveMemory(secretKey: SecretKey, file: File, byteArray: ByteArray) {
        try {

            //encrypt file
            val encodedData = encrypt(secretKey, byteArray)

            //Save Encrypted file in app cache
            FileUtils.saveFile(encodedData, file)

        } catch (e: Exception) {
            Log.d("mTag", "${e.message}")
        }
    }

}