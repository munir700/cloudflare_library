package com.sslcf

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger

class SllPinning: Application() {
    override fun onCreate() {
        super.onCreate()
        // Add to your Application class onCreate
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG)
    }
}