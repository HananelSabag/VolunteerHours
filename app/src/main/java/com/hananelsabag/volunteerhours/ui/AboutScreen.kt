package com.hananelsabag.volunteerhours.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.hananelsabag.volunteerhours.data.SettingsRepository
import com.hananelsabag.volunteerhours.notification.ReminderWorker
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun SettingsScreen(
    settingsRepo:  SettingsRepository,
    isExporting:   Boolean = false,
    onExportClick: () -> Unit = {}
) {
    val scope        = rememberCoroutineScope()
    val context      = LocalContext.current
    val theme        by settingsRepo.themeFlow.collectAsState(initial = "system")
    val notifEnabled by settingsRepo.notificationsFlow.collectAsState(initial = false)

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { settingsRepo.setNotifications(true) }
            scheduleReminderAt9AM(context)
        } else {
            // FIX Low #17: explain why notifications won't work
            Toast.makeText(context, "יש לאשר הרשאה בהגדרות כדי לקבל תזכורות", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("הגדרות", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // ── Theme ────────────────────────────────────────────────────────
        SectionLabel("ממשק")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ערכת נושא", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("light" to "בהיר", "system" to "מערכת", "dark" to "כהה").forEach { (key, label) ->
                        FilterChip(
                            selected = theme == key,
                            onClick  = { scope.launch { settingsRepo.setTheme(key) } },
                            label    = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Notifications ────────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        SectionLabel("התראות")
        Card(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("תזכורות יומיות", fontWeight = FontWeight.Medium)
                    Text("תזכורת בכל בוקר בשעה 09:00", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = notifEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                scope.launch { settingsRepo.setNotifications(true) }
                                scheduleReminderAt9AM(context)
                            }
                        } else {
                            scope.launch { settingsRepo.setNotifications(false) }
                            WorkManager.getInstance(context).cancelUniqueWork("daily_reminder")
                        }
                    }
                )
            }
        }

        // ── Data ─────────────────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        SectionLabel("נתונים")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ייצוא נתונים", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Text(
                    "ייצוא כל הרשומות לקובץ CSV — ניתן לפתוח ב-Excel עם תמיכה בעברית",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick  = onExportClick,
                    enabled  = !isExporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("מייצא...")
                    } else {
                        Icon(Icons.Default.FileDownload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ייצוא CSV")
                    }
                }
            }
        }

        // ── About ─────────────────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        SectionLabel("אודות")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(Icons.Default.Person,  "מפתח",     "חננאל סבג")
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(Icons.Default.Info,    "גרסה",     "1.0")
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(Icons.Default.Code,    "פלטפורמה", "Android · Material3 · Room")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// FIX: schedule at exactly 09:00 the next morning, not "24h from now"
private fun scheduleReminderAt9AM(context: android.content.Context) {
    val now    = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
    }
    val initialDelayMs = target.timeInMillis - now.timeInMillis

    val work = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
        .build()

    // REPLACE so re-enabling recalculates the correct next 9 AM
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_reminder", ExistingPeriodicWorkPolicy.REPLACE, work
    )
}
