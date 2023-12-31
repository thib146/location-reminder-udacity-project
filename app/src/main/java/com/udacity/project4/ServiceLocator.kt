package com.udacity.project4

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.InternalCoroutinesApi

object ServiceLocator {
    private val lock = Any()

    private var database: RemindersDatabase? = null

    @Volatile
    var remindersDataSource: ReminderDataSource? = null
        @VisibleForTesting set

    @OptIn(InternalCoroutinesApi::class)
    fun provideRemindersRepository(context: Context): ReminderDataSource {
        synchronized(this) {
            return remindersDataSource ?: createRemindersRepository(context)
        }
    }

    private fun createRemindersRepository(context: Context): ReminderDataSource {
        val database = database ?: createDataBase(context)
        val newRepo = RemindersLocalRepository(database.reminderDao())
        remindersDataSource = newRepo
        return newRepo
    }

    private fun createDataBase(context: Context): RemindersDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "Reminders.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            // Clear all data to avoid test pollution
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            remindersDataSource = null
        }
    }
}