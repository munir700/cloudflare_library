package com.sslcf

/**
 * Define object class to interact with native library class
 * First load Library and define methods to access cpp operations
 * @author Munir Ahmad
 */
object NativeCloudflareData {
     init {
         System.loadLibrary("native-lib")
     }

    external fun encryptedPart1(): String
    external fun encryptedPart2(): String
}