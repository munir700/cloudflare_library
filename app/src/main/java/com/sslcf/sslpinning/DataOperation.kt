package com.sslcf.sslpinning

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.sslcf.BuildConfig
import com.sslcf.NativeCloudflareData
import datastorelibrary.DataStore
import datastorelibrary.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import yap.sslpinninglibrary.DATA
import yap.sslpinninglibrary.ENCRYPTED_DATA
import yap.sslpinninglibrary.OAEP_HASHING_ALGORITHM

/***
 * Perform data operations to encrypt data.
 * Main responsibilities to interact with firebase to fetch encrypted data and save into DataStore
 * On storing encrypted data into DataStore start process to encrypt data
 * @author Munir Ahmad
 */
class DataOperation {
    /***
     * Decide if the data is not stored in the DataStore, retrieve it from Firebase first.
     * After retrieving from the firebase, the data is then stored in the DataStore.
     * DataStore is activated when values change in preferences.
     * BuildEncryptedData () on callback call method
     * So the main purpose of this method is to get data from Firebase and store it in DataStore.
     * @param coroutineScope
     * @param context
     */
    fun getEncryptedData(
        coroutineScope: CoroutineScope,
        context: Context,
        dataStoreSuccess: (DataStore) -> (Unit),
        failure: (String?) -> (Unit)
    ) {
        coroutineScope.launch(Dispatchers.Default) {
            DataStoreManager().getForceFetchFirebase(context).catch { e ->
                e.printStackTrace()
                failure.invoke(e.message)
            }.collect { isForceFirebaseFetch ->
                if (isForceFirebaseFetch == true) {
                    FirebaseHelper().getEncryptedDataFirebase(coroutineScope, context, failure)
                } else {
                    DataStoreManager().getDataStoreEncryptedInfo(context).catch { e ->
                        e.printStackTrace()
                        failure.invoke(e.message)
                    }.collect { dataStore ->
                        if (dataStore.rsaEncryptedData.isNullOrEmpty())
                            FirebaseHelper().getEncryptedDataFirebase(
                                coroutineScope,
                                context,
                                failure
                            )
                        else
                            dataStoreSuccess.invoke(dataStore)
                    }
                }
            }
        }
    }

    /**
     * Decide that all relevant data exists then create a JSONObject to insert the ENCRYPTED_DATA attribute required for the decryption process.
     * Make encryption private using encryption utilities
     * Call method to perform the actual encryption process
     * @param dataStore
     */
    fun buildEncryptedData(dataStore: DataStore): String? {
        if (dataStore.rsaEncryptedData != null && dataStore.rsaPrivateKey != null && dataStore.passwordKey != null) {
            val jsonEncryptedData: JSONObject
            try {
                jsonEncryptedData = JSONObject(dataStore.rsaEncryptedData!!)
                jsonEncryptedData.getJSONObject(DATA).put(
                    ENCRYPTED_DATA,
                    "${NativeCloudflareData.encryptedPart1()}${NativeCloudflareData.encryptedPart2()}${BuildConfig.encryptedDataPart3}${BuildConfig.encryptedDataPart4}"

                )
                jsonEncryptedData.getJSONObject(DATA).put(
                    OAEP_HASHING_ALGORITHM,
                    "SHA256"
                )
                return jsonEncryptedData.toString()

            } catch (e: JSONException) {
                Log.e("JSONException", "ex ${e.message}")
            } catch (e: Exception) {
                Log.e("Exception", "ex ${e.message}")
            }
        }
        return null
    }

    /**
     * Extract data from snapshot and store data in data store.
     *
     * @param coroutineScope
     * @param context
     * @param dataSnapshot
     */

    fun putEncryptedDataDataStore(
        coroutineScope: CoroutineScope,
        context: Context,
        dataSnapshot: DataSnapshot
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            DataStoreManager().saveSecureEncodedInfo(
                context,
                (dataSnapshot.value as HashMap<String, String>)[DataStoreManager.PASS_WORD_KEY],
                (dataSnapshot.value as HashMap<String, String>)[DataStoreManager.RSA_ENCRYPTED_DATA],
                (dataSnapshot.value as HashMap<String, String>)[DataStoreManager.RSA_PRIVATE_KEY]
            )
        }
    }
}