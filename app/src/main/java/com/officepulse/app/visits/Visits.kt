package com.officepulse.app.visits

import kotlinx.coroutines.flow.Flow

data class Office(val id: String, val name: String, val building: String)
data class VisitInput(val officeId: String = "", val topic: String = "", val start: Long = 0, val end: Long = 0)
data class Visit(val id: String, val officeName: String, val input: VisitInput,
                 val status: String = "pending", val reason: String = "")
data class FieldErrors(val office: String? = null, val topic: String? = null,
                       val start: String? = null, val end: String? = null)

fun validateVisit(input: VisitInput) = FieldErrors(
    office = if (input.officeId.isBlank()) "Choose an office." else null,
    topic = when {
        input.topic.isBlank() -> "Enter a visit topic."
        input.topic.length > 200 -> "Use at most 200 characters."
        else -> null
    },
    start = if (input.start <= 0) "Choose a start time." else null,
    end = if (input.end <= input.start) "End must be after start." else null
)

fun searchVisits(visits: List<Visit>, query: String): List<Visit> = visits.filter {
    it.officeName.contains(query.trim(), ignoreCase = true) ||
        it.input.topic.contains(query.trim(), ignoreCase = true)
}

interface VisitRepository {
    fun observeMine(): Flow<List<Visit>>
    suspend fun offices(): List<Office>
    suspend fun get(id: String): Visit
    suspend fun save(id: String?, input: VisitInput, office: Office)
    suspend fun delete(id: String)
}

