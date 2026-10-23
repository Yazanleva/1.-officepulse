package com.officepulse.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = auth.currentUser?.toAuthUser()

    override val authState = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAuthUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun register(email: String, password: String): Result<Unit> =
        authResult { auth.createUserWithEmailAndPassword(email.trim(), password).await() }

    override suspend fun login(email: String, password: String): Result<Unit> =
        authResult { auth.signInWithEmailAndPassword(email.trim(), password).await() }

    private suspend fun authResult(block: suspend () -> Unit): Result<Unit> =
        runCatching { block() }.onFailure {
            if (it is CancellationException) throw it
        }

    private fun FirebaseUser.toAuthUser() = AuthUser(uid, email)

    override fun logout() {
        auth.signOut()
    }
}
