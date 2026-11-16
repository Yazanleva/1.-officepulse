package com.officepulse.app.visits

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreVisits : VisitRepository {
    private val db = FirebaseFirestore.getInstance()
    private val visits = db.collection("visits")
    private fun uid() = requireNotNull(FirebaseAuth.getInstance().uid)
    override fun observeMine() = callbackFlow {
        val subscription = visits.whereEqualTo("ownerId", uid()).addSnapshotListener { snapshot, error ->
            if (error != null) close(error)
            else if (snapshot != null) runCatching {
                snapshot.documents.map { it.toVisit() }.sortedBy { it.input.start }
            }.onSuccess { trySend(it) }.onFailure { close(it) }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun offices() = db.collection("offices").orderBy("name").get().await().documents.map {
        Office(it.id, requireNotNull(it.getString("name")), requireNotNull(it.getString("building")))
    }

    override suspend fun get(id: String) = visits.document(id).get().await().toVisit()

    override suspend fun save(id: String?, input: VisitInput, office: Office) {
        val fields = mutableMapOf<String, Any>(
            "officeId" to office.id, "officeName" to office.name, "topic" to input.topic.trim(),
            "startAt" to Timestamp(Date(input.start)), "endAt" to Timestamp(Date(input.end)),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (id == null) {
            fields.putAll(mapOf("ownerId" to uid(), "createdAt" to FieldValue.serverTimestamp(),
                "status" to "pending", "rejectReason" to ""))
            visits.document().set(fields).await()
        } else {
            val ref = visits.document(id)
            db.runTransaction { tx ->
                val previous = tx.get(ref).toVisit().input
                val changes = fields.toMutableMap()
                if (previous.officeId != input.officeId || previous.start != input.start || previous.end != input.end) {
                    changes["status"] = "pending"
                    changes["rejectReason"] = ""
                }
                tx.update(ref, changes)
            }.await()
        }
    }

    override suspend fun delete(id: String) { visits.document(id).delete().await() }

    private fun DocumentSnapshot.toVisit() = Visit(id, requireNotNull(getString("officeName")),
        VisitInput(requireNotNull(getString("officeId")), requireNotNull(getString("topic")),
            requireNotNull(getTimestamp("startAt")).toDate().time, requireNotNull(getTimestamp("endAt")).toDate().time),
        requireNotNull(getString("status")), getString("rejectReason").orEmpty())
}

