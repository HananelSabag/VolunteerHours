package com.hananelsabag.volunteerhours.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hananelsabag.volunteerhours.data.Volunteer
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private const val MAX_HOURS = 24f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntrySheet(
    savedVolunteers:    List<Volunteer> = emptyList(),
    preselectedId:      Long? = null,
    preselectedName:    String? = null,
    onDismiss: () -> Unit,
    onConfirmExisting:  (volunteerId: Long, date: String, hours: Float) -> Unit = { _, _, _ -> },
    onConfirmFreeText:  (name: String, phone: String, date: String, hours: Float) -> Unit = { _, _, _, _ -> }
) {
    val isoFmt  = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dispFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // ── Volunteer selection ──────────────────────────────────────────────────
    // isNewMode: false = pick existing, true = type new
    val isPreselected = preselectedId != null
    var isNewMode by remember { mutableStateOf(savedVolunteers.isEmpty() || preselectedId != null) }
    var selectedVolunteer by remember { mutableStateOf<Volunteer?>(null) }
    var showVolunteerPicker by remember { mutableStateOf(false) }
    var newName  by remember { mutableStateOf(preselectedName ?: "") }
    var newPhone by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    // ── Hours ────────────────────────────────────────────────────────────────
    var useTimeRange by remember { mutableStateOf(false) }
    var hoursText    by remember { mutableStateOf("") }
    var hoursError   by remember { mutableStateOf(false) }
    var hoursErrorMsg by remember { mutableStateOf("הכנס מספר חיובי") }

    // Time range state
    var startHour   by remember { mutableIntStateOf(9)  }
    var startMinute by remember { mutableIntStateOf(0)  }
    var endHour     by remember { mutableIntStateOf(10) }
    var endMinute   by remember { mutableIntStateOf(0)  }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    val rangeMinutes  = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
    val rangeHours    = rangeMinutes / 60f
    val rangeValid    = rangeMinutes > 0
    val rangeDisplay  = if (rangeValid) formatHours(rangeHours) + " שע׳" else "⚠ סיום לפני התחלה"

    // ── Date ─────────────────────────────────────────────────────────────────
    var showDatePicker by remember { mutableStateOf(false) }
    var dateMillis     by remember { mutableStateOf(System.currentTimeMillis()) }
    val displayDate    = remember(dateMillis) { dispFmt.format(Date(dateMillis)) }

    // ── Volunteer picker dialog ──────────────────────────────────────────────
    if (showVolunteerPicker) {
        AlertDialog(
            onDismissRequest = { showVolunteerPicker = false },
            title = { Text("בחר מתנדב", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(savedVolunteers, key = { it.id }) { v ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedVolunteer = v; showVolunteerPicker = false }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(v.name, style = MaterialTheme.typography.bodyLarge)
                                if (v.phone.isNotBlank())
                                    Text(v.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showVolunteerPicker = false }) { Text("ביטול") } }
        )
    }

    // ── Date picker ──────────────────────────────────────────────────────────
    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { state.selectedDateMillis?.let { dateMillis = it }; showDatePicker = false }) { Text("אישור") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("ביטול") } }
        ) { DatePicker(state = state) }
    }

    // ── Time pickers ─────────────────────────────────────────────────────────
    if (showStartPicker) {
        val state = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute)
        AlertDialog(
            onDismissRequest = { showStartPicker = false },
            title = { Text("שעת התחלה") },
            text = { Box(Modifier.fillMaxWidth(), Alignment.Center) { TimePicker(state = state) } },
            confirmButton = {
                TextButton(onClick = { startHour = state.hour; startMinute = state.minute; showStartPicker = false }) { Text("אישור") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("ביטול") } }
        )
    }
    if (showEndPicker) {
        val state = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute)
        AlertDialog(
            onDismissRequest = { showEndPicker = false },
            title = { Text("שעת סיום") },
            text = { Box(Modifier.fillMaxWidth(), Alignment.Center) { TimePicker(state = state) } },
            confirmButton = {
                TextButton(onClick = { endHour = state.hour; endMinute = state.minute; showEndPicker = false }) { Text("אישור") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("ביטול") } }
        )
    }

    // ── Main sheet ───────────────────────────────────────────────────────────
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("הוספת רשומה", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // ── Volunteer section ─────────────────────────────────────────────
            if (isPreselected) {
                // Read-only — came from detail screen
                OutlinedTextField(
                    value = preselectedName ?: "", onValueChange = {},
                    label = { Text("מתנדב") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(), readOnly = true, enabled = false, singleLine = true
                )
            } else {
                // Mode selector (only if there are saved volunteers)
                if (savedVolunteers.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isNewMode,
                            onClick  = { isNewMode = false; newName = ""; newPhone = "" },
                            label    = { Text("מתנדב קיים") },
                            leadingIcon = if (!isNewMode) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isNewMode,
                            onClick  = { isNewMode = true; selectedVolunteer = null },
                            label    = { Text("חדש") },
                            leadingIcon = if (isNewMode) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (!isNewMode && savedVolunteers.isNotEmpty()) {
                    // Existing volunteer picker button
                    OutlinedButton(
                        onClick   = { showVolunteerPicker = true },
                        modifier  = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            selectedVolunteer?.name ?: "בחר מתנדב...",
                            color = if (selectedVolunteer != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    if (nameError && selectedVolunteer == null)
                        Text("יש לבחור מתנדב", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                } else {
                    // New volunteer — name + phone
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it; nameError = false },
                        label = { Text("שם מתנדב") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        isError = nameError,
                        supportingText = if (nameError) ({ Text("יש להזין שם") }) else null,
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("טלפון (אופציונלי)") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Text(
                        "ישמר אוטומטית ברשימת המתנדבים",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Date ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = displayDate, onValueChange = {},
                label = { Text("תאריך") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                modifier = Modifier.fillMaxWidth(), singleLine = true, readOnly = true
            )

            // ── Hours mode toggle ─────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !useTimeRange, onClick = { useTimeRange = false },
                    label = { Text("שעות") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = useTimeRange, onClick = { useTimeRange = true },
                    label = { Text("משעה לשעה") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (!useTimeRange) {
                // Number input mode
                OutlinedTextField(
                    value = hoursText, onValueChange = { hoursText = it; hoursError = false },
                    label = { Text("מספר שעות") },
                    leadingIcon = { Icon(Icons.Default.Timer, null) },
                    isError = hoursError,
                    supportingText = if (hoursError) ({ Text(hoursErrorMsg) }) else null,
                    placeholder = { Text("לדוגמה: 2.5") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            } else {
                // Time range mode
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showStartPicker = true }, Modifier.weight(1f)) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("%02d:%02d".format(startHour, startMinute))
                    }
                    Text("→", Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.titleLarge)
                    OutlinedButton(onClick = { showEndPicker = true }, Modifier.weight(1f)) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("%02d:%02d".format(endHour, endMinute))
                    }
                }
                Surface(
                    color  = if (rangeValid) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.errorContainer,
                    shape  = MaterialTheme.shapes.medium
                ) {
                    Text(
                        rangeDisplay,
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                        color      = if (rangeValid) MaterialTheme.colorScheme.onPrimaryContainer
                                     else MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // ── Buttons ───────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, Modifier.weight(1f)) { Text("ביטול") }
                Button(
                    onClick = {
                        // Resolve final hours value
                        val finalHours: Float? = if (useTimeRange) {
                            if (!rangeValid) { hoursError = true; hoursErrorMsg = "שעת סיום חייבת להיות אחרי שעת התחלה"; null }
                            else rangeHours
                        } else {
                            val h = hoursText.toFloatOrNull()
                            hoursError = when {
                                h == null || h <= 0f -> { hoursErrorMsg = "הכנס מספר חיובי"; true }
                                h > MAX_HOURS        -> { hoursErrorMsg = "מקסימום $MAX_HOURS שעות לרשומה"; true }
                                else                 -> false
                            }
                            if (hoursError) null else h
                        }

                        finalHours ?: return@Button

                        val dateStr = isoFmt.format(Date(dateMillis))

                        if (isPreselected) {
                            onConfirmExisting(preselectedId!!, dateStr, finalHours)
                            return@Button
                        }

                        if (!isNewMode && savedVolunteers.isNotEmpty()) {
                            nameError = selectedVolunteer == null
                            if (!nameError) onConfirmExisting(selectedVolunteer!!.id, dateStr, finalHours)
                        } else {
                            nameError = newName.isBlank() || newName.length > 100
                            if (!nameError) onConfirmFreeText(newName.trim(), newPhone.trim(), dateStr, finalHours)
                        }
                    },
                    Modifier.weight(1f)
                ) { Text("שמור") }
            }
        }
    }
}
