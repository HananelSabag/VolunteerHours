package com.hananelsabag.volunteerhours.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSheet(
    onDismiss:     () -> Unit,
    onAddEntry:    () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.VolunteerActivism,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "ברוך הבא!",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "כאן תנהל את שעות ההתנדבות.\n" +
                "• הוסף מתנדבים ועקוב אחר הרשומות שלהם\n" +
                "• הוסף רשומה עם שם חדש — המתנדב ייווצר אוטומטית\n" +
                "• ייצא CSV לאקסל בכל עת",
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Button(
                onClick  = { onAddEntry(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("הוסף רשומה ראשונה", fontSize = 16.sp)
            }

            TextButton(onClick = onDismiss, Modifier.fillMaxWidth()) {
                Text("אחר כך")
            }
        }
    }
}
