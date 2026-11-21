package com.officepulse.app

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.officepulse.app.visits.FirestoreVisits
import com.officepulse.app.visits.VisitInput
import com.officepulse.app.ui.theme.OfficePulseTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID

class BookingDecisionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun waitText(text: String) = compose.waitUntil(30_000) {
        compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
    private fun screenshot(name: String) {
        val i = InstrumentationRegistry.getInstrumentation()
        val bitmap = i.uiAutomation.takeScreenshot()
        File(i.targetContext.filesDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
    @Test fun realTriggerConfirmsRejectsAndRechecksEdits() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runLab3") == "true")
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val repo = FirestoreVisits()
        val nonce = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()
        val first = "decision-a-$nonce@officepulse.example"
        val second = "decision-b-$nonce@officepulse.example"
        val start = System.currentTimeMillis() + 86400000L + (nonce.hashCode().toLong() and 0x7fffffffL) * 1000L
        suspend fun status(id: String, expected: String) {
            repeat(120) {
                if (db.collection("visits").document(id).get().await().getString("status") == expected) return
                delay(250)
            }
            fail("Server did not produce $expected for $id")
        }
        try {
            auth.createUserWithEmailAndPassword(first, password).await()
            val office = repo.offices().first { it.id == "registry" }
            repo.save(null, VisitInput(office.id, "First confirmed visit", start, start + 60000), office)
            val firstId = db.collection("visits").whereEqualTo("ownerId", auth.uid).get().await().documents.single().id
            status(firstId, "confirmed")
            auth.signOut()
            auth.createUserWithEmailAndPassword(second, password).await()
            repo.save(null, VisitInput(office.id, "Conflicting visit", start, start + 60000), office)
            val secondId = db.collection("visits").whereEqualTo("ownerId", auth.uid).get().await().documents.single().id
            status(secondId, "rejected")
            compose.setContent { OfficePulseTheme { AppNavHost() } }
            waitText("Status: Rejected")
            waitText("This office is already booked during that time. Choose another time.")
            compose.onNodeWithText("First confirmed visit").assertDoesNotExist()
            screenshot("lab3-rejected")
            repo.save(secondId, VisitInput(office.id, "Adjacent edited visit", start + 60000, start + 120000), office)
            status(secondId, "confirmed")
            waitText("Status: Confirmed")
            waitText("Adjacent edited visit")
            compose.onNodeWithText("This office is already booked during that time. Choose another time.").assertDoesNotExist()
            screenshot("lab3-confirmed")
            repo.delete(secondId)
            waitText("No visits yet. Choose Book a visit to arrange your first appointment.")
            auth.signOut()
            auth.signInWithEmailAndPassword(first, password).await()
            repo.delete(firstId)
        } finally { auth.signOut() }
    }
}
