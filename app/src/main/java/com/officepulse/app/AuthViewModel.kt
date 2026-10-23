package com.officepulse.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
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
    private val _uiState = MutableStateFlow(AuthUiState(userEmail = repository.currentUser?.email))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private val _isLoggedIn = MutableStateFlow(repository.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authState.collect { user ->
                _isLoggedIn.value = user != null
                _uiState.update {
                    if (user != null || it.userEmail != null) AuthUiState(userEmail = user?.email)
                    else it
                }
            }
        }
    }

    fun updateEmail(value: String) {
        if (!_uiState.value.isLoading) _uiState.update { it.copy(email = value, emailError = null, message = null) }
    }

    fun updatePassword(value: String) {
        if (!_uiState.value.isLoading) _uiState.update {
            it.copy(password = value, passwordError = null, confirmPasswordError = null, message = null)
        }
    }

    fun updateConfirmation(value: String) {
        if (!_uiState.value.isLoading) _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, message = null) }
    }

    fun login(email: String, password: String) = authenticate(email, password, null)

    fun register(email: String, password: String, confirmPassword: String) =
        authenticate(email, password, confirmPassword)

    private fun authenticate(email: String, password: String, confirmPassword: String?) {
        if (_uiState.value.isLoading) return
        val emailError = AuthValidator.validateEmail(email)
        val passwordError = AuthValidator.validatePassword(password)
        val confirmationError = confirmPassword?.let {
            AuthValidator.validatePasswordConfirmation(password, it)
        }
        _uiState.update {
            it.copy(email = email, password = password, confirmPassword = confirmPassword.orEmpty(),
                emailError = emailError, passwordError = passwordError,
                confirmPasswordError = confirmationError, message = null)
        }
        if (emailError != null || passwordError != null || confirmationError != null) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = if (confirmPassword == null) repository.login(email.trim(), password)
                    else repository.register(email.trim(), password)
                result.exceptionOrNull()?.let { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(message = readableError(error)) }
                }
                // The auth-state listener controls the session and navigation.
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearErrors() {
        if (!_uiState.value.isLoading) {
            _uiState.update { AuthUiState(email = it.email, userEmail = it.userEmail) }
        }
    }

    fun logout() {
        if (!_uiState.value.isLoading) repository.logout()
    }

    private fun readableError(error: Throwable): String = when (error) {
        is FirebaseNetworkException -> "Check your internet connection and try again."
        is FirebaseTooManyRequestsException -> "Too many attempts. Please wait and try again."
        is FirebaseAuthUserCollisionException -> "This email already has an account. Please log in."
        is FirebaseAuthWeakPasswordException -> "Your password is too weak. Choose a stronger password."
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
            "Check your email and password, then try again."
        else -> "Unable to sign in or register. Please try again."
    }
}
