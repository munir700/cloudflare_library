package com.sslcf.sslpinning

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import datastorelibrary.DataStore
import datastorelibrary.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import yap.sslpinninglibrary.DATA
import yap.sslpinninglibrary.OAEP_HASHING_ALGORITHM

/***
 * Perform data operations to encrypt data.
 * Main responsibilities to interact with firebase to fetch encrypted data and save into DataStore
 * On storing encrypted data into DataStore start process to encrypt data
 * @author Munir Ahmad
 */
class DataOperation {
    /**
     * Retrieves encrypted data, prioritizing DataStore unless forced Firebase fetch is enabled.
     *
     * Data flow:
     * 1. Check if forced Firebase fetch flag is enabled
     * 2. If yes, fetch from Firebase and store in DataStore
     * 3. If no, try to get data from DataStore
     * 4. If DataStore has no data, fetch from Firebase as fallback
     *
     * @param coroutineScope Coroutine scope to launch operations
     * @param context Android context
     * @param dataStoreSuccess Callback when data is successfully retrieved
     * @param failure Callback when an error occurs
     */
    fun getEncryptedData(
        coroutineScope: CoroutineScope,
        context: Context,
        dataStoreSuccess: (DataStore) -> Unit,
        failure: (String?) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) { // IO dispatcher is better for data operations
            try {
                // Check if we should force fetch from Firebase
                val isForceFirebaseFetch = try {
                    DataStoreManager().getForceFetchFirebase(context)
                        .first() // Using first() instead of collect for single value
                } catch (e: Exception) {
                    Log.e("DataRetrieval", "Error checking force fetch flag: ${e.message}")
                    e.printStackTrace()
                    failure(e.message)
                    false // Default to false if there's an error
                }

                if (isForceFirebaseFetch == true || isForceFirebaseFetch == null) {
                    Log.d("DataRetrieval", "Force fetching from Firebase")
                    FirebaseHelper().getEncryptedDataFirebase(
                        coroutineScope = coroutineScope,
                        context = context,
                        failure = failure,
                        success = {
                            getEncryptedDataFromDataStore(
                                coroutineScope = coroutineScope,
                                context = context,
                                dataStoreSuccess = dataStoreSuccess,
                                failure = failure
                            )
                        }
                    )
                } else {
                    // Try DataStore first
                    getEncryptedDataFromDataStore(
                        coroutineScope = coroutineScope,
                        context = context,
                        dataStoreSuccess = dataStoreSuccess,
                        failure = failure
                    )
                }
            } catch (e: Exception) {
                Log.e("DataRetrieval", "Unexpected error in data retrieval: ${e.message}")
                e.printStackTrace()
                failure(e.message)
            }
        }
    }


    private fun getEncryptedDataFromDataStore(
        coroutineScope: CoroutineScope,
        context: Context,
        dataStoreSuccess: (DataStore) -> Unit,
        failure: (String?) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.Default) { // IO dispatcher is better for data operations

            // Try DataStore first
            try {
                val dataStore =
                    DataStoreManager().getDataStoreEncryptedInfo(context).first()

                if (dataStore.rsaEncryptedData.isNullOrEmpty()) {
                    Log.d("Encrypted DataStore", "DataStore empty")
                    failure("DataStore empty, getEncryptedDataFromDataStore()")
                } else {
                    Log.d("DataRetrieval", "Data retrieved from DataStore successfully")
                    dataStoreSuccess(dataStore)
                }
            } catch (e: Exception) {
                Log.e("DataRetrieval", "Error retrieving from DataStore: ${e.message}")
                e.printStackTrace()
                failure(e.message)

                // Try Firebase as a last resort after DataStore failure
                try {
                    FirebaseHelper().getEncryptedDataFirebase(
                        coroutineScope = coroutineScope,
                        context = context,
                        success = {},
                        failure = failure
                    )
                } catch (fbException: Exception) {
                    Log.e(
                        "DataRetrieval",
                        "Firebase fallback also failed: ${fbException.message}"
                    )
                    failure("Both DataStore and Firebase retrieval failed")
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
                /* jsonEncryptedData.getJSONObject(DATA).put(
                     ENCRYPTED_DATA,
                     "${NativeCloudflareData.encryptedPart1()}${NativeCloudflareData.encryptedPart2()}${BuildConfig.encryptedDataPart3}${BuildConfig.encryptedDataPart4}"

                 )*/
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