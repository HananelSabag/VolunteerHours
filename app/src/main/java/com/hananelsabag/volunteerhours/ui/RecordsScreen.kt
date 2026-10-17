package com.hananelsabag.volunteerhours.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hananelsabag.volunteerhours.data.VolunteerWithStats
import com.hananelsabag.volunteerhours.viewmodel.VolunteerViewModel
import kotlinx.coroutines.launch

private val Gold   = Color(0xFFFFC107)
private val Silver = Color(0xFF9E9E9E)
private val Bronze = Color(0xFFCD7F32)

// Shared avatar helpers (mirrors VolunteerDetailScreen, kept local to avoid coupling)
private val statAvatarPalette = listOf(
    Color(0xFF5C6BC0), Color(0xFF26A69A), Color(0xFFEF5350),
    Color(0xFFAB47BC), Color(0xFF29B6F6), Color(0xFF66BB6A),
    Color(0xFFFF7043), Color(0xFF8D6E63), Color(0xFF26C6DA),
    Color(0xFFD4E157)
)
private fun statAvatarColor(name: String) =
    statAvatarPalette[(name.hashCode() and 0x7FFFFFFF) % statAvatarPalette.size]
private fun statInitials(name: String) =
    name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")

@Composable
fun VolunteersScreen(
    vm: VolunteerViewModel,
    onVolunteerClick: (Long) -> Unit
) {
    val volunteersWithStats by vm.volunteersWithStats.collectAsState()
    val volunteers          by vm.volunteers.collectAsState()
    val totalHoursAll       by vm.totalHours.collectAsState()
    var savedPage           by rememberSaveable { mutableIntStateOf(0) }
    val pagerState          = rememberPagerState(initialPage = savedPage, pageCount = { 2 })
    LaunchedEffect(pagerState.currentPage) { savedPage = pagerState.currentPage }
    val scope      = rememberCoroutineScope()
    val tabTitles  = listOf("ניהול", "סטטיסטיקות")

    var showAddVolunteer by remember { mutableStateOf(false) }
    var showAddEntry     by remember { mutableStateOf(false) }

    Scaffold(
        // Fix: outer NavHost Modifier.padding(innerPadding) already consumed status-bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick        = { showAddEntry = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) { Icon(Icons.Default.AccessTime, contentDescription = "הוסף רשומה") }
                ExtendedFloatingActionButton(
                    onClick = { showAddVolunteer = true },
                    icon    = { Icon(Icons.Default.PersonAdd, null) },
                    text    = { Text("מתנדב חדש") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "מתנדבים",
                style    = MaterialTheme.typography.titleMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 2.dp)
            )
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                PillTabSelector(
                    selectedIndex = pagerState.currentPage,
                    tabs          = tabTitles,
                    modifier      = Modifier.fillMaxWidth(),
                    onTabSelected = { scope.launch { pagerState.animateScrollToPage(it) } }
                )
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0    -> ManagementTab(volunteersWithStats, onVolunteerClick)
                    else -> StatsTab(volunteersWithStats, totalHoursAll)
                }
            }
        }
    }

    if (showAddVolunteer) {
        AddVolunteerSheet(
            onDismiss = { showAddVolunteer = false },
            onConfirm = { name, phone -> vm.addVolunteer(name, phone); showAddVolunteer = false }
        )
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
}

// ── Management tab ────────────────────────────────────────────────────────────

@Composable
private fun ManagementTab(volunteers: List<VolunteerWithStats>, onVolunteerClick: (Long) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(volunteers, searchQuery) {
        if (searchQuery.isBlank()) volunteers
        else volunteers.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value           = searchQuery,
            onValueChange   = { searchQuery = it },
            placeholder     = { Text("חיפוש מתנדב...") },
            leadingIcon     = { Icon(Icons.Default.Search, null) },
            modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape           = MaterialTheme.shapes.extraLarge
        )
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (searchQuery.isBlank()) "אין מתנדבים עדיין\nלחץ + להוספה"
                    else "לא נמצא \"$searchQuery\"",
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { v ->
                    VolunteerCard(v, onClick = { onVolunteerClick(v.id) })
                }
            }
        }
    }
}

@Composable
private fun VolunteerCard(v: VolunteerWithStats, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(v.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${v.entryCount} רשומות", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (v.lastActivity != null)
                        Text("• ${formatDate(v.lastActivity)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    "${formatHours(v.totalHours)} שע׳",
                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ── Stats tab ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsTab(volunteers: List<VolunteerWithStats>, totalHoursAll: Float) {
    val totalHours   = totalHoursAll
    val totalEntries = volunteers.sumOf { it.entryCount }

    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier                = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment     = Alignment.CenterHorizontally,
                    verticalArrangement     = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "סה\"כ שעות",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                    )
                    Text(
                        formatHours(totalHours),
                        fontSize   = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        StatChip("${volunteers.size}", "מתנדבים")
                        Text("•", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f))
                        StatChip("$totalEntries", "רשומות")
                    }
                }
            }
        }

        if (volunteers.isNotEmpty()) {
            item {
                Text(
                    "דירוג מתנדבים",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(volunteers.size) { i ->
                val v          = volunteers[i]
                val rank       = i + 1
                val rankEmoji  = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> null }
                val rankColor  = when (rank) { 1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                val fraction   = if (totalHours > 0f) (v.totalHours / totalHours).coerceIn(0f, 1f) else 0f
                val avatarBg   = statAvatarColor(v.name)
                val onAvatar   = if (avatarBg.luminance() > 0.4f) Color(0xFF1A1A1A) else Color.White
                val initials   = statInitials(v.name)

                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Avatar circle
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, color = onAvatar, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Name row + hours
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    if (rankEmoji != null)
                                        Text(rankEmoji, fontSize = 16.sp)
                                    else
                                        Text(
                                            "#$rank",
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 13.sp,
                                            color      = rankColor
                                        )
                                    Text(v.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                }
                                Text(
                                    "${formatHours(v.totalHours)} שע׳",
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary,
                                    style      = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Full-width progress bar
                            LinearProgressIndicator(
                                progress  = { fraction },
                                modifier  = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color     = rankColor.copy(alpha = 0.85f),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Text(
                                "${v.entryCount} רשומות",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 64.dp), Alignment.Center) {
                    Text("אין נתונים עדיין", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimaryContainer,
            style      = MaterialTheme.typography.bodyMedium
        )
        Text(
            label,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
