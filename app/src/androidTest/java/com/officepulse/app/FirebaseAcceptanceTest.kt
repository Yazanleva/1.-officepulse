package com.officepulse.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.officepulse.app.ui.theme.OfficePulseTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** Opt-in cloud check. Creates one dedicated test account; never logs its password. */
class FirebaseAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private class RecordingRepository : AuthRepository {
        private val delegate = FirebaseAuthRepository()
        override val currentUser get() = delegate.currentUser
        override val authState: Flow<AuthUser?> get() = delegate.authState
        var lastErrorCode: String? = null

        private fun record(result: Result<Unit>): Result<Unit> {
            lastErrorCode = (result.exceptionOrNull() as? FirebaseAuthException)?.errorCode
                ?: result.exceptionOrNull()?.javaClass?.simpleName
            return result
        }

        override suspend fun register(email: String, password: String) = record(delegate.register(email, password))
        override suspend fun login(email: String, password: String) = record(delegate.login(email, password))
        override fun logout() = delegate.logout()
    }

    private fun waitFor(text: String) {
        compose.waitUntil(timeoutMillis = 30_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    private fun submit(text: String) = compose.onNodeWithText(text).performScrollTo().performClick()

    @Test fun realFirebaseRegistrationErrorsLogoutAndLogin() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runCloudAuth") == "true")
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        val repository = RecordingRepository()
        val email = "officepulse-lab1-${UUID.randomUUID().toString().take(12)}@example.com"
        val password = "Lab7!${UUID.randomUUID()}"
        val completed = mutableListOf<String>()
        val evidence = JSONObject().put("email", email).put("backend", "officepulse-86e4d")
        try {
            compose.setContent {
                val model = remember { AuthViewModel(repository) }
                OfficePulseTheme { AppNavHost(authViewModel = model) }
            }
            submit("No account? Register")
            compose.onNodeWithText("Email").performTextInput(email)
            compose.onNodeWithText("Password").performTextInput(password)
            compose.onNodeWithText("Confirm password").performTextInput(password)
            submit("Create account")
            waitFor("Log out")
            compose.onNodeWithText(email).assertIsDisplayed()
            completed += "Registration through Compose opens Home"

            runBlocking { auth.currentUser!!.reload().await() }
            assertNotNull(auth.currentUser?.uid)
            evidence.put("uid", auth.currentUser!!.uid)
            completed += "User reload succeeds against the real Firebase server"

            submit("Log out")
            waitFor("Log in to your account")
            completed += "Logout returns to Login"

            submit("No account? Register")
            compose.onNodeWithText("Email").performTextInput(email)
            compose.onNodeWithText("Password").performTextInput(password)
            compose.onNodeWithText("Confirm password").performTextInput(password)
            submit("Create account")
            waitFor("This email already has an account. Please log in.")
            completed += "Duplicate email shows a friendly error"

            submit("Already have an account? Log in")
            compose.onNodeWithText("Password").performTextInput(password + "wrong")
            submit("Log in")
            waitFor("Check your email and password, then try again.")
            completed += "Wrong password shows a friendly error"

            compose.onNodeWithText("Password").performTextReplacement(password)
            submit("Log in")
            waitFor("Log out")
            compose.onNodeWithText(email).assertIsDisplayed()
            completed += "Correct credentials return to Home"
            evidence.put("passed", true)
        } finally {
            evidence.put("completed", JSONArray(completed))
            evidence.put("lastErrorCode", repository.lastErrorCode ?: JSONObject.NULL)
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
                .resolve("lab1-cloud-result.json").writeText(evidence.toString(2))
        }
    }
}
