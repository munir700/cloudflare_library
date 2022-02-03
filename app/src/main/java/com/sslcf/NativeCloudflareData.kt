package com.sslcf

object NativeCloudflareData {
    init {
        System.loadLibrary("native-lib")
    }

    external fun encryptedDataPart1(): String
    external fun encryptedDataPart2(): String
}