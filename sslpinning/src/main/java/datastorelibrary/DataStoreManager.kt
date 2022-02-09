package datastorelibrary

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map


class DataStoreManager() {

    companion object {
        const val PASS_WORD_KEY = "pass_word_key"
        const val RSA_ENCRYPTED_DATA = "rsa_encrypted_data"
        const val RSA_PRIVATE_KEY = "rsa_private_key"
        private const val FORCE_FETCH_FIREBASE = "force_fetch_firebase"


        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "EncodedData")

        val SECURE_PASSWORD_PRE = stringPreferencesKey(PASS_WORD_KEY)
        val RSA_ENCRYPTED_PRE = stringPreferencesKey(RSA_ENCRYPTED_DATA)
        val RSA_PRIVATE_PRE = stringPreferencesKey(RSA_PRIVATE_KEY)
        val FORCE_FETCH_PRE = booleanPreferencesKey(FORCE_FETCH_FIREBASE)
    }


    suspend fun saveForceFirebaseFetch(
        context: Context,
        forceFirebaseFetch: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[FORCE_FETCH_PRE] = forceFirebaseFetch
        }
    }

    fun getForceFetchFirebase(context: Context) = context.dataStore.data
        .map { preferences ->
            preferences[FORCE_FETCH_PRE]
        }


    suspend fun saveSecureEncodedInfo(
        context: Context,
        passwordKey: String?,
        rsaEncryptedData: String?,
        rsaPrivateKey: String?
    ) {
        context.dataStore.edit { preferences ->
            preferences[SECURE_PASSWORD_PRE] = passwordKey ?: ""
            preferences[RSA_ENCRYPTED_PRE] = rsaEncryptedData ?: ""
            preferences[RSA_PRIVATE_PRE] = rsaPrivateKey ?: ""
        }
    }

    fun getDataStoreEncryptedInfo(context: Context) = context.dataStore.data
        .map { preferences ->
            DataStore(
                preferences[SECURE_PASSWORD_PRE]?.let {
                    String(Base64.decode(it, Base64.NO_WRAP))
                },
                preferences[RSA_ENCRYPTED_PRE]?.let {
                    String(Base64.decode(it, Base64.NO_WRAP))
                },
                preferences[RSA_PRIVATE_PRE]?.let {
                    String(Base64.decode(it, Base64.NO_WRAP))
                }
            )
        }
}

class DataStore(
    val passwordKey: String?,
    val rsaEncryptedData: String?,
    val rsaPrivateKey: String?
)

