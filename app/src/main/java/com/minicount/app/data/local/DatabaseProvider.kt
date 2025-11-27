package com.minicount.app.data.local

import android.content.Context
import androidx.room.Room

/**
 * Singleton provider for MiniCountDatabase
 * Ensures only one database instance exists throughout the app lifecycle
 * Used by widgets and other components that can't use Hilt injection
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: MiniCountDatabase? = null

    /**
     * Get or create the database instance
     * Thread-safe with double-checked locking
     */
    fun getDatabase(context: Context): MiniCountDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                MiniCountDatabase::class.java,
                "minicount_db"
            )
                .addMigrations(
                    com.minicount.app.di.AppModule.MIGRATION_1_2,
                    com.minicount.app.di.AppModule.MIGRATION_2_3,
                    com.minicount.app.di.AppModule.MIGRATION_3_4
                )
                .build()
            INSTANCE = instance
            instance
        }
    }

    /**
     * Clear the database instance (for testing or cleanup)
     */
    @Synchronized
    fun clearInstance() {
        INSTANCE?.close()
        INSTANCE = null
    }
}
