package yap.sslpinninglibrary

import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.*

/**
 * SSL/TLS pinning must have SSLSocketFactory in OkHttpClient.Builder.
 * SSLSocketFactory started by loading client certificates to build KeyStore.
 * Setup TrustManagerFactory and KeyManagerFactory to initiate SSLContext.
 * When all items are started correctly, then bind SSLSocketFactory to OkHttpClient.Builder.
 * @author Munir Ahmad
 */
class CerOkHttpClient {
    /**
     * Setup SSLSocketFactory for OkHttpClient.Builder
     *
     * @param decryptedFile
     * @param passwordKey
     * @param success lambda
     * @param failure lambda
     */
    fun setupSSLSocket(
        decryptedFile: ByteArray,
        passwordKey: String,
        success: (SSLSocketFactory, X509TrustManager) -> (Unit),
        failure: (String?) -> (Unit)
    ) {

        try {

            // Set up KeyStore
            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(decryptedFile.inputStream(), passwordKey.toCharArray())


            // Set up Trust Managers
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers


            // Set up Key Managers
            val keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, null)
            val keyManagers = keyManagerFactory.keyManagers


            // Obtain SSL Socket Factory
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, trustManagers, SecureRandom())


            getX509TrustManager(trustManagerFactory, failure)?.let {
                success.invoke(
                    sslContext.socketFactory,
                    it
                )
            }

        } catch (e: Exception) {
            failure.invoke(e.message)
        }
    }

    private fun getX509TrustManager(
        trustManagerFactory: TrustManagerFactory,
        failure: (String?) -> Unit
    ): X509TrustManager? {
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers == null || trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            val e: IllegalStateException =
                IllegalStateException("Wrong trust manager: " + Arrays.toString(trustManagers))
            failure.invoke(e.message)
            return null
        }
        return trustManagers[0] as X509TrustManager
    }

}