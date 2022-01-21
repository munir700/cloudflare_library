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


    fun encryptDownloadedFile(context: Context, encodedKey: String) {
        try {
            val cloudflareCertificates = "cloudflare_cer.cer"
            val filesDirectory: String = context.filesDir.absolutePath
            val filePath = filesDirectory + File.separator + cloudflareCertificates
            val fileData = FileUtils.readFile(File(filePath))

            val keyStream = context.resources.openRawResource(R.raw.dynamic).readBytes()

            //get secret key
            val secretKey = EncryptionUtils.generateSecretKey(encodedKey)

            //encrypt file
            val encodedData = encrypt(secretKey, fileData)

            FileUtils.saveFile(encodedData, File(filePath))

        } catch (e: Exception) {
            Log.d("mTag", "${e.message}")
        }
    }

}