package com.hananelsabag.volunteerhours.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.sp
import com.hananelsabag.volunteerhours.data.Volunteer
import com.hananelsabag.volunteerhours.data.VolunteerEntry
import com.hananelsabag.volunteerhours.viewmodel.VolunteerViewModel

// Deterministic avatar color from volunteer name
private val avatarPalette = listOf(
    Color(0xFF5C6BC0), Color(0xFF26A69A), Color(0xFFEF5350),
    Color(0xFFAB47BC), Color(0xFF29B6F6), Color(0xFF66BB6A),
    Color(0xFFFF7043), Color(0xFF8D6E63), Color(0xFF26C6DA),
    Color(0xFFD4E157)
)
private fun avatarColor(name: String) =
    avatarPalette[(name.hashCode() and 0x7FFFFFFF) % avatarPalette.size]

private fun initials(name: String) =
    name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDetailScreen(
    volunteerId: Long,
    vm: VolunteerViewModel,
    onBack: () -> Unit
) {
    val volunteers   by vm.volunteers.collectAsState()
    val volunteer    = volunteers.find { it.id == volunteerId }
    val entries      by remember(volunteerId) { vm.getEntriesForVolunteer(volunteerId) }.collectAsState(initial = emptyList())
    val totalHours   by remember(volunteerId) { vm.getTotalHoursForVolunteer(volunteerId) }.collectAsState(initial = 0f)
    val lastDeleted  by vm.lastDeletedEntry.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    var showAddEntry     by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(volunteer, volunteers) {
        if (volunteers.isNotEmpty() && volunteer == null) onBack()
    }

    LaunchedEffect(lastDeleted) {
        if (lastDeleted != null && lastDeleted!!.volunteerId == volunteerId) {
            val result = snackbarHost.showSnackbar("רשומה נמחקה", actionLabel = "ביטול", duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) vm.undoDeleteEntry() else vm.clearLastDeletedEntry()
        }
    }

    if (showDeleteDialog && volunteer != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("מחיקת מתנדב") },
            text  = { Text("האם למחוק את ${volunteer.name}?\nכל הרשומות שלו ימחקו ולא ניתן לשחזר.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.deleteVolunteer(volunteer); showDeleteDialog = false },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("מחק") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("ביטול") } }
        )
    }

    Scaffold(
        // Fix: outer NavHost's Modifier.padding(innerPadding) already consumed status-bar insets.
        // Setting these to zero prevents the inner Scaffold from counting them a second time.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        volunteer?.name ?: "",
                        style    = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                windowInsets   = WindowInsets(0, 0, 0, 0),
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "חזור") } },
                actions = {
                    if (volunteer != null)
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "מחק", tint = MaterialTheme.colorScheme.error)
                        }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            if (volunteer != null)
                ExtendedFloatingActionButton(
                    onClick = { showAddEntry = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("הוסף רשומה") }
                )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // ── Hero header ──────────────────────────────────────────────────
            item {
                volunteer?.let { v ->
                    VolunteerHeroHeader(volunteer = v, totalHours = totalHours, entryCount = entries.size)
                }
            }

            // ── Records ──────────────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Text("רשומות", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (entries.isNotEmpty())
                        Text("${entries.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (entries.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), Alignment.Center) {
                        Text("אין רשומות עדיין\nלחץ + להוספה", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        SwipeDeleteEntry(entry = entry, onDelete = { vm.deleteEntry(entry) })
                    }
                }
            }
        }
    }

    if (showAddEntry && volunteer != null) {
        AddEntrySheet(
            preselectedId   = volunteerId,
            preselectedName = volunteer.name,
            onDismiss       = { showAddEntry = false },
            onConfirmExisting = { vid, date, hours -> vm.addEntry(vid, date, hours); showAddEntry = false }
        )
    }
}

@Composable
private fun VolunteerHeroHeader(volunteer: Volunteer, totalHours: Float, entryCount: Int) {
    val color    = avatarColor(volunteer.name)
    val initials = initials(volunteer.name)
    val onColor  = if (color.luminance() > 0.4f) Color(0xFF1A1A1A) else Color.White

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle
        Box(
            Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initials,
                color = onColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            volunteer.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (volunteer.phone.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Phone, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(volunteer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Stats row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatPill(Modifier.weight(1f), formatHours(totalHours), "שעות", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            StatPill(Modifier.weight(1f), entryCount.toString(), "רשומות", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun StatPill(modifier: Modifier, value: String, label: String, bg: Color, fg: Color) {
    Surface(modifier = modifier, color = bg, shape = MaterialTheme.shapes.large) {
        Column(
            Modifier.padding(vertical = 14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = fg)
            Text(label, style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.7f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeDeleteEntry(entry: VolunteerEntry, onDelete: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { v ->
            if (v == SwipeToDismissBoxValue.EndToStart) { visible = false; onDelete(); true } else false
        }
    )

    AnimatedVisibility(visible = visible, exit = fadeOut(tween(250)) + shrinkVertically(tween(250))) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(
                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                        MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                    label = "swipe_bg"
                )
                Box(Modifier.fillMaxSize().background(color, MaterialTheme.shapes.medium).padding(end = 20.dp), Alignment.CenterEnd) {
                    Icon(Icons.Default.Delete, "מחק", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            },
            enableDismissFromStartToEnd = false
        ) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text(formatDate(entry.date), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                        Text(
                            "${formatHours(entry.hours)} שע׳",
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
