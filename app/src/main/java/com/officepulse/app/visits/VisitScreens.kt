package com.officepulse.app.visits

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

private fun displayTime(value: Long) = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(value))

@Composable
fun VisitsScreen(model: VisitListModel, email: String, logout: () -> Unit, edit: (String?) -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("OfficePulse", style = MaterialTheme.typography.labelLarge)
            Text("My visits", style = MaterialTheme.typography.headlineLarge)
            Text(email)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { edit(null) }) { Text("Book a visit") }
                TextButton(onClick = logout) { Text("Log out") }
            }
        }
        when {
            state.loading -> item { CircularProgressIndicator() }
            state.error != null -> item { Text(state.error.orEmpty()); TextButton(onClick = model::retry) { Text("Retry") } }

        }
        items(state.visits, key = { it.id }) { visit ->
            OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { edit(visit.id) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(visit.officeName, style = MaterialTheme.typography.titleMedium)
                    Text(visit.input.topic)
                    Text(displayTime(visit.input.start) + " – " + displayTime(visit.input.end))
                    Text(visit.status.replaceFirstChar { it.uppercase() })
                    if (visit.reason.isNotBlank()) Text(visit.reason)
                }
            }
        }
    }
}

@Composable
private fun TimeField(label: String, value: Long, error: String?, enabled: Boolean, change: (Long) -> Unit) {
    var raw by rememberSaveable(value) { mutableStateOf(if (value == 0L) "" else value.toString()) }
    OutlinedTextField(raw, { raw = it; it.toLongOrNull()?.let(change) }, enabled = enabled,
        label = { Text(label + " (Unix milliseconds)") }, singleLine = true)
    FieldError(error)
}

@Composable
private fun FieldError(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
}

@Composable
fun VisitFormScreen(model: VisitFormModel, editing: Boolean, close: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirm by rememberSaveable { mutableStateOf(false) }
    val input = state.input
    LaunchedEffect(state.result) {
        state.result?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); close() }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (editing) "Edit visit" else "Book a visit", style = MaterialTheme.typography.headlineMedium)
        TextButton(onClick = close, enabled = !state.busy) { Text("Back to visits") }
        if (state.loading) CircularProgressIndicator()
        FieldError(state.message)
        if (state.loadFailed) Button(onClick = model::load) { Text("Retry") }
        if (!state.loading && !state.loadFailed) {
            Text("Choose an office", style = MaterialTheme.typography.titleMedium)
            state.offices.forEach { office ->
                FilterChip(selected = office.id == input.officeId, enabled = !state.busy,
                    onClick = { model.change(input.copy(officeId = office.id)) },
                    label = { Text(office.name + " · " + office.building) })
            }
            FieldError(state.errors.office)
            OutlinedTextField(input.topic, { model.change(input.copy(topic = it)) },
                label = { Text("Visit topic") }, enabled = !state.busy,
                isError = state.errors.topic != null, modifier = Modifier.fillMaxWidth())
            FieldError(state.errors.topic)
            TimeField("Start", input.start, state.errors.start, !state.busy) { model.change(input.copy(start = it)) }
            TimeField("End", input.end, state.errors.end, !state.busy) { model.change(input.copy(end = it)) }
            Button(onClick = model::save, enabled = !state.busy) { Text(if (state.busy) "Saving…" else "Save visit") }
            if (editing) TextButton(onClick = { confirm = true }, enabled = !state.busy) { Text("Delete visit") }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Delete this visit?") },
        text = { Text("Remove “" + input.topic + "” from My visits?") },
        confirmButton = { TextButton(onClick = { confirm = false; model.delete() }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
}

