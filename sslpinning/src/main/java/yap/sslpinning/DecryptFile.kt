package yap.sslpinning

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object DecryptFile {

    @Throws(Exception::class)
    private fun decrypt(secretKey: SecretKey, fileData: ByteArray): ByteArray {
        val decrypted: ByteArray
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        decrypted = cipher.doFinal(fileData)
        return decrypted
    }

    fun decryptEncryptedFile(secretKey: SecretKey, keyStream: ByteArray): ByteArray {
        return decrypt(secretKey, keyStream)
    }


}