package dev.locationapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [LocationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        // migration from version 1 (AES) to version 2 (RSA)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS locations")
                database.execSQL("""
                    CREATE TABLE locations (
                        id TEXT PRIMARY KEY NOT NULL,
                        encrypted_latitude BLOB NOT NULL,
                        encrypted_longitude BLOB NOT NULL,
                        accuracy REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        provider TEXT NOT NULL
                    )
                """)
            }
        }
    }
}
