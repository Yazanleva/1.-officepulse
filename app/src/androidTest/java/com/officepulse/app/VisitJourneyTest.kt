package com.officepulse.app

import android.graphics.Bitmap
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.officepulse.app.ui.theme.OfficePulseTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID

class VisitJourneyTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    private fun waitText(label: String) {
        compose.waitUntil(20_000) { compose.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun tap(label: String) = compose.onNodeWithText(label).performScrollTo().performClick()

    private fun screenshot(name: String) {
        val file = File(instrumentation.targetContext.filesDir, name + ".png")
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun pick(label: String, hour: Int) {
        tap(label)
        onView(withId(android.R.id.button1)).perform(click())
        onView(isAssignableFrom(TimePicker::class.java)).check { view, error ->
            if (error != null) throw error
            (view as TimePicker).hour = hour
            view.minute = 0
        }
        onView(withId(android.R.id.button1)).perform(click())
    }

    @Test fun studentBooksEditsSearchesAndDeletesOwnVisit() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runVisits") == "true")
        val auth = FirebaseAuth.getInstance()
        val password = UUID.randomUUID().toString()
        val email = "journey-" + UUID.randomUUID() + "@officepulse.example"
        runBlocking { auth.createUserWithEmailAndPassword(email,password).await() }
        try {
            compose.setContent { OfficePulseTheme { AppNavHost() } }
            waitText("No visits yet. Choose Book a visit to arrange your first appointment.")
            tap("Book a visit")
            waitText("Student Registry · Main building")
            tap("Save visit")
            waitText("Enter a visit topic.")
            screenshot("lab2-validation")
            tap("Student Registry · Main building")
            compose.onNodeWithText("Visit topic").performTextInput("Course registration")
            pick("Start", 10)
            pick("End", 11)
            tap("Save visit")
            waitText("Course registration")
            screenshot("lab2-list")
            val db = FirebaseFirestore.getInstance()
            val record = runBlocking { db.collection("visits").whereEqualTo("ownerId",auth.uid).get().await().documents.single() }
            val created = record.getTimestamp("createdAt")
            tap("Course registration")
            waitText("Save visit")
            compose.onNodeWithText("Visit topic").performTextReplacement("Transcript request")
            tap("Save visit")
            waitText("Transcript request")
            val edited = runBlocking { record.reference.get().await() }
            assertEquals(created, edited.getTimestamp("createdAt"))
            assertTrue(edited.getTimestamp("updatedAt")!! > record.getTimestamp("updatedAt")!!)
            compose.onNodeWithText("Search by office or topic").performTextInput("no matches")
            waitText("No visits match your search.")
            screenshot("lab2-search")
            tap("Clear")
            tap("Transcript request")
            waitText("Delete visit")
            tap("Delete visit")
            compose.onNodeWithText("Cancel").performClick()
            tap("Delete visit")
            compose.onNodeWithText("Delete").performClick()
            waitText("No visits yet. Choose Book a visit to arrange your first appointment.")
            assertTrue(runBlocking { db.collection("visits").whereEqualTo("ownerId", auth.uid).get().await().isEmpty })
        } finally { auth.signOut() }
    }
}

