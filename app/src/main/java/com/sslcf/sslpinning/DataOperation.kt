package com.sslcf.sslpinning

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
import yap.utils.EncryptionUtils

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
    fun getEncryptedData(coroutineScope: CoroutineScope, context: Context) {
        coroutineScope.launch(Dispatchers.Default) {
            DataStoreManager().getForceFetchFirebase(context).catch { e ->
                e.printStackTrace()
            }.collect { isForceFirebaseFetch ->
                if (isForceFirebaseFetch == true) {
                    FirebaseHelper().getEncryptedDataFirebase(coroutineScope, context)
                } else {
                    DataStoreManager().getDataStoreEncryptedInfo(context).catch { e ->
                        e.printStackTrace()
                    }.collect { dataStore ->
                        if (dataStore.rsaEncryptedData.isNullOrEmpty())
                            FirebaseHelper().getEncryptedDataFirebase(coroutineScope, context)
                        else
                            buildEncryptedData(coroutineScope, dataStore)
                    }
                }
            }
        }
    }

    /**
     * Decide that all relevant data exists then create a JSONObject to insert the ENCRYPTED_DATA attribute required for the decryption process.
     * Make encryption private using encryption utilities
     * Call method to perform the actual encryption process
     * @param coroutineScope
     * @param dataStore
     */
    private fun buildEncryptedData(coroutineScope: CoroutineScope, dataStore: DataStore) {
        if (dataStore.rsaEncryptedData != null && dataStore.rsaPrivateKey != null && dataStore.passwordKey != null) {
            var jsonEncryptedData = JSONObject()
            try {
                jsonEncryptedData = JSONObject(dataStore.rsaEncryptedData!!)
                jsonEncryptedData.getJSONObject(DATA).put(
                    ENCRYPTED_DATA,
                    "95602103e50022d5122e77e4393581fde2f9da202456197d6d3e02687a448b0de14ec7413cd719eaaace5e04dce364177686432739f8571f531e0282be8cd242758b61a5965cd39c9089eb6684c9da1caa1100cdc4483ff207ed6f4e10005d20fe098a1f5ce5f8ccc3d5a628e0ae8cf7a3627fdd0142acf1157125c340d2285b79f97d899159ff63c2d8328c020b4e50d636efaf59a349da8f1316a9e932c4198bd64c56c09d40d0942bf9d1ff42f562b89c52c9683c69fad09a48b1051351f33d31736cca0a0f4d35c36d93dec122103e8df8681b0d725e110d4e47fc1d728e8aeb68cbcc6f27dc72eaa710c54c8107e69f424f63c172fa2c7a815ac0502037c1bb9fe35f8da1504aa1ed8583e061381e2d4b7b0d8f4a83fc9e56327f9192cdcb21b4e334caeb3e9a7d35e0d603429d78304d5f14a27496cdfa371f084eac5fa68b65897c595c63ac5f75e55e6810571e2eebdbc7bb122d1da3cad16d4318276738ff2b326de5627b343a77d549da0c773382b8bf07d84aec4d404a1353d5cd3b974f03ac03e332d442f8ecc3013e110d6ccc01c118c30329875be450fc42b8fbbe8ba0b35bc5e9632a8aeb6dc825b0d1b25cc8442cd058ea3b57c785b81df9e3371489bc1476d958b9b5ffb9f2a3942fd9bda04cc9eb97fb6e582f9c7d9f90ce0d2dcf3d53540c64f4a938fafa4e4026edc603b1e6d78f8ec51b612de0f6cd09b8b0e506410d6912fc4987eb25e5a1a158a1308fbed08f248db6c33e646954ad3f792008310ae0e4a47479f40692ba6162fca21ce445dfdd02b6cf7c7bedada622d34e4de07e241096d95c0e448319e3a57a4e5024cdb287cd54d7c04e7d98eaf7d56638fe2c8d6580599d5c7183d57580ed62289ec4adf1fcfe4efd0e053a865053e4e14cf5b644b1f9b319fa97f4b16956ab647d92b16c3ac64946a7b67c2fd19d6ccc56aa09105a09070ede310e55443d3148e789afa8449895a72eca81ae1432ab07cb4e31870028729a711a324a1536e18ec3c0c812bba5cb0615b938c064d6db9f8873593e9b1d14cf9c7c4b4c69b895cb2ca167150478252f61f876aa9787cf21594a6861d4380a4c2fba4bcc747b81b16ab6e552b28a1e666ff3f3de883035d869e1553984e02ae7550b515c0e7bcebaca81c0e1f11cdf715ad4159574dcb6c3b0aab2d8acb6c593024f27e3058019c65762ae353f4564a8e84073e7042ef0ab72670a3df88f0995865901e2c0d0331867a40bf90d8c36a885435724bc6fc641cfb2840a95e725d309d65d9706d550be79924f768da599157a01989463694e3830a31e74e3637f8c0aa541344ba9b355cb4b5fac27798d28a03c6477d0b5c20a53188d35a1ae600ad4f485141bedb3ef79151548a7d2573de2116b668ace8089b7ff0552cd2fb6c341c9663a4b22160bbf3e1f4ddf20d86732d99f8e3f51486a11afec98ce42cc24bf0228969078e754438d104f026552f32867f6b920d177e11bc4834e7195d42193449242aaa6228b9200c1dd1012a98bff7f364e04cccc5772e7aca1fbe64c3ce5764b76b3fef0257e14641aefdc1cd8699b52e3fb9dd9573c2a4f6af59a5194e2cd9ec119a8257a1eff9abab895435add516d87230dc0c0c8aaf85408d0a4de828e5e856433c68dcb94686e1cdb381ce4e6d0aa168a3b428e81baa4bc03edc3d09c6ea32f2c24931e43d56a66f88887d724a1fe57e8809ed490d143fc608699655d6b19ddfd27330297b13ecf5f4f899cdeed88f2e35ff616f12d2fd3a05874cc90e38ce1bcc01a915e0a2e86ca5309f9199ea4f62a9340c526db82b7e48d378d7286b6ac786b1b87a8d74fda98f73c9d15c5c5760c39088657febd6d6a725d87cc4d3f56d2bb919632851bc385ee38f401e08f1bf03069171d1c626442eec23fe1172ab8d5d8c3c841d211394dd3ab3f4e174e33cbf15c2cdc9c0f55cba63a3cccde91e170a893ad73d27e5f3e76091a513f57149cea6bc5bd327bf349eda416ab31cc031b84a04372986adcbfd6dab2d9ea6a5932724fff22254b08c538bfc535a8aeaaf190b0a1f66832ec601b74848f15f1bb90b2e74f28df79b5a94fcdbe8d855821c78f29052926193b220af73c1087044cd10e76eee280901023907d4cae959fd43e5d70c1f4a187e1c581bab5bdb0df748d5d45a318e1fda748749d332e61a1ee6e4f991599c127583bf75d62ac069d75dfb43f5d4f14806f5ff4a1db0973d4c06fe56a3fb5ac4399b491f29eb86d617753033e3fe3350118d341a336e9c260a42a1e8dad9532f1b090c004701590c154a8a1fa4bc2a5941ceb215c7fc5096e3e7af6499e790da5b01b4f7a67fde20eca3b2e28b1285ee80423cca1fd5bc2e3b1908b8775508ab8e73061b60d68e61012f126c4e2e6a91ed2b60b6692b4960a56bee5b796b6a6e93e2d83f2f100b1e43bc4ffe8bf660fc8f65c71ba4e9116942c82ba53ccc60d4337e2a8b83c7d4cfd05a9379c36fb069acefc01795c6a5b6b8afaa9cf0e9670256220615d5d6ddde8a5738b74f8fe5d8ad422f2b7d7acacd87a8ea1a9aed97825b385fbc5acca19a5aa7ea34683af79a5680c5e2da747179bc31ba7c7fcd727b91b42674e4fb05b3f57a29bd7b693e7649a9b2c1669d4e3d537826ff358252919c0e35a9b772a41e20e10dd7fc6263e1e0ae392ee8f9b0fa07a2b1e62fd16da2f41baf1d11840e5b89189bac74ec71527ab3c8be1a71b498a5b4bf3d35399afda3aa6c0836581249cae8a5e4c0ca982c6e437b9af885a4f1e925cd6dd3215001c05f88b8c07d707ac7f6a8c9829b5d7c81584343f446c75ad39d05d3803eaa56e532320f30bf8e03b2ec156d9099d466ba97f2ccf177bee46c9a3c6c0e728a34830731bb541d7a04c0cf4c84cd8d85c4d37d67371f385bec97d112743016e4b783a90cc937127dd1877d47833b7a2f49be39ba883e2800fa9933f6c3aa18decc29f8db070edef785b0c6269e222920f1ca9cf8fc481ae6310c3c1c8f70ece1769136990f067fa4d632a493dc6962ba3143c2140469bf813f95cc2d97b533dfa0eb62fe5100e86869df02d393e5346797fa8ac37bcbbe5a73af61ecded0068b1f3b87be9fb1ff9b0e1e94de1011a94e4d7b0a5fe923022da9dd16ff64b184c299d14f1ec0182f8f1ac0ac61bb04a021c59afad195515372ae2f60d7f09e09b00c843d07ec8ccd212ad53e928b0e635d52c4e244658a5eda50be47674b620d299100ddad0b1a58d9b6435ca3f6e8959545c55379e9d9159d8fcfffa806c6ac2a46d1e658e3d692320e74893f08d7dcb0e7c97c5d1edac5924233878ebda7de405a0abe4a26564b14dec799cb9b0390c0e1de89361bf71e2341ea271456cfea08768dc84eaab65863b0764fb699c6184ed566beb12064d7f1520cb4d757e3aebfe639a35ce4e294ef32363d70c273324773b05e47172deb8af05c9eb99aec9877be0135edfd4fb768443e70b94df4e42cde5745f78432466227620141ce08a02df8203413119ec9a2f64ba02cec96624b48e11296160339bfd83753ce97b71da00df2e7e652e05f3595d9ec2b68e3eb167634cb34ad831c18b9377c37e3a93d7f986b0951e0aa8ba29d20e72d0c6bf30700a2e6be4031393e13306da21a578233f86d3a11e1ae4021f99a1544319ebfaf942b5c3e5a8bc8ac3c44bb31af10bc48eb9286d4a372c8e0a9aae3f4d61fff75afa899631eccc01a8b55d618602728eaf1adc4b4e156a6651fd1969f067726d9696c147f81f24d046df4d06b74e55661cd4ff6fb9da6dc775fdb553ad791a38bbdbfefda835a81272bd4da7f21f58e786720d8fc6d9ba4d389a37c65cf736aeb745759f90084a5334681d3baea9c362c1cd1508f9bcecd8aa0d848144342457581a32fb95ab7167be421550c1adf12c6d46e6388a25d994e8ee948fc6c6327742cd00319a859ff4b73024f1437b4bee65bd18ac1f9ef4fb24367edc1ea80421899f18a959bb060f70c81e1db64d4735ad69c38887b206a777925a4c7e7bbdf2a34e88e23d2887e10f99285260c41d75922b21cfb04015d34923c60564ec06ae89b5e95d26d72415ce445f382fdf787cb48465c282a712789d91abac6b64b6cd798ee85152bca9df68ad2d4890d88ed59d59fc98135f87e570dbbe75148d11ad32c8750572029cde8e682794548c1f65a820326f299f673008578c154e0730f56b1edd095b99e39427ffaac9745133494966e182990ade453414d0a3a3118d3760e691cefec1941e0c10b5d955dc8419e78ed276327e626c891b324ef53eb9051d9d0bee0a132adbdaadefd27cf9f43234b55f0f74e0e4f9f4e6baa06ebe1f5fb0cd38810b4f62e2633688be90c2364c39ec15cf5edb21c37d765898e7b683c5acf3ebd28e9f4c78b615484b43e4d79702e4e96fa00e21249ea973e3ba832330e8324cc64612a33d3fe953129f46d006ab40bf136671b49f6c3642a22d9afd5f663d17e8cbf4855b67d96786f87ec0c6983c66c07c281a39313c992d32a1020e3de53cbd669bf090229a0710a3c5ea1311a30680881fa67772f39bee7411463816f6fa36431966da5822e75a9f56736f97bdd0339a5b29c9bc41f105fec69623522fdbbdb1936bc11aa6aab8f62e604220d88ce750b2d3e048b3ff1f63cfd89b5d1fbb65dacc8bc176be185c6e63ac4b55e9cc105b785a9233eddb6d2eb559a6e0df255217f60532967bd616d9f3c1a3c0d2edb7ada5c64a340d6b127051fa176171d97a6c6ca72583bdcb0a14ce24cf2c5c13ff1db06ffa8d4aa0e57c367194996672218d3b20bbcada3e4f98e1494f96594ef8075ab7e4760bef3bb7db06f3250ce95f8726bdba3b09427b24fa085df04a743f05af6b4755138d97940c5bd7e3b73906194582d55ec8eb7ffddcaf57252bb0ce863e42c3da34ba1cd1809254b9d75151725de35a8d3daed0bf01750b2beec8f621b4b0cad7e652fc6a204de30a57a8e1dc9cc8b03b4eac342cda9ef2ddeb79c90d4f7229b2"
                )

            } catch (e: JSONException) {
                Log.e("JSONException", "ex ${e.message}")
            } catch (e: Exception) {
                Log.e("Exception", "ex ${e.message}")
            }
            /**
             * TODO
             * What will happen JSONObject throw exception?
             */

            val privateKey =
                EncryptionUtils.loadDecryptionKey(dataStore.rsaPrivateKey!!.byteInputStream())

            DataDecryption().decryptAsymmetric(
                coroutineScope,
                dataStore.passwordKey!!,
                jsonEncryptedData.toString(),
                privateKey
            )
        }
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