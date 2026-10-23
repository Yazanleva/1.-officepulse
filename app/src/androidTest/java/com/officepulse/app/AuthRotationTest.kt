package com.officepulse.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class AuthRotationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun formValuesSurviveActivityRecreation() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runRotationCheck") == "true")
        assumeTrue(FirebaseAuth.getInstance().currentUser == null)
        compose.onNodeWithText("Email").performTextInput("rotation-check@example.com")
        compose.onNodeWithText("Password").performTextInput("Rotation7!")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("rotation-check@example.com").assertIsDisplayed()
        compose.onNodeWithText("Password").assertTextContains("Rotation7!")
        compose.onNodeWithText("Email").performTextReplacement("")
        compose.onNodeWithText("Password").performTextReplacement("")
    }
}
