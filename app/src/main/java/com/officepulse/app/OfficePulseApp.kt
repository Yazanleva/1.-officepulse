package com.officepulse.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OfficePulseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.USE_EMULATORS) {
            FirebaseAuth.getInstance().useEmulator("127.0.0.1", 9099)
            FirebaseFirestore.getInstance().useEmulator("127.0.0.1", 8080)
        }
    }
}
