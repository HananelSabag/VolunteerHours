package com.hananelsabag.volunteerhours.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "volunteer_entries",
    foreignKeys = [ForeignKey(
        entity = Volunteer::class,
        parentColumns = ["id"],
        childColumns = ["volunteerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("volunteerId")]
)
data class VolunteerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val volunteerId: Long,
    val date: String,
    val hours: Float
)
