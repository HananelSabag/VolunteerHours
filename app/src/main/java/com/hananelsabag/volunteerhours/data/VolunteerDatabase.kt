package com.hananelsabag.volunteerhours.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v2 → v3: added savedToList column to volunteers
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE volunteers ADD COLUMN savedToList INTEGER NOT NULL DEFAULT 1")
    }
}

@Database(
    entities = [Volunteer::class, VolunteerEntry::class],
    version = 3,
    exportSchema = false
)
abstract class VolunteerDatabase : RoomDatabase() {
    abstract fun volunteerDao(): VolunteerDao
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile private var INSTANCE: VolunteerDatabase? = null

        fun getInstance(context: Context): VolunteerDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, VolunteerDatabase::class.java, "volunteer_db")
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // safety net for unknown future migrations
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
