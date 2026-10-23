package com.officepulse.app

import kotlinx.coroutines.flow.Flow

data class AuthUser(val uid: String, val email: String?)

interface AuthRepository {
    val currentUser: AuthUser?
    val authState: Flow<AuthUser?>

    suspend fun register(email: String, password: String): Result<Unit>

    suspend fun login(email: String, password: String): Result<Unit>

    fun logout()
}
