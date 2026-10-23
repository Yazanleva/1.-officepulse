package com.officepulse.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    private class FakeRepository(initial: AuthUser? = null) : AuthRepository {
        override val authState = MutableStateFlow(initial)
        override val currentUser get() = authState.value
        var calls = 0
        var receivedEmail: String? = null
        var result: Result<Unit> = Result.success(Unit)
        override suspend fun login(email: String, password: String): Result<Unit> {
            calls++
            receivedEmail = email
            return result
        }
        override suspend fun register(email: String, password: String) = login(email, password)
        override fun logout() { authState.value = null }
    }

    @Test fun invalidInputNeverCallsTheRepository() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = AuthViewModel(repository)
        model.register("bad", "123", "different")
        advanceUntilIdle()
        assertEquals(0, repository.calls)
        assertNotNull(model.uiState.value.emailError)
        assertNotNull(model.uiState.value.passwordError)
        assertNotNull(model.uiState.value.confirmPasswordError)
        assertFalse(model.uiState.value.isLoading)
    }

    @Test fun mismatchedConfirmationNeverCallsTheRepository() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = AuthViewModel(repository)
        model.register("student@example.com", "abcdef", "ABCDEF")
        advanceUntilIdle()
        assertEquals(0, repository.calls)
        assertNotNull(model.uiState.value.confirmPasswordError)
    }

    @Test fun navigationWaitsForTheAuthListenerRatherThanTheRequestResult() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = AuthViewModel(repository)
        model.login(" student@example.com ", "abcdef")
        assertTrue(model.uiState.value.isLoading)
        model.login("student@example.com", "abcdef")
        advanceUntilIdle()
        assertEquals(1, repository.calls)
        assertEquals("student@example.com", repository.receivedEmail)
        assertFalse(model.isLoggedIn.value)
        repository.authState.value = AuthUser("student-id", "student@example.com")
        advanceUntilIdle()
        assertTrue(model.isLoggedIn.value)
        assertEquals("student@example.com", model.uiState.value.userEmail)
        assertEquals("", model.uiState.value.password)
    }

    @Test fun existingSessionStartsSignedInAndLogoutFollowsListener() = runTest(dispatcher) {
        val repository = FakeRepository(AuthUser("existing", "student@example.com"))
        val model = AuthViewModel(repository)
        advanceUntilIdle()
        assertTrue(model.isLoggedIn.value)
        model.logout()
        advanceUntilIdle()
        assertFalse(model.isLoggedIn.value)
        assertNull(model.uiState.value.userEmail)
    }

    @Test fun unexpectedFailureIsReadableAndDoesNotLeakExceptionDetails() = runTest(dispatcher) {
        val repository = FakeRepository().apply {
            result = Result.failure(IllegalStateException("private backend details"))
        }
        val model = AuthViewModel(repository)
        model.login("student@example.com", "abcdef")
        advanceUntilIdle()
        assertEquals("Unable to sign in or register. Please try again.", model.uiState.value.message)
        assertFalse(model.uiState.value.isLoading)
        assertFalse(model.isLoggedIn.value)
    }

    @Test fun editingAFieldClearsItsErrorAndSwitchingFormsClearsPasswords() = runTest(dispatcher) {
        val model = AuthViewModel(FakeRepository())
        model.login("bad", "123")
        model.updateEmail("student@example.com")
        model.updatePassword("abcdef")
        assertNull(model.uiState.value.emailError)
        assertNull(model.uiState.value.passwordError)
        model.clearErrors()
        assertEquals("student@example.com", model.uiState.value.email)
        assertEquals("", model.uiState.value.password)
        assertEquals("", model.uiState.value.confirmPassword)
    }
}
