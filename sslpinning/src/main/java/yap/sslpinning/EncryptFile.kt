package yap.sslpinning

import android.content.Context
import android.util.Log
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptFile {


    @Throws(Exception::class)
    private fun encrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = yourKey.encoded
        val secretKeySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKeySpec,
            IvParameterSpec(ByteArray(cipher.blockSize))
        )
        return cipher.doFinal(fileData)
    }


    fun encryptDownloadedFile(context: Context, encodedKey: String, file: File, byteArray: ByteArray) {
        try {
            //val fileData = FileUtils.readFile(file)


            //get secret key
            val secretKey = EncryptionUtils.generateSecretKey(encodedKey)

            //encrypt file
            val encodedData = encrypt(secretKey, byteArray)

            FileUtils.saveFile(encodedData, file)

        } catch (e: Exception) {
            Log.d("mTag", "${e.message}")
        }
    }

}