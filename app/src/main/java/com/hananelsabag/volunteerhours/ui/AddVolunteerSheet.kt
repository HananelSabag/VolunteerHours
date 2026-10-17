package com.hananelsabag.volunteerhours.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVolunteerSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("הוספת מתנדב", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("שם מלא") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                isError = nameError,
                supportingText = if (nameError) ({
                    Text(if (name.trim().isEmpty()) "שדה חובה" else "שם ארוך מדי (מקסימום 100 תווים)")
                }) else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("טלפון (אופציונלי)") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("ביטול") }
                Button(
                    onClick = {
                        val trimmed = name.trim()
                        nameError = trimmed.isEmpty() || trimmed.length > 100
                        if (!nameError) onConfirm(trimmed, phone.trim())
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("הוסף") }
            }
        }
    }
}
