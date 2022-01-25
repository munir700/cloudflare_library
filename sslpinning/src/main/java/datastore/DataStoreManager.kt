package datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class DataStoreManager() {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "EncodedData")
        val SECURE_ENCODED_KEY = stringPreferencesKey("secure_encoded_key")
    }


    suspend fun saveSecureEncodedKey(context: Context,secureEncodedKey: String) {
        context.dataStore.edit { preferences ->
            preferences[SECURE_ENCODED_KEY] = secureEncodedKey
        }
    }

    fun getSecureEncodedKey(context: Context) = context.dataStore.data
        .map { preferences ->
            preferences[SECURE_ENCODED_KEY]
        }
}

