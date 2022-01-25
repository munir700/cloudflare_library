package yap.sslpinning

import android.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
    /**
     * Custom Secret file
     * Provide encoded key
     */
    fun generateSecretKey(encodedKey: String): SecretKey {
        val decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
}