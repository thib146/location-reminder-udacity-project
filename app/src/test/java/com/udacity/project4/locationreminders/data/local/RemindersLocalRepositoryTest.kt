package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RemindersLocalRepositoryTest {
    private val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 0.0, 0.0)
    private val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 1.0, 3.0)
    private val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 6.0, 8.0)
    private val localReminders = listOf(reminder1, reminder2).sortedBy { it.id }
    private val newReminders = listOf(reminder3).sortedBy { it.id }

    private lateinit var database: RemindersDatabase

    // Class under test
    private lateinit var remindersRepository: RemindersLocalRepository

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).build()

        remindersRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @Test
    fun getReminders_requestsAllRemindersFromLocalDataSource() = runTest {
        val reminders = remindersRepository.getReminders() as Result.Success

        assertThat(reminders.data, IsEqual(localReminders))
    }

}