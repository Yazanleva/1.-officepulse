package com.officepulse.app.visits

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitFormTest {
    private val dispatcher = StandardTestDispatcher()
    private val office = Office("registry","Registry","Main")
    private val example = VisitInput(office.id,"Transcript",1000,2000)
    private inner class Fake : VisitRepository {
        var writes = 0
        var deletes = 0
        var fail = false
        val visits = mutableMapOf("saved" to Visit("saved", office.name, example))
        override fun observeMine() = flowOf(visits.values.toList())
        override suspend fun offices() = listOf(office)
        override suspend fun get(id: String) = visits.getValue(id)
        override suspend fun save(id: String?, input: VisitInput, office: Office) {
            writes++
            if (fail) error("offline")
            val key = id ?: "created"
            visits[key] = Visit(key, office.name, input)
        }
        override suspend fun delete(id: String) { deletes++; visits.remove(id) }
    }
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    @Test fun invalidInputShowsErrorsWithoutWriting() = runTest(dispatcher) {
        val repo = Fake()
        val model = VisitFormModel(repo, null)
        advanceUntilIdle()
        model.save()
        advanceUntilIdle()
        assertNotNull(model.state.value.errors.office)
        assertEquals(0, repo.writes)
    }
    @Test fun validInputSavesOnceAndReportsSuccess() = runTest(dispatcher) {
        val repo = Fake()
        val model = VisitFormModel(repo, null)
        advanceUntilIdle()
        model.change(example)
        model.save()
        model.save()
        advanceUntilIdle()
        assertEquals(example, repo.visits.getValue("created").input)
        assertEquals(1, repo.writes)
        assertEquals("Visit saved.", model.state.value.result)
    }
    @Test fun failedWriteShowsMessageAndAllowsRetry() = runTest(dispatcher) {
        val repo = Fake().apply { fail = true }
        val model = VisitFormModel(repo, null)
        advanceUntilIdle()
        model.change(example)
        model.save()
        advanceUntilIdle()
        assertTrue(model.state.value.message!!.contains("retry"))
        assertFalse(model.state.value.busy)
        assertEquals(1, repo.writes)
        repo.fail = false
        model.save()
        advanceUntilIdle()
        assertEquals(2, repo.writes)
        assertEquals("Visit saved.", model.state.value.result)
    }
    @Test fun editingLoadsExistingVisitAndDeletesById() = runTest(dispatcher) {
        val repo = Fake()
        val model = VisitFormModel(repo, "saved")
        advanceUntilIdle()
        assertEquals(example, model.state.value.input)
        model.delete()
        advanceUntilIdle()
        assertTrue(repo.visits.isEmpty())
        assertEquals(1, repo.deletes)
        assertEquals(0, repo.writes)
        assertEquals("Visit deleted.", model.state.value.result)
    }
}

