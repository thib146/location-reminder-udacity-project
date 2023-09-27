package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.TestApplication
import com.udacity.project4.locationreminders.data.FakeRemindersRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, sdk = [29])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var remindersLocalRepository: FakeRemindersRepository

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        remindersLocalRepository = FakeRemindersRepository()
        val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 0.0, 0.0)
        val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 1.0, 2.0)
        val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 5.0, 6.0)

        remindersLocalRepository.addReminders(reminder1, reminder2, reminder3)

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersLocalRepository
        )
    }

    @Test
    fun loadReminders_showLoadingTrueAndFalse() {
        // When loading all reminders
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        // Then assert that the loading indicator is showing during loading
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Then assert that the loading indicator is NOT showing when done
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_viewModelRemindersLiveDataObjectUpdated() {
        // When loading all reminders
        remindersListViewModel.loadReminders()

        // Then assert that the remindersList in the ViewModel has been updated correctly
        val reminderListViewModel = remindersListViewModel.remindersList.value
        if (!reminderListViewModel.isNullOrEmpty()) {
            for (reminderViewModel in reminderListViewModel) {
                val reminderDatabase = remindersLocalRepository.remindersServiceData[reminderViewModel.id]
                assertThat(reminderDatabase?.id, `is`(reminderViewModel.id))
                assertThat(reminderDatabase?.title, `is`(reminderViewModel.title))
                assertThat(reminderDatabase?.description, `is`(reminderViewModel.description))
                assertThat(reminderDatabase?.latitude, `is`(reminderViewModel.latitude))
                assertThat(reminderDatabase?.longitude, `is`(reminderViewModel.longitude))
                assertThat(reminderDatabase?.location, `is`(reminderViewModel.location))
            }
        }
    }
}
