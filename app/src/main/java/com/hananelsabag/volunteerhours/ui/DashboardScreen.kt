package com.hananelsabag.volunteerhours.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananelsabag.volunteerhours.data.EntryWithVolunteer
import com.hananelsabag.volunteerhours.viewmodel.VolunteerViewModel

@Composable
fun HomeScreen(
    vm: VolunteerViewModel,
    showOnboarding: Boolean   = false,
    onOnboardingSeen: () -> Unit = {}
) {
    val volunteersWithStats by vm.volunteersWithStats.collectAsState()
    val allEntries          by vm.allEntries.collectAsState()
    val totalHours          by vm.totalHours.collectAsState()
    val totalEntryCount     by vm.totalEntryCount.collectAsState()
    val volunteers          by vm.volunteers.collectAsState()
    val volunteerCount      = volunteersWithStats.size
    val avgHours            = if (volunteerCount > 0) totalHours / volunteerCount else 0f
    val topVolunteer        = volunteersWithStats.firstOrNull()

    var showAddEntry by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // U2: subtle page label, not dominant heading
                Text("בית", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(formatHours(totalHours), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("סה\"כ שעות התנדבות", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStat(Modifier.weight(1f), Icons.Default.Group,                  volunteerCount.toString(), "מתנדבים",
                        MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    QuickStat(Modifier.weight(1f), Icons.AutoMirrored.Filled.TrendingUp, formatHours(avgHours),     "ממוצע",
                        MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    QuickStat(Modifier.weight(1f), Icons.AutoMirrored.Filled.ListAlt,    totalEntryCount.toString(), "רשומות",
                        MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (topVolunteer != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), border = CardDefaults.outlinedCardBorder()) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
                            Text("🥇", fontSize = 28.sp)
                            Column(Modifier.weight(1f)) {
                                Text("מתנדב מוביל", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(topVolunteer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                                Text("${formatHours(topVolunteer.totalHours)} שע׳",
                                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            if (allEntries.isNotEmpty()) {
                item { Text("פעילות אחרונה", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(minOf(allEntries.size, 5)) { i -> RecentActivityCard(allEntries[i]) }
            } else {
                item { EmptyHomeState() }
            }
        }

        // F5: only "הוסף רשומה" FAB on home — "מתנדב חדש" moved to volunteers tab
        Box(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)) {
            ExtendedFloatingActionButton(
                onClick = { showAddEntry = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("הוסף רשומה") }
            )
        }
    }

    if (showAddEntry) {
        AddEntrySheet(
            savedVolunteers   = volunteers,
            onDismiss         = { showAddEntry = false },
            onConfirmExisting = { vid, date, hours -> vm.addEntry(vid, date, hours); showAddEntry = false },
            onConfirmFreeText = { name, phone, date, hours ->
                vm.addEntryFreeText(name, phone, date, hours)
                showAddEntry = false
            }
        )
    }

    // F4: one-time onboarding
    if (showOnboarding) {
        OnboardingSheet(
            onDismiss  = onOnboardingSeen,
            onAddEntry = { showAddEntry = true }
        )
    }
}

@Composable
private fun QuickStat(modifier: Modifier, icon: ImageVector, value: String, label: String, bg: Color, fg: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(20.dp), tint = fg)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = fg)
            Text(label, style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RecentActivityCard(entry: EntryWithVolunteer) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.volunteerName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(formatDate(entry.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Text("${formatHours(entry.hours)} שע׳",
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun EmptyHomeState() {
    Column(Modifier.fillMaxWidth().padding(top = 48.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.VolunteerActivism, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
        Text("אין נתונים עדיין", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("לחץ + להוסיף רשומה ראשונה", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), textAlign = TextAlign.Center)
    }
}
