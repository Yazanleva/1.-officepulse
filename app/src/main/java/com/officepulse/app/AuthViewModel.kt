package com.officepulse.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val message: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(userEmail = repository.currentUserEmail)
    )

    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        authenticate(
            email = email,
            password = password,
            confirmPassword = null
        )
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String
    ) {
        authenticate(
            email = email,
            password = password,
            confirmPassword = confirmPassword
        )
    }

    private fun authenticate(
        email: String,
        password: String,
        confirmPassword: String?
    ) {
        if (_uiState.value.isLoading) return

        val emailError = AuthValidator.emailError(email)
        val passwordError = AuthValidator.passwordError(password)
        val confirmationError = confirmPassword?.let {
            AuthValidator.confirmPasswordError(password, it)
        }

        _uiState.value = AuthUiState(
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmationError
        )

        if (
            emailError != null ||
            passwordError != null ||
            confirmationError != null
        ) {
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                if (confirmPassword == null) {
                    repository.login(email, password)
                } else {
                    repository.register(email, password)
                }

                _uiState.value = AuthUiState(
                    userEmail = repository.currentUserEmail
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = AuthUiState(
                    message = readableError(exception)
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun clearErrors() {
        if (_uiState.value.isLoading) return

        _uiState.value = AuthUiState(
            userEmail = repository.currentUserEmail
        )
    }

    fun logout() {
        if (_uiState.value.isLoading) return

        repository.logout()
        _uiState.value = AuthUiState()
    }

    private fun readableError(exception: Exception): String {
        return when (exception) {
            is FirebaseNetworkException ->
                "Check your internet connection and try again."

            is FirebaseAuthUserCollisionException ->
                "This email already has an account. Please log in."

            is FirebaseAuthWeakPasswordException ->
                "Your password is too weak. Choose a stronger password."

            is FirebaseAuthInvalidCredentialsException ->
                "Check your email and password, then try again."

            else ->
                "Unable to sign in or register. Please try again."
        }
    }
}