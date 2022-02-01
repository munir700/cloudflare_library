package com.sslcf

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
import org.json.JSONObject
import yap.utils.EncryptionUtils

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
            val jsonEncryptedData =
                JSONObject(dataStore.rsaEncryptedData!!).apply {
                    this.getJSONObject("cloudflareEncryptedData").put(
                        "encryptedData",
                        "67157672f5be6d22600ec5b78d4a4601304bb8e834b7bde9e76f9c916575570798367d25365978eb26b689a5bc1f6b63032ab99a739b4d0bcb524f039d3e7fdcf196f74f38aad82d85b11836e5a2a8207236997259628c3ca20166412b3038f11b2b458feec2710c66ac82183df4b79b78fd27fe7ee969dbd2ebcc3a942092398207b486a5c6e55410e4654723e6246d6c9f6018731a886098be5238d86834faf01d666fbfcd1d28f4ac893f80178ceb6b3493386416efe2b427f74e702e3dc75788eada6986526e87ec49fe7d775898795f84af34f151b665cdb13dba5585f45f45010c28236bc3369ee70bd33e45b380c61b7691147484a0962a5dd6500bd3e4acff654f6b2139430953e2687d29c11bc8d11c84f4977435993486d6bb930fc64beb28f0e1da2f1843d0d489bc860be842b30ff1dc10349edb3aeec43540ef0104de84d016e084f5aaa6acd97467a8107ed7a97638ce045abbf5b3342b051e86c1892eda5c1d16500fb30ade4b4355e28cc5ab8ac1c47863f09422d03dbb32801ca86c0311da5817addf55a045c0cf4bdfd481929a32c61627841797d491ed18da074dc4dfc0ca69082e5d1c7e3343a9785bcc651e6542506f01fba1c50c938e82eabd27c86773fb3451eb17ee6943fa722dccfe42bd4ac2131484be8635e9ae97168bcf2696de5ac3b270e944d250082c6bce79857524758b11b5b3a1ed6413a3fa56c0e9f3edd825f2b77c10bc8e95d447e3b348a10fd220c3f617baa4a61911e5c58ffafbfe2496d03e55426021adcdadfecbbfe649c7162da510dca09b54a0116d956c8cebb1351492c117d622c0b704ca716c572e0a09d5877203b2296d9feee8032e34e6d9efbb6cc97a216b310be3eb99d0322063960d3405a189e42dfa3c479545535292feac3074759bcd306927895191a753edea6b91e6ec6360973bda31ee240024437c88d65464a191749f52232bce282830907bb7aac58e43aef365fd358bc260430cce8b2036e6bb6461ce93e99a604a94524f5ad50ef18071123af3901d7ae8d08d2cb0ff03e89817e2eadc6f898f0f8459e0f85b1e0d5e199400ede2032a8ae3b75365f4337af233a6bcfdc20f2657daa48f653fbdb91c605bbde4cdf92f5d25151c615009af773d5134e6b7fa6068a02507fe260966533122848b24ede38e2f73660c2689e46dc6d4829f32d68290a8c7180d78263dcc3b663fcab392da54fc7ce8bf9735c90a1c33da52337f0305cd4ce1f7e3c5b63639c19cb58f66cce5d0cd4aa07b864b3a4b672c760a1da16bd4d383c9ba78b909ba474d6e0b25810b8cdaacaf9eead155a6eda5fcdc78f37027cc88cc1703a5eb1b85916d3f18498654c900ac2720d1c782b929dbb6e21e9a7c217b2cac494f77035efd7e591e4b494633a9299a2dc43661a2ee43f131ffb9548e0c5ada2634794cb13b9a7373194178b1d5182554fe4696c036082a6cdf3f20f4bd0e1e92217845c46992158d94d77bc085e339fa83f919e8202d852732aa592038e0cabf4fb7fc469f8e611296369be47fa09766845e45855aab4df0209605e3c0bce38cf6644e3789c12e48fe07ffdf56abdc43cea9220b0007fea1f4f63fb7368820bedc2b1b42ea10f5ff14011ba25e2740df813b400cbe4bf7ef7e34844544c32fed58c2de546dd502299caf43f8f455d006b00e55bab026525456760b677464a6827393bd4a66081c4e4f78826e88d04716b0133842691d7a3a100e9902c99253b762596515941eb942e76127f5093b403d1187df5156d8c7ff5a439ff1b890610b6916dbab11c90ce1b280f4fd49dcf030ff31793064460ac6a5ca979a3a5b8aa5a6b16511ea52cedd489daed986f7c5cc83940eb821da6ed41d3c5832799bbcde36be5e717146605ed0033b1e14ea022c29b05ce4a3dbbd5f45d2ab7ab65ccd82f2e3c73ad9ca615ef143d2a101bbdd99400a14cfb2ff9be0c9ff13f4e4bf050d088d7ad3e7c9d1e255759afdbea77762d1cc6505b5782b09692ebfe62c9382312d999fa7da3b8f09b77d96f1e1042a8e6c851b5415e77695fe6b0e37b21f0275d06c0a3668f34bae86c9dd0b0d71bcf5af204a8f26eb0ecd2e05c9060307b7b3518571d07804e6a6e37a49e7678482c25d5dc49c527a61c079fcb7ced4b537b543138ca3d6686fb97d6a14f83c83236b651ffcf9e40958759c24527323dab3f4f6e67d1a75f5636c5b9cee98150e2b7e1fbfd869a391e80d266bfda7c45cbf648cddd7be616d9446181cbc7fb3c6341641e3446a757d81ce363a8815352f0e12afdf83e87624de42874b8ccc680a28c596012658e3e2f2cf25246f7d29ead4de72fecd9e05bbe2b7879d120a0990e830e59fb37d2e241f2838be145d2a1c778bc83a8d4e47fc23c8c6984a2eb7838aadd957fdf37c00f884e23e43c01ccfc965a671e8c6034b8df4f9ae2de1d7b92f1d22e47de3b4acf496b6ccb6e7b06ffa03bfbf2871135506aade1aa76b20f71602e28fc5612a5193e8a53210626674fb139cdc8f809381f0504eb1b04e8b7b95d2f6c61067d110e42d584c7bd5118e8822ecb9212775026cd6aa7738a47efe011dd108484e4ab2e97acf1f95179f9b1b5e91fd06825c6db89121e1f40d0dedb15fe6e1d17d629b0bafa1ca62a295a997079b7f5dd26f2a8669fed7991927f6d91848c0ba773aa72b035773a9102275b767262500e50acdbb90a0b7d22aec017cd57d48d3aeca72b9cb1bb74548f8457c4e63ad163ee9fa95d820b451d8a1a0642a704c24a35fe508cf9435985701280f7d1c59da0795f1e315e80538efda1302b56df7f02d739be790c639146ccf54383dd709f53cef1734b84cb55e95f896a25fe329d54168d538875b1cbc8ffd6bd6ba59025e499736e21c33e23087f4bb671547e4838186c90ba0b4f2b728bedb9e8a18a7d36c52d671c6e2d72e8c1f8756a429a9adeda19ba6a31e6d37f7bc39b30f84ddfb939a7614c26f12ad570dee8b1816fc0b903696d74337534e429b805b3428f9398ea0eb84041c132e1630632c0753db18039d57bdedd38a0f067e17421e2b61610900c1b9596ddf6bdc3354f9c7fa17eff4c2e4afd7d4d632583cb176ae6c979524b0b0c7a59545d9031df31821962505ec51dcf276b88f22140e45f1ca7841f2fc860b644d8c3cce8bf53eb4a834cb2fb510cbaa16065f1f9e5d60b00126355534c2951ff4737322eb04f8b519707d5c3786d58bdfd337c8a97a1136027cbbc538203203258de5fb8904a855949eefe9a30c76d8299b3e6814827e3715e5ef144f255db5f8339b0c5725369f3c31508fce68c4373c0b03ba9563d424ce58fb1f5c846b9ad96720b95177f503a0b24e5d912de240a1dd1b6106b55b0fe732813db608ccba4aa84c192d13e050a5e9bf97d4ab381c82e27cd5ef0ba62c494fa47bff58737bf61bfdfddcc7ae55c0222199744afebd82256f4fc54c25a94fbe3a96fc7c0af0314e19848c5cab080ea8b7006c14f50d1f37c5d66dd70c374dd9c92e0f89175cec4c80f8e9bc63d789c7a6ba08072addd6ef75c4a238512b0c0ff14683de1add54ede430a676d3212675466c142928fa2d8ba664be189dc01546846e2ed72b1c931bc555530b2e5f43527addb77296fa2f2ec3beb24be078496a0f5e4bafe6bfccca97fd3c33b96680d8302d60d82729a44c5681d21c3bc67e4b8d0f37b4e13a9701f7743a7613e675267c161679371f1389d3c877493fe7e8ba30895929bfa267e00225de8338bd06e9aee2b754e7ca6d9d98b7d248e6469ffe53b364a1737e7f01d8a93f6970d3f7c18386eb0d043101b58d1752628e7b52c156fb7acc2c2027bcd5ff586e8ba14320585b783721a57c938ae26a7c7b1618ecd7af5959e77b7a0b41e73ae62d09ac204de43e3e652b1eaa2cf4d456f38c931d12daa69d79f93fb2e62ae41717702cab1aceef692883d393f41fe50a5831017cbdab1667bd0968e0607b588251919ff83554e0be1442a22dd9807d0c9e9318463005a01e949ffb34e32d6347790a7a13bab8c99f1e7e8e6266502124d15939726ecf82f98f99b05747d47e8779a9a6fec7d3b7c37ec275f597f1e6d50a9cf6165334e84f3fc6b318f76ead9a1107e943eb5688ac7a1613ea5069f04319c565e6e609c0a9d8f0d02584c34bef53bfbe510474145d31489619e5bea612e566e2a56e69da73a392841a33f93e69f478f077909e45ce995099f252e7a989d333960d3ecd70b04bd668948897b50123ed52aa8d3b07ed0f231a32686e63b60b32253717f88b7f5d99d2a417b3736289db7b110887a29ac667838fb2898d14adb61176082dd76a133b92632f688b5eadc3e6a6937433967674ea4a84b4809e68957c099beac191ed55083199ca11aa913fee64a36dd4dee031bd576bafd8c277ebb7c6f660860653a75c29966f179f0052c9987263b0d0aeb238ac992c0a26e4867326e091868527c0c47f4a75dacf77028f91433da131137f8ef7eaa6b2e986df95a355d25f9fcd9ab20976bca74d62520a93da239bf3b118575b7abeba311c9b3e15abbbe0d86160e54a1d782f2cfebbfb9b126105b167ed719bf83256def4db91427a93c4a57826931f1a473d052b4b52cc814e193db92f9f17db2f9e5bb87fe0bf02acfd950cca6533d14789a432161727e830f24e5c80875abb9e6a2a5ff00fb8025e94759bb6409f6a3fa3b6f78722cceaba73a8722bf29b41d2b8f24b5b3d798beb8a0a4c0883334ab069aa52151aea501f5c265dfac5d3d29c1e56b1b0f1a74254cea686fbd9a13251ab750bb4ebdcf42d3265dcfb2e19ea4701f4e4da338150d50f72c2990c2d05f774874586bd1c54a59d1e076dd757e8f3bed2b3e850d86c1100a5fb143c193c3ada96c6784e4a40c04fe16d891d8170ad0afa219756ab3c8e7647c88561312fdb0d916dbbd3c89bc512599ff501bc334b376e5bdf353606b2d27085a6c2e9246c73f1c65b7e93e91f4945641fc38eef62f78ea2fc03c70a"
                    )
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