package com.hananelsabag.volunteerhours.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hananelsabag.volunteerhours.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VolunteerViewModel(app: Application) : AndroidViewModel(app) {
    private val db           = VolunteerDatabase.getInstance(app)
    private val volunteerDao = db.volunteerDao()
    private val entryDao     = db.entryDao()

    // ── Public state flows ──────────────────────────────────────────────────

    val volunteersWithStats = volunteerDao.getAllWithStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val volunteers = volunteerDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allVolunteerNames = volunteerDao.getAllNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savedVolunteerNames = volunteerDao.getSavedNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allEntries = entryDao.getAllWithVolunteer()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalHours = entryDao.getTotalHours()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    val totalEntryCount = entryDao.getTotalEntryCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Error channel ───────────────────────────────────────────────────────

    private val _error = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val error = _error.asSharedFlow()

    private fun launchDb(block: suspend () -> Unit) {
        viewModelScope.launch {
            try { block() }
            catch (e: Exception) {
                _error.tryEmit("שגיאה: ${e.localizedMessage ?: "שגיאה לא ידועה"}")
            }
        }
    }

    // ── Undo delete — queue so rapid deletions don't lose data ─────────────

    private val _deletedQueue   = ArrayDeque<VolunteerEntry>()
    private val _lastDeletedEntry = MutableStateFlow<VolunteerEntry?>(null)
    val lastDeletedEntry          = _lastDeletedEntry.asStateFlow()

    // ── Volunteer operations ────────────────────────────────────────────────

    fun addVolunteer(name: String, phone: String = "") = launchDb {
        val existing = volunteerDao.findByName(name.trim())
        if (existing != null) {
            volunteerDao.insert(existing.copy(savedToList = true, phone = phone.ifBlank { existing.phone }))
        } else {
            volunteerDao.insert(Volunteer(name = name.trim(), phone = phone.trim(), savedToList = true))
        }
    }

    fun deleteVolunteer(volunteer: Volunteer) = launchDb {
        volunteerDao.delete(volunteer)
    }

    // ── Entry operations ────────────────────────────────────────────────────

    fun addEntry(volunteerId: Long, date: String, hours: Float) = launchDb {
        entryDao.insert(VolunteerEntry(volunteerId = volunteerId, date = date, hours = hours))
    }

    // saveToList removed — new volunteers always save; phone added for new volunteer creation
    fun addEntryFreeText(name: String, phone: String = "", date: String, hours: Float) = launchDb {
        val trimmed  = name.trim()
        val existing = volunteerDao.findByName(trimmed)
        val volunteer = if (existing != null) {
            if (!existing.savedToList)
                volunteerDao.insert(existing.copy(savedToList = true))
            existing
        } else {
            val id = volunteerDao.insert(Volunteer(name = trimmed, phone = phone.trim(), savedToList = true))
            volunteerDao.getById(id) ?: error("Volunteer insert failed for id=$id")
        }
        entryDao.insert(VolunteerEntry(volunteerId = volunteer.id, date = date, hours = hours))
    }

    fun deleteEntry(entry: VolunteerEntry) {
        // FIX #2: queue-based undo — rapid deletes no longer lose data
        _deletedQueue.addLast(entry)
        _lastDeletedEntry.value = entry
        launchDb { entryDao.delete(entry) }
    }

    fun undoDeleteEntry() {
        val entry = _deletedQueue.removeLastOrNull() ?: return
        _lastDeletedEntry.value = _deletedQueue.lastOrNull()
        launchDb { entryDao.insert(entry.copy(id = 0)) }
    }

    fun clearLastDeletedEntry() {
        _deletedQueue.clear()
        _lastDeletedEntry.value = null
    }

    // ── Per-volunteer queries ───────────────────────────────────────────────

    fun getEntriesForVolunteer(id: Long) = volunteerDao.getEntriesForVolunteer(id)
    fun getTotalHoursForVolunteer(id: Long) = volunteerDao.getTotalHoursForVolunteer(id)

    // ── CSV Export ──────────────────────────────────────────────────────────

    // No Context param — uses getApplication() to avoid Activity context leak
    fun exportToCsv(onDone: (Uri?) -> Unit) {
        val ctx = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entries = entryDao.getAllWithVolunteerList()
                val csv = buildString {
                    append("﻿") // BOM — Excel recognises Hebrew UTF-8
                    appendLine("שם מתנדב,תאריך,שעות")
                    entries.forEach {
                        // Escape quotes inside field; wrap name in quotes (may contain commas)
                        val name = "\"${it.volunteerName.replace("\"", "\"\"")}\""
                        appendLine("$name,${it.date},${it.hours}")
                    }
                }
                val file = File(ctx.cacheDir, "volunteer_hours_export.csv")
                file.writeText(csv, Charsets.UTF_8)
                val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
                withContext(Dispatchers.Main) { onDone(uri) }
            } catch (e: Exception) {
                _error.tryEmit("שגיאת ייצוא: ${e.localizedMessage}")
                withContext(Dispatchers.Main) { onDone(null) }
            }
        }
    }
}
