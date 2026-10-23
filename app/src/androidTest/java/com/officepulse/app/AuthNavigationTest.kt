package com.officepulse.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.officepulse.app.ui.theme.OfficePulseTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AuthNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private class FakeRepository(initial: AuthUser? = null) : AuthRepository {
        override val authState = MutableStateFlow(initial)
        override val currentUser get() = authState.value
        var calls = 0
        override suspend fun login(email: String, password: String): Result<Unit> {
            calls++
            authState.value = AuthUser("test-user", email)
            return Result.success(Unit)
        }
        override suspend fun register(email: String, password: String) = login(email, password)
        override fun logout() { authState.value = null }
    }

    private fun show(repository: FakeRepository) {
        compose.setContent {
            val model = remember { AuthViewModel(repository) }
            OfficePulseTheme { AppNavHost(authViewModel = model) }
        }
    }

    @Test fun registerRejectsMismatchedConfirmationBeforeCallingRepository() {
        val repository = FakeRepository()
        show(repository)
        compose.onNodeWithText("No account? Register").performClick()
        compose.onNodeWithText("Email").performTextInput("student@example.com")
        compose.onNodeWithText("Password").performTextInput("abcdef")
        compose.onNodeWithText("Confirm password").performTextInput("different")
        compose.onNodeWithText("Create account").performClick()
        compose.onNodeWithText("Passwords do not match.").assertIsDisplayed()
        assertEquals(0, repository.calls)
    }

    @Test fun existingSessionShowsHomeAndLogoutShowsLogin() {
        val repository = FakeRepository(AuthUser("saved-user", "student@example.com"))
        show(repository)
        compose.onNodeWithText("student@example.com").assertIsDisplayed()
        compose.onNodeWithText("Log out").performClick()
        compose.onNodeWithText("Log in to your account").assertIsDisplayed()
    }

    @Test fun validLoginFollowsSessionToHome() {
        val repository = FakeRepository()
        show(repository)
        compose.onNodeWithText("Email").performTextInput("student@example.com")
        compose.onNodeWithText("Password").performTextInput("abcdef")
        compose.onNodeWithText("Log in").performClick()
        compose.onNodeWithText("student@example.com").assertIsDisplayed()
        compose.onNodeWithText("Log out").assertIsDisplayed()
        assertEquals(1, repository.calls)
    }
}

