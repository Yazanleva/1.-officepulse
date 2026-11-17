package com.officepulse.app.visits

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.officepulse.app.ui.theme.CampusHeader
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
    var query by rememberSaveable { mutableStateOf("") }
    val visible = searchVisits(state.visits, query)
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            CampusHeader("My visits")
            Spacer(Modifier.height(12.dp))
            Text(email, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { edit(null) }) { Text("Book a visit") }
                TextButton(onClick = logout) { Text("Log out") }
            }
        }
        item { OutlinedTextField(query, { query = it }, label = { Text("Search by office or topic") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = "" }) { Text("Clear") } }) }
        when {
            state.loading -> item { CircularProgressIndicator() }
            state.error != null -> item { Text(state.error.orEmpty()); TextButton(onClick = model::retry) { Text("Retry") } }
            state.visits.isEmpty() -> item { Text("No visits yet. Choose Book a visit to arrange your first appointment.") }
            visible.isEmpty() -> item { Text("No visits match your search.") }
        }
        items(visible, key = { it.id }) { visit ->
            OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { edit(visit.id) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(visit.officeName, style = MaterialTheme.typography.titleMedium)
                    Text(visit.input.topic)
                    Text(displayTime(visit.input.start) + " – " + displayTime(visit.input.end))
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.Events.TITLE, visit.input.topic)
                            .putExtra(CalendarContract.Events.EVENT_LOCATION, visit.officeName)
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, visit.input.start)
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, visit.input.end)
                        try { context.startActivity(intent) } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, "Install a calendar app to add this visit.", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Add to calendar") }
                }
            }
        }
    }
}

@Composable
private fun TimeField(label: String, value: Long, error: String?, enabled: Boolean, change: (Long) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), enabled = enabled, onClick = {
        val cal = Calendar.getInstance()
        if (value > 0) cal.timeInMillis = value
        DatePickerDialog(context, { _, year, month, day ->
            TimePickerDialog(context, { _, hour, minute ->
                cal.set(year, month, day, hour, minute, 0)
                cal.set(Calendar.MILLISECOND, 0)
                change(cal.timeInMillis)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(if (value == 0L) "Select date & time" else displayTime(value), style = MaterialTheme.typography.bodySmall)
        }
    }
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
        TextButton(onClick = close, enabled = !state.busy) { Text("‹   My visits") }
        CampusHeader(if (editing) "Edit visit" else "Book a visit")
        if (state.loading) CircularProgressIndicator()
        FieldError(state.message)
        if (state.loadFailed) Button(onClick = model::load) { Text("Retry") }
        if (!state.loading && !state.loadFailed) {
            Text("Campus offices", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Demo locations", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.offices.forEach { office ->
                val selected = office.id == input.officeId
                val (tile, ink, mark) = when (office.id) {
                    "exchange" -> Triple(Color(0xFFDDE8FF), Color(0xFF314989), "IO")
                    "finance" -> Triple(Color(0xFFFFE1C3), Color(0xFF7A4319), "SF")
                    else -> Triple(Color(0xFFFFD9E7), Color(0xFF730237), "SR")
                }
                Surface(shape = RoundedCornerShape(18.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().selectable(selected = selected, enabled = !state.busy,
                        role = Role.RadioButton, onClick = { model.change(input.copy(officeId = office.id)) })) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(42.dp).background(tile, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                            Text(mark, color = ink, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(office.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(office.building, style = MaterialTheme.typography.bodySmall)
                        }
                        RadioButton(selected = selected, onClick = null)
                    }
                }
            }
            FieldError(state.errors.office)
            OutlinedTextField(input.topic, { model.change(input.copy(topic = it)) },
                label = { Text("Visit topic") }, enabled = !state.busy,
                isError = state.errors.topic != null, modifier = Modifier.fillMaxWidth())
            FieldError(state.errors.topic)
            Text("Appointment time", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TimeField("Start", input.start, state.errors.start, !state.busy) { start ->
                val duration = if (input.end > input.start && input.start > 0) input.end - input.start else 0
                model.change(input.copy(start = start, end = if (duration > 0) start + duration else input.end))
            }
            TimeField("End", input.end, state.errors.end, !state.busy) { model.change(input.copy(end = it)) }
            if (input.start > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 60).forEach { minutes ->
                        val end = input.start + minutes * 60000L
                        FilterChip(selected = input.end == end, enabled = !state.busy,
                            onClick = { model.change(input.copy(end = end)) }, label = { Text("$minutes min") })
                    }
                }
            }
            Button(onClick = model::save, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp)) { Text(if (state.busy) "Saving…" else "Save visit") }
            if (editing) TextButton(onClick = { confirm = true }, enabled = !state.busy) { Text("Delete visit") }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Delete this visit?") },
        text = { Text("Remove “" + input.topic + "” from My visits?") },
        confirmButton = { TextButton(onClick = { confirm = false; model.delete() }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
}

