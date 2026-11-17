package com.officepulse.app.visits

import org.junit.Assert.*
import org.junit.Test

class VisitLogicTest {
    private val input = VisitInput("registry", "Enrolment", 1000, 2000)
    private val row = Visit("one", "Student Registry", input)

    @Test fun validBookingHasNoErrors() = assertEquals(FieldErrors(), validateVisit(input))
    @Test fun officeIsRequired() = assertNotNull(validateVisit(input.copy(officeId = "")).office)
    @Test fun whitespaceTopicIsEmpty() = assertNotNull(validateVisit(input.copy(topic = "  ")).topic)
    @Test fun topicAtLimitIsAccepted() = assertNull(validateVisit(input.copy(topic = "x".repeat(200))).topic)
    @Test fun topicAboveLimitIsRejected() = assertNotNull(validateVisit(input.copy(topic = "x".repeat(201))).topic)
    @Test fun startIsRequired() = assertNotNull(validateVisit(input.copy(start = 0)).start)
    @Test fun equalTimesAreRejected() = assertNotNull(validateVisit(input.copy(end = 1000)).end)
    @Test fun emptySearchShowsAll() = assertEquals(listOf(row), searchVisits(listOf(row), " "))
    @Test fun officeSearchIgnoresCase() = assertEquals(listOf(row), searchVisits(listOf(row), "REGIS"))
    @Test fun topicSearchIgnoresCase() = assertEquals(listOf(row), searchVisits(listOf(row), " ENROL "))
    @Test fun missingTermShowsNoRows() = assertTrue(searchVisits(listOf(row), "finance").isEmpty())
    @Test fun statusAndSearchBothApply() {
        val confirmed = row.copy(status = "confirmed")
        assertEquals(listOf(confirmed), searchVisits(listOf(row, confirmed), "registry", "Confirmed"))
        assertTrue(searchVisits(listOf(confirmed), "finance", "Confirmed").isEmpty())
    }
}
