package com.officepulse.app

object AuthValidator {

    fun validateEmail(email: String): String? {
        val trimmedEmail = email.trim()
        val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

        return when {
            trimmedEmail.isEmpty() -> "Enter your email."
            !emailPattern.matches(trimmedEmail) -> "Enter a valid email."
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Enter your password."
            password.length < 6 -> "Use at least 6 characters."
            else -> null
        }
    }

    fun validatePasswordConfirmation(
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            confirmPassword.isEmpty() -> "Confirm your password."
            password != confirmPassword -> "Passwords do not match."
            else -> null
        }
    }
}
