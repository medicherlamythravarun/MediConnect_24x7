package com.example.mediconnect24x7

import android.app.Application
import com.google.firebase.FirebaseApp

class MediConnectApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
