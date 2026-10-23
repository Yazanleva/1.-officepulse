package com.officepulse.app

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).await()
    }

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()
    }

    override fun logout() {
        auth.signOut()
    }
}