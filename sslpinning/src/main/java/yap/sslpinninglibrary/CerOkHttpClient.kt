package yap.sslpinninglibrary

import android.util.Log
import okhttp3.OkHttpClient
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
object CerOkHttpClient {
    /**
     * Setup SSLSocketFactory for OkHttpClient.Builder
     *
     * @param builder
     * @param decryptedFile
     * @param passwordKey
     */
    fun setupOkHttpClientBuilderSSLSocket(
        builder: OkHttpClient.Builder,
        decryptedFile: ByteArray,
        passwordKey: String
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
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            getX509TrustManager(trustManagerFactory).let {
                builder.sslSocketFactory(sslSocketFactory, it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Exception", "error${e.printStackTrace()}")
        }
    }

    private fun getX509TrustManager(trustManagerFactory: TrustManagerFactory): X509TrustManager {
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers == null || trustManagers.size != 1 || trustManagers[0] !is X509TrustManager
        ) {
            val e = IllegalStateException("Wrong trust manager: " + Arrays.toString(trustManagers))
            throw e
        }
        return trustManagers[0] as X509TrustManager
    }

}