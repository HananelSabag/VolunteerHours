package com.hananelsabag.volunteerhours.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("""
        SELECT e.id, e.volunteerId, v.name AS volunteerName, e.date, e.hours
        FROM volunteer_entries e
        JOIN volunteers v ON e.volunteerId = v.id
        ORDER BY e.date DESC, e.id DESC
    """)
    fun getAllWithVolunteer(): Flow<List<EntryWithVolunteer>>

    // Suspend variant for one-shot operations (CSV export)
    @Query("""
        SELECT e.id, e.volunteerId, v.name AS volunteerName, e.date, e.hours
        FROM volunteer_entries e
        JOIN volunteers v ON e.volunteerId = v.id
        ORDER BY v.name ASC, e.date DESC
    """)
    suspend fun getAllWithVolunteerList(): List<EntryWithVolunteer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: VolunteerEntry)

    @Delete
    suspend fun delete(entry: VolunteerEntry)

    @Query("SELECT COALESCE(SUM(hours), 0) FROM volunteer_entries")
    fun getTotalHours(): Flow<Float>

    @Query("SELECT COUNT(*) FROM volunteer_entries")
    fun getTotalEntryCount(): Flow<Int>
}
