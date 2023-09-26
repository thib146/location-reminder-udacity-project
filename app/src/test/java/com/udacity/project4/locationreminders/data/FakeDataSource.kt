package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.Error
import com.udacity.project4.locationreminders.data.dto.Result.Success

// FakeDataSource acts as a test double to the LocalDataSource
class FakeDataSource(var reminderItems: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminderItems?.let {
            return Success(ArrayList(it))
        }
        return Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderItems?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder: ReminderDTO? = reminderItems?.filter { it.id == id }?.get(0)
        reminder?.let {
            return Success(it)
        }
        return Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminderItems?.clear()
    }
}