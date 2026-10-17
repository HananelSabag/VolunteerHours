package com.hananelsabag.volunteerhours.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VolunteerDao {

    @Query("""
        SELECT v.id, v.name, v.phone, v.createdAt,
               COALESCE(SUM(e.hours), 0) AS totalHours,
               COUNT(e.id) AS entryCount,
               MAX(e.date) AS lastActivity
        FROM volunteers v
        LEFT JOIN volunteer_entries e ON v.id = e.volunteerId
        WHERE v.savedToList = 1
        GROUP BY v.id
        ORDER BY totalHours DESC
    """)
    fun getAllWithStats(): Flow<List<VolunteerWithStats>>

    @Query("SELECT * FROM volunteers WHERE savedToList = 1 ORDER BY name ASC")
    fun getAll(): Flow<List<Volunteer>>

    @Query("SELECT DISTINCT name FROM volunteers ORDER BY name ASC")
    fun getAllNames(): Flow<List<String>>

    @Query("SELECT name FROM volunteers WHERE savedToList = 1 ORDER BY name ASC")
    fun getSavedNames(): Flow<List<String>>

    @Query("SELECT * FROM volunteers WHERE id = :id")
    suspend fun getById(id: Long): Volunteer?

    // Case-insensitive, trim-safe name match
    @Query("SELECT * FROM volunteers WHERE TRIM(LOWER(name)) = TRIM(LOWER(:name)) LIMIT 1")
    suspend fun findByName(name: String): Volunteer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(volunteer: Volunteer): Long

    @Delete
    suspend fun delete(volunteer: Volunteer)

    @Query("SELECT * FROM volunteer_entries WHERE volunteerId = :id ORDER BY date DESC, rowid DESC")
    fun getEntriesForVolunteer(id: Long): Flow<List<VolunteerEntry>>

    @Query("SELECT COALESCE(SUM(hours), 0) FROM volunteer_entries WHERE volunteerId = :id")
    fun getTotalHoursForVolunteer(id: Long): Flow<Float>
}
