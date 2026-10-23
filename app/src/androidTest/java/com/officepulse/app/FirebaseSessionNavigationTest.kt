package com.officepulse.app

import android.content.Intent
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Run after the opt-in cloud acceptance test, with its session retained. */
class FirebaseSessionNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun checkAppLeftForeground() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.uptimeMillis() + 5000
        while (automation.rootInActiveWindow?.packageName?.toString() == "com.officepulse.app" &&
            SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(50)
        }
        assertFalse("Back must leave the root screen instead of showing the previous auth screen",
            automation.rootInActiveWindow?.packageName?.toString() == "com.officepulse.app")
    }

    private fun reopen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.startActivity(Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test fun backCannotRestorePreviousAuthenticationScreens() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runCloudSessionChecks") == "true")
        assumeTrue("Run the cloud acceptance test with the app retained first",
            FirebaseAuth.getInstance().currentUser != null)
        val completed = mutableListOf<String>()

        compose.onNodeWithText("Log out").assertIsDisplayed()
        pressBackUnconditionally()
        checkAppLeftForeground()
        completed += "Back from authenticated Home leaves the app instead of showing Login"

        reopen()
        compose.onNodeWithText("Log out").assertIsDisplayed()
        completed += "Reopening restores the authenticated Home screen"

        compose.onNodeWithText("Log out").performClick()
        compose.onNodeWithText("Log in to your account").assertIsDisplayed()
        pressBackUnconditionally()
        checkAppLeftForeground()
        completed += "Back after logout leaves Login instead of restoring Home"

        reopen()
        compose.onNodeWithText("Log in to your account").assertIsDisplayed()
        compose.onNodeWithText("Log out").assertDoesNotExist()
        completed += "Reopening after logout remains on Login"

        InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
            .resolve("lab1-session-result.json").writeText(JSONObject()
                .put("passed", true).put("completed", JSONArray(completed)).toString(2))
    }
}
