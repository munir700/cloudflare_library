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
import java.io.InputStream

class FirebaseOperation {

    fun getEncryptedData(coroutineScope: CoroutineScope, context: Context) {
        coroutineScope.launch(Dispatchers.IO) {
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
            val jsonEncryptedData = JSONObject(dataStore.rsaEncryptedData!!).getJSONObject(DATA)
            try {
                jsonEncryptedData.apply {
                    put(
                        ENCRYPTED_DATA,
                        "076ee9ca13c9aff516669f55727a49c8330b82d5280834104b06a8fb7653faad1df141ffa99d8a1deca3aa32b5592e6395ac53b60b8ebadca6957b00ebd3e98f9d3b3dbfb0c5012a548718d7043c35a7812637ba261ceb881d3eb7ae65e2f5882761ba9ad59a87d3ae644aa65a66303039feddeb7e60f34255f8ec36d3c2f9b8c13c9a47773bf06c04323ca019676487e1bb5519f8d3274dce8d52e1c297519aba06cd15710833cd7ae8f4bdc3094e19dddfb12527fa452d56303eedeb3d7abd041ba952dee0a23dc3fca38c3e1d2d1de4ff46894c1c01e50093f18e9d42080c6329830d9c217f1e4bbc93e31ce7be0bec6656be7ebd4e8ee052477c1825907b315537f9f26fbbf2b93d2c75d96d0579b8f616aa12ff30cf12af95539e2e10dd9d401be3d770e5eeed54afad360cf1560931d5a5fdab0b9b872ca4602a05d9a925a1e8c4078602c9a11e98e3bd6358f532085ad01b706fd4e4345b8b1f6995303079b7f7fda9cdcf70db9115d216dc987cc992bd46de26c971cf664be9ce24133dd3712b510b8eb002a5e8d11d3fccede18503a8348479687a601b9f8de97f898b644fbf09bd5c078101879e435da9dd60ebb08b598fea650b48388926b2d54697dba8489922f0421f92f0d959d1986b5c65ef3002e21dfc17fd59014a673236ab7fe908481d41cf31723390bafaeb0b6b5167874214f192126a8288571e35cfe3f71fffbbf47fd224009e9d7d2ad988867b2fc9567ab14e85776b583fcfc1b65046e0a38f62089451ca2c8348f316c686f02ab550a935f930c2839d406f6075ea01b15f6f5259572d3863f8958788330ab86576f9d40a02ffb5c3e86d23f320f1556cbeb85fd615afc3d98b515ef631b2b8f83e67b7bdced6026309d83fcec9bb99d2da421e18bda7410f09b7b7b6368e3f2e140b9e04af9f67022703557b76cdfc3969697affd82720417c0dd644bc6175fb0b82e562673531d5397db53806020ebe748536517ec6df81f50ed1480e5a8fb61f65f71fc3af1244b3fe692b4ffe057ea08da34bfea5e45a0fe7b1aad79cdeea41e6eaeaa524ef05b3644ba67aa34b15f83294747dd225a63a7bf0aa72fd0cad12f6a7fbf9aee84279d3c9403893e3b346caa1479432b4ae4f6b7c88fbfa044f8b561fc4d27cfb2b0caded8484d72767d3d148630aacce5d8124e1fe52410a7b73b47afad539bab0f83018d4bccfd30f12a2689a18d765ae18f8f300308d95e655f15dc584d2cedd708ff3383e80ec62c540002319bedf6b6adfda6b812ecd1b5b1bd0191621f839b3d991b76f724cd3c4151dffbbe13c06384294538ce7b2b7afcd2d7aba583cbc6a68f8de2e505d2b1dbcf6577d3225f20d66d08d6d8908fb7d567bb02c8dcf1f21d31d4e0ba4b363307c463410e6584ac0d1200c441c071da77455baff7ff9f06b3fd3abcb5e0bb6b5f47ce850c1b2e57ab59b69130fb04560732d9a2c58c97ae512b91e5eb2b6c76b573b34ff627742281156d40133fc8c06a94428a2db96c756b6ad0f0a718d019e4e0ffd386fdb0c7b392f3f7125bbe3f33cd518b1460a671ba0cfd0eb6d715669f2068d31d548e541277c3ea3dda566f90e3b3bef6b37cdd07f6cce9083bb5c7695db7d138b2237f686060d5de6154a60cd171e1e43c14f6c01e5070f3caf619c48e2238c4867a9c9737b5f8e7132c3e9f91aeacdcd9af962c466eaba27e7091c0c51064252e8125f4f904868cb14d535791df1b6dd7ca317afef69a18bdfae335272104a1d8d7252be9e541b03c3173bca59b13173d92cbbde4275864d221c5164ee466be04626e5a293982844e9fc962ea8877283e4b9129e08ba26231bdc46ee813dc33d7ab0a7f21b744d08f40f9b7f11f58b5e047b9e3cf9c4500bd60497b171d29cec49dc688a27473911483b380f587dbee98eb05855d2ce2a38fbcef65031ee864e4457130ab5b1919ee0107a30ff40fd0f42925bdaba62d991b0d301fdc13e367aa1c35036e09672938535528f2a28811dabc2c953283646076f9de16fd2ff27f32fec0753dee6014a2090605733de89319acb3102527a2371c8d7a53c42e666103b533ee3c26107afb9d9117e3b876cbb70315824dc471fc2bb0aa211748f0825a2e7438f693cc3b2bb6c73c0a98648240bc6ce86c24573901b730a5797f051dac09ac2a09b6a4e7cfefdd58ddcb2ba9930735d03767b801ad59f1165047fe1e16a0bfb4e9d15f02fe63b3089bf0b776c123fe7710d1a158aa2ded190850e1ce0451ba55eebb7ab36fad1013ec89fc552792c4a79f1f21d160c4e68cb3e128ac3535deafe372bca5ecd34a6ef94bf8c4a3c341094c1dd2bab0ee81d1051033fec6c663a6583261413626f10ab97f94129bbdaa4357d193645b2c3ad0c3d582c85ab25d3178baa244fae62d448382214aa5ba610daba7ae44b889ca29b9bcf0b5f30ed1d53629eda03392a3e823ce17b6fc6be40239e30c3d6c179f43f2975bd4270624bf8a70ff38a524d5033c0e163ec73033f4bb2c30ddb7461e855986600441b891f4857c1a557fd609df79935405bc47ee1ec848ee21f35ce0f7707dd69d55ecc12115aecbfee22ce64d520a4f647dc3bd8545c819ca8f4ed435ac9d9b67edee2fbde385a4c811250e0481dc293ff596cf7bae721bc24c528e6e2e3eb14cb64f7b9bcc7a57185fed4cdfc9f160ffb338eba60fc214da714fbf4f6ed797a995b8119b5f1cef445ff623e0b42d58b3ee8d1485aa87114c7d02acd7ecef1c2c91571ee54eba36b6200a83a8c1a4075979319f58aaf3f3fa77b11bd65a90620b0adb348700f71aa309daf9ad09e5d170a020ae6b51dc2a3d15901b6ee45451bba8e2c0e505ddaec9d5bd145f00a97db30ddb9c88e09b2424a0c44b97e078d63c66d577bd8e1517417059046858510b78a46e4bd3779ee56fe4f508f50a83af3c9b9e9246affcecca7abbfe9f75afbf1bac9ef89677743db78cd9a7e04e7311bc162d47d8e5d62f64d8c705b640f2450641230669d008ca8d672607c38332b85fe2a865bd56d57568844a7a47bf6e49ab4e26050ccbcd5880ffdd9f39b8c2d752250231b019f7147e7ac624807ab90619990338d45c7c447d5456dd3f0addb22a55bff6d0a14a6e436703547d2ea424f8be6ac6be34c470ca30ec7a24d630d4d46e645d1b8529c265e3601a734a17c2eca7e6b101e4c9ccdb3502e1d054358f3e77a8e0105db5353da389d697853c6df84dfcdf40b34a6aa28163175331a7c561839261363aaddca2d4e4e5ed80e50cb50f1556c70908b5f780fa4ba39300d8d5aa10c9567a02cf303afd07a30caabbf416932afe663ee391e9c8ce0e8d1c5d64908120db53adfe1d2e9ae11dc57e618b599b9fbebeda8658b5cbe014c45a036d080b2097fc7f9c1629e51cb7490764ebd8927f8546d0bdbcd19f958daf33a6bc995b9dcd4822f8fd929e21dc5da422fa75e3a4232b0968aeb5078fa8c4e6453df7fbc63d673370a89fe9ae1964088fffcb3bf59bfc3a8e600b4cf09d3724912becf91792f939d9bb5962137287b99cc3978f207aad03e23a533083a4ad2c847e88d4630a2de9b68c4c4df70ab4ce06b95edc3db54f3b98b0c2f429f42481908127a81ad49495bc8718cdedf739f5fe11634ad86e8b6f0b8ec20bf4735aee92b2e109de1c60429431e73f72b4048448c7ec7d4c0dad0905a0cf8a71f021c79b3f40f9e3858b4bed05ac4198025f23fed8f30462a52acd5ad98112c22fbb056521592b90947b1912c387a4e8730b6316e9f4ea7539eee4a01ae57f6dc155e08af7428b0efd1890a97f28b76197e0057fc23418e069e61e0500f573e975ee3e48a14ee98382c48601e5cf8dd0599339b34c8e65ded5e40e6e3c76fefa633e471d589be7e314c28c9f27a9fe1f3b76bca680dfec973b77f7bf2549f9f1caf1fb559ec5c440587cc8dcd797751fea42b40638d55c7629c6a368a28f7568f943d683461e675a275fa8af139eb7493ab13e94ebba54713c50f7ac5a1cd65805cb4e4f845bd79f7955a3d3c953cee6b49728aa577e7ee2b7769e58faad48f94bd0a93b905ac1b10d0e75a1546208e9c035658b43851b3ada91db8786e708c4a4b4d06e285920adc94a8522de3c4df410f5c1c9d1b2eb94ebd87cc1b2351d6b5c8ccd48ab7e5b9b4c4c31a021975add90561343894e28ccb5342cf49e5a6b8549662d4aac91daeb76f1be3cb267bb18d5e7bf08c9c0ec32c3f85f2bfb6677ae737fa787dafb83cf35d7798347a300cc4ed0f35f5a1ff9444a6e26a0ab7d70751d2d2fd65ff55533ff5e20972ac74aaeb261838efd6558e1a2348d04c66305f9361caad7ed8c1f6fbeba2bb73be1e74f7fadd2059fb88d624991ac1e2a66ff27ead7c556ee1bdbac6e4a992904affca32a7b4ea9ff23cf399e54867b1a9711501b7ff68dacc23f57f156565ff90799306310978b4643d7e45be275f182fa0e271f1c703196e191ea85d1f68f83a0f90176e7822f24e85381f6dbf7c66706ec820e0f85cc0bd47319f875416c315d501d664bfeb06a3acf56c371b91d0606dca5d64774716a6ceb65e547347ab4ebe3edf6c1bde5a810fcadc4aaac094b22c1da76215565597367e6d282b8fa5c048f27e301aecb1f1179b91f77f04bed07e311cfdd10620326541cdb8482a3a38534b702d000a12920b97f389a649bb6ee5580be34b4758781b9f8935f89066aff209d444c8aed2b4c54bb9b5d63988b4130498fb425b7dbb9eb99b5b75a7f4e39a697c5998a3d39672cb3754368225f7f6a7e0e07ca7c5d3a42ee850c62e2062c97efcf98a052f5854cd3b2a26156b6e1b7a7e6e55d0dd74e996f534c4461aeef42e7ef1ab643cff9615f865bf33d77445b1e853fa8b8c6c439b8368cbbee1e225e91e73260251b37d54e46b36794fb241a7d782a5034d86b03f54bee07fb2c36d2964f1b99d049205bcba370912a06cd3555f425b2abefb7c18e1b7eb4724035f44e1db6836623e8ae14a18058e6d"
                    )
                }
            } catch (e: JSONException) {
                Log.e("JSONException","ex ${e.message}")
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


    fun test(coroutineScope: CoroutineScope, privateInputStream: InputStream, data: String) {

        val privateKey =
            EncryptionUtils.loadDecryptionKey(privateInputStream)

        DataDecryption().decryptAsymmetric(
            coroutineScope,
            "pdqxnxEE2U3hPCsKk/nFhA==",
            data,
            privateKey
        )
    }

    private fun setEncryptedData(coroutineScope: CoroutineScope, context: Context) {

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