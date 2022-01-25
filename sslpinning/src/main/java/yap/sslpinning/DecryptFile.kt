package yap.sslpinning

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object DecryptFile {
    /***
     * Decrypt file
     * Cipher provide functionality of a cryptographic decryption
     */
    @Throws(Exception::class)
    private fun decrypt(secretKey: SecretKey, fileData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    fun decryptEncryptedFile(secretKey: SecretKey, keyStream: ByteArray): ByteArray {
        return decrypt(secretKey, keyStream)
    }
}