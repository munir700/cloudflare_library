package com.sslcf.sslpinning

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import datastorelibrary.DataStoreManager
import kotlinx.coroutines.CoroutineScope

/**
 * Interact with firebase to store and retrieve data
 * On Successful Encryption encrypted data along with password and private key send to Firebase for storage
 */
class FirebaseHelper {
    /***
     * Save encrypted data to firebase along with private key and password key
     * Private key will use to decrypt data
     * Password key will use to build KeyStore
     * @param passwordKey
     * @param rsaEncryptedData
     * @param rsaPrivateKey
     * The saved data will fetch when decryption required
     */
    fun setFirebaseDatabase(
        passwordKey: String,
        rsaEncryptedData: String,
        rsaPrivateKey: String
    ) {
        Firebase.database.apply {
            this.reference.removeValue()
            this.getReference(DataStoreManager.PASS_WORD_KEY).setValue(passwordKey)
            this.getReference(DataStoreManager.RSA_ENCRYPTED_DATA).setValue(rsaEncryptedData)
            this.getReference(DataStoreManager.RSA_PRIVATE_KEY).setValue(rsaPrivateKey)
        }
    }

    /**
     * Fetch data from Firebase
     *
     * @param coroutineScope
     * @param context
     */

    fun getEncryptedDataFirebase(coroutineScope: CoroutineScope, context: Context) {
        Log.w("setEncryptedData", "called")

        // Read from the database
        Firebase.database.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                DataOperation().putEncryptedDataDataStore(coroutineScope, context, dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FirebaseExecution", "Failed to read value.", error.toException())
            }
        })
    }


}