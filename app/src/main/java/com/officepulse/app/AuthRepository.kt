package com.officepulse.app

interface AuthRepository {
    val currentUserEmail: String?

    suspend fun register(email: String, password: String)

    suspend fun login(email: String, password: String)

    fun logout()
}