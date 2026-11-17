package com.officepulse.app.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VisitListState(val visits: List<Visit> = emptyList(), val loading: Boolean = true, val error: String? = null)

class VisitListModel(private val repo: VisitRepository) : ViewModel() {
    private val refresh = MutableStateFlow(0)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state = refresh.flatMapLatest {
        repo.observeMine().map { VisitListState(it, loading = false) }
            .onStart { emit(VisitListState()) }
            .catch { emit(VisitListState(loading = false, error = "Couldn’t load visits. Check your connection and retry.")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0, 0), VisitListState())
    fun retry() { refresh.update { it + 1 } }
}

data class FormState(val input: VisitInput = VisitInput(), val offices: List<Office> = emptyList(),
    val loading: Boolean = true, val busy: Boolean = false, val errors: FieldErrors = FieldErrors(),
    val message: String? = null, val loadFailed: Boolean = false, val result: String? = null)

class VisitFormModel(private val repo: VisitRepository, private val id: String?) : ViewModel() {
    private val data = MutableStateFlow(FormState())
    val state = data.asStateFlow()
    init { load() }

    fun load() {
        data.value = FormState()
        viewModelScope.launch {
            try {
                val offices = repo.offices()
                val initial = id?.let { repo.get(it).input } ?: VisitInput()
                data.value = FormState(input = initial, offices = offices, loading = false)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                data.value = FormState(loading = false, loadFailed = true, message = "Couldn’t open this form. Check your connection and retry.")
            }
        }
    }
    fun change(input: VisitInput) {
        data.update { if (it.busy) it else it.copy(input = input, errors = FieldErrors(), message = null) }
    }
    fun save() {
        val current = data.value
        if (current.busy || current.loading || current.loadFailed) return
        val office = current.offices.find { it.id == current.input.officeId }
        var errors = validateVisit(current.input)
        if (office == null) errors = errors.copy(office = "Choose an available office.")
        if (errors != FieldErrors() || office == null) {
            data.update { it.copy(errors = errors) }
            return
        }
        perform("Visit saved.") { repo.save(id, current.input, office) }
    }
    fun delete() {
        if (id != null && !data.value.busy) perform("Visit deleted.") { repo.delete(id) }
    }
    private fun perform(result: String, operation: suspend () -> Unit) {
        data.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                operation()
                data.update { it.copy(result = result) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                data.update { it.copy(busy = false, message = "Couldn’t save this change. Check your connection and retry.") }
            }
        }
    }
}

