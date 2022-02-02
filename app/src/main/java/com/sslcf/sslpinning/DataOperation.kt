package com.sslcf.sslpinning

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import datastore.DataStore
import datastore.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import yap.sslpinning.*
import yap.utils.EncryptionUtils

class DataOperation {

    fun getEncryptedData(coroutineScope: CoroutineScope, context: Context) {
        coroutineScope.launch(Dispatchers.Default) {
            DataStoreManager().getForceFirebaseFetch(context).catch { e ->
                e.printStackTrace()
            }.collect { isForceFirebaseFetch ->
                if (isForceFirebaseFetch == true) {
                    setEncryptedData(coroutineScope, context)
                } else {
                    DataStoreManager().getDatsStoreInfo(context).catch { e ->
                        e.printStackTrace()
                    }.collect { dataStore ->
                        if (dataStore.rsaEncryptedData.isNullOrEmpty())
                            setEncryptedData(coroutineScope, context)
                        else
                            buildEncryptedData(coroutineScope, dataStore)
                    }
                }
            }
        }
    }

    private fun buildEncryptedData(coroutineScope: CoroutineScope, dataStore: DataStore) {
        if (dataStore.rsaEncryptedData != null && dataStore.rsaPrivateKey != null && dataStore.passwordKey != null) {
            var jsonEncryptedData = JSONObject()
            try {
                jsonEncryptedData = JSONObject(dataStore.rsaEncryptedData!!)
                jsonEncryptedData.getJSONObject(DATA).put(
                    ENCRYPTED_DATA,
                    "5619ceb1ebe98354d2195fa9c150076fcba83067821458686846698e0e5a3fc0f51e3e9bf777bac9147c71230cc02e9ab6be0a3f051e0813fc20329276b9a582689c911bf8a4d287bf0056e1012908c199daf06ef494a9d9e780245b288574005dad518e6cc0becccba65496891b9ddcffac8098cbc408ff6ca75af46069242d558182e6a088b06d6f6cd7ca497b501763fb52906938ef492a82c84ed4a61564311783db3d6d8e9df96df96120b29ea454275e8b6bce75e3e4ef1e03332d8e71d8f5822a59e79ca7ad90fa543ff5857bd6420058b90aa702bbbd55abb6f79e83f54fd32ce2a150d33f97f1e77e282597e4b55cfb9c048b8e792a2254591967df93e1cfbe3c528222d6ae71608cecc82d4e4f055353ce1a7b949b0370238ac1b02fc5022ca4553db82fd4713225956e64bd74e065b7a3f67f448d112703dd1b23a83fdb736c1c4ee2d5b7740af0c21240ac21cc5f7513fc520e7247c14ed862d984da4daa07fb0b991ebd63ea637ccb04bd9d21954515109aab551ac3035f6ce92920aa4d6c15db269c7b3a5716a064bbe8c0e408d4ee92ac31e79ac0d58d3583daf0f286e6a40564f4b26cc130451b4f6e94f2a79c7a68fbdf9f1f7e6716ef5ba38ff625484db8bd5e32369ef6d72ac36f630a23a9c6f2f2534471e9447cea8a542e5dd1d121eeb74e40733058179bb7db1ae2fe9efc0e660e11e0638d10618036d8b05072de58bb7f20fe6bca4cc6cf9511357292a031c28587f7fafb6553a510688e7a81c3767fd73f20978bf657561e83548032a6893a6f9c6d1e5d86e333967885034e8280884d0b0b8b0a49180e5846bbbe50b42b36f495c5ce942673b7acfc219efe35fd077cc75968770f98b76fc1527ffe71b9f8cc230bc1e2e8fbc4fcc0fcf6f02af44caaa629a5cd71ac89e2ea3d8efafefee579c8515afc055a0471ef28adc96022120ae47e9d92594ea75b7c4aca83a180d3f44728df82cb4ef97a9f1870c1884408f14316a610361d1e1a461a9a5edc175fb208694f72324c09d622dfa5604dfbfea487890d39e0554c8a0aeebc98aadf5b1cef27223e90d45992dfadf0bd404bc8e6ea44b8d6a46e947322613b124d8ccaa223bff413487ded61b230b11d7181adb748037bdfdc194553721f2b9bf3565ebe85a5f21e174d976098b7f26770be837d79700955170662ced27d312f4bc82b1c4455c9b29fc28d63ab7253324a53af6b30e4274285d6afecdd08f163820f3ff87e9b20392f808744c95e93b7fd5c5272fdea74c75be360bc78f39d288687d72beed0b66adcde61450b59e691fe2d4d1c622994133b249df2f273f0a72bc2561f166f7f244d7710e329dfe3d1c12f8d4af135d634a20f51b6c6bf4a542138d450c4f99875042c06b5511bf253897bf8b542bcd5bf7a70446ba708d40ce417cd53ccf358f7658a261e00d686ae46c3d9549d7eafb07fcf93194ffd372e63f23fb32d5bc188e96280b980738787c4c608eb9e62ffb3eacfc4edbdce56e9b67d0157debfacdd595d18af43759b0fc35d698209f8e50700437314e598d4c7570a9fc07925407bc4c2b0985a8cc7efaec9520e40c50fec97e4d89f480803d423df0c49a3719a39567003adc3d7617c6f49a0f37a76ea7ee9090de1baf65df990230ac6ac323cb31caebc7f701d8bca8f38d2f0748f899a72f73f6b5ee09ed951b7be253c516592199acf7bf58eed5e58457d9fd7347b75ca672924dc1b4ee8b99cd530df07a649b755e2cee9952482376b7e5ea53c71bfa44e873ebea4dd2dfad30403e34061897603018bda00e2f69b76d50525b17e6e56b57157f27dbe706177aec2d4b9ff2b6f575a47d3db927f455196e3066e65e58cb9401f26e9c95b385718b4b30b9454ddbb8faef4ce180e812cbe3d540063e65bfe04ae66e5c94ca040d177fbb737ccccbdd5c66a5eaf8345507c99316762f6ee4b86ae107c8e969e6aa835fa7e2cffd1bea43ac2484c031329e2025ff492afef536a74bc17c326f17c66cdc2f54f0c75bd7e59497db3a6884ede001891505255bf83f7a859184e60032a8332a14dc31ad5a810c57320c00577d364152bbda2587a8e73a8feaf462eb7bb4246819b8ba0c489bbb8c3468f9760af8f2763d95b58fd51b3407ced19a3d7ad77e6ad7f026e7899dfb827bb173af0888fad501fc92ed39d6b2bc9d12b74c3043a75ec13219f102488ec8fa6f4320b9f37c205a21ceef05a70b173998e54341b1909b62bd3df764eaee8641d99a582426bffd855d02a64abe0459361f62fa2ff18a3a93dfe1a2ddb0e7bc835b3cb02a4bc821edc3929f41cb11a8f89f1ad20bde3dbb166d2b7f76b638311f4956cdcb40ef2a063a61dfb59dacb7faa95f0b8b2a843209367deff8ff5789233e4a8ce6fff88bfd74322b57c485caae22def17f6fc253f54cf056487f7113c8d644bcb7c1bde8914b4eb9ae96cea8ff846b1fcd59ba0b90f846968a06c1bb64b4a6f7422c30b7b76a9eb39400760c64fed767912a94801882fa3cd675f19347f8afc3f4ec937504a1b8f911e325bbbea6f76d194780a57ea1363ad261cc4592c1496c3032b86c8725231a58a498103e3b1f038c111cd2ffac4cb44ed8463138bcc931765d5cbbf11e88b850b2722defc83ee725285e2b24f26266b49fa974c8501c0bbb8b5cdd2bacf286254edf9a42f99da108a8b4949466fdf9c6776ae33d1fb9c23611ea2994622b800b897a717c8cf448636de4a9dae8a70332fe379c4efcc8bb9c68140c5fb44cf29073b34fff26b18875579e23064cc230e49744171f46eefa8bea50c728ad480bf4a6c708d03e22aed3b9949b06e0461eae1ba2a79d247e8195b8c0f011e2b3cc5dfe2c6244247666b348094b1f918a9465d400dc2ecd09906953f2ff7849a836b635c5ab6677e5753025a634bd3edac4a69e32eae67dcc089c6cd61190afb2ca55a480234fd8e555e354e32bb1e2fe166447e5b14a691f9c80bfea2e5a9709122d2eefb10b8fa2cc36fc5533ee5eb32d3c310e548fda681b40a23b2c2d53d195109af77f6cc9248858cf90231105b7af33a424fcbe2bbd04789685616b6b85866713a44faff749550e9b18d3c74e10601eba8b92d34a6e7ed059d9769b05fac399d653d97442fa535964d5a9becc826afc97703a14285f7e4b57b574c4a7af648cc8a4b0bf924525d199a1fa9c3f67365cc72c1b235fe29d97bce13d9f3f00c6de404e0fbedd17e49f66d8bd9b4d483673f8ef2ae79eb73b589a0a682d24d1858210731c75cfbe2fa6051cef0f1eb4348ea00dbf2ed428acffd22ad7de4f1e62997507105b2b77130d9f5255bc9c57920b9c8e32b8160364be2c90329e32db62fac4a4ab51a35ad275d7c0b8a7b33aa27aa5d01b4da4f3f87a7b3b1605b09ad7576d2654f020e1610e6ebd586dbffa7bc384bfc76d9a61b5beaff13bd901f8d18a79bd60bf68766e72dd9d7d79022e3819e008027553ab1cd4cd006ed511886e043240ee289dab3bc5cc698899a46ff6df82270316e77c72617b292ab6cd2875f109695cc5fe75992ae4bf6222edfba4a1f5a809db66924edea3bbe98c808077ae381c98658dd69440bf265dc3128ab7d8f6895a462bca8226a6730d29869645e50f3d6495453c252c7c78373af2b04002945ae3d01290b1142f5491965fe054b531ce6fda42b9d50a8a700bc477519cb3a07c369a47634505ca943e252cb647547ce7a7b78f86e5f4cb906a5120857e3dbba79bee31c8e7f37e052978c52934d63b8d093a8985150091412261c2fa0a8e9cd5e3f946f9427ba998c6fe9ff417b688d80a0ba38bc9dc35225e06fc305aa0957b5ad2b1dc1391c8db9715b3160add243487e07ffb1f09cccb2690f88979da570c5b4d2bdc230654033cd3384c451c673af94819e81e440efef93b12561e15ba34f40b6e6e6e70f12a0d499b062fa068fbead1959ac0d8eebb192805dd6618e48a8f450c466128a202d5517159f37702999cdd7b4d3eae7692efcc0ced2cd6161677ba46220e01a3172cbecc990820cfb67845e23eff5aaeba8e1a8d6598a6510511399ebe55a42ef4ab221f45bd27765ec7e915798d9a42a509bb0ed326b4619a4d4122ee956fd3ed8fd4d3fcf17f72a53284f6306c756609cddec1e7ff6bf0c7ba77feef0c16c3876c8d95ee8e437f3e09ab4643140432b1c6a1f2649f3d514d766f1dd47838291bb7fa61fba89982ac05e9cfe6764770e7c4bf7a8b6a3ffcda9eea745f6449eb5f2a659c78d5e8aee4f0b446957db02ee712219d88edfbef2b10d329fb3c805ecddd4ab421618b3bff61420e712f911edb6ff9d3ab0185d61649510ca56571beecccb89ccc2de01ff742bc876a7fde04791ddb932c60d7bbfd142830d735b16e61a8e62681b833c212755fbcd74e4f4959dd39bd82e8e21f0b40823e43b80cc1751eb80111172dff00ea0c8b14a3c44d10a75e8c5f905e60cdff9940554f57458d0dd2dcfc2966573ba607c667d950c3a1667785f190274cc708effea2a37e45d597ead6859386d72e551c53ee771d802929c316f951ee853b09e8212e697dcb55b4593c3d5fbd068d404daddc9bc971df785d3f9bbcdf7eba0a5629aac1aba84fbcccd2a46d0687ffcce6827f1e041098be428a77209e65d8c305b39ee2efc83a4c3a95116cd6818d7bef59249093784277d7ac0776b7c3768e7b95e3bf592295a2d1a4c0da9b3e3768dc81fd916a83418f6d2d07030e4dbf652ad7d9f005742d7ad64593a59dc64cae61490aef9720cf37f0fe2cf12a6cb27ce23fc3faf5d6819979df0e28963b4c372dd245336a587b88d30baba9343911841a43881e6547406712d505f66b2cdf722ee21286c529c2b92410e65d9455cda8b7512535668e7641149e4813e4a9f2a1d12069c5e8f0d3a64981f10127d3f22258eac93f8217eb631b12a73431b3b76ce2217f5e3b08e8377a3ad515a43ed8f4acce907de6c2aea1bed92ecaa00206a428cca679e6c91a8860e89c19b5f"
                )

            } catch (e: JSONException) {
                Log.e("JSONException", "ex ${e.message}")
            } catch (e: Exception) {
                Log.e("Exception", "ex ${e.message}")
            }

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


    private fun setEncryptedData(coroutineScope: CoroutineScope, context: Context) {
        Log.w("setEncryptedData", "called")

        // Read from the database
        Firebase.database.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                coroutineScope.launch(Dispatchers.IO) {
                    DataStoreManager().saveSecureEncodedInfo(
                        context,
                        (dataSnapshot.value as HashMap<String, String>)[DataStoreManager.PASS_WORD_KEY],
                        (dataSnapshot.value as HashMap<String, String>)[DataStoreManager.RSA_ENCRYPTED_DATA],
                        (dataSnapshot.value as HashMap<String, String>)[DataStoreManager.RSA_PRIVATE_KEY]
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FirebaseExecution", "Failed to read value.", error.toException())
            }
        })
    }
}