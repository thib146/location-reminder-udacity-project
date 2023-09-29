package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.TestApplication
import com.udacity.project4.locationreminders.data.FakeRemindersRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalMatchers.*
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(application = TestApplication::class, sdk = [29])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveRemindersViewModel: SaveReminderViewModel
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

        saveRemindersViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersLocalRepository
        )
    }

    @Test
    fun validateAndSaveValidReminder_dataSourceUpdated() {
        // Given a new reminder to be saved
        val newReminder = ReminderDataItem("Title", "Description", "Location", 3.4, 5.6)

        // When the new reminder is saved
        saveRemindersViewModel.validateAndSaveReminder(newReminder)

        // Then the data source should be updated accordingly
        val reminderDatabase = remindersLocalRepository.remindersServiceData[newReminder.id]
        assertThat(reminderDatabase?.id, `is`(newReminder.id))
        assertThat(reminderDatabase?.title, `is`(newReminder.title))
        assertThat(reminderDatabase?.description, `is`(newReminder.description))
        assertThat(reminderDatabase?.location, `is`(newReminder.location))
        assertThat(reminderDatabase?.latitude, `is`(newReminder.latitude))
        assertThat(reminderDatabase?.longitude, `is`(newReminder.longitude))
    }

    @Test
    fun validateAndSaveNullTitleReminder_dataSourceNotUpdatedAndSnackbarShown() {
        // Given a new invalid reminder to be saved - with a null title
        val newReminder = ReminderDataItem(null, "Description", "Location", 3.4, 5.6)

        // When the new reminder is saved
        saveRemindersViewModel.validateAndSaveReminder(newReminder)

        // Then the data source should be NOT be updated  + the Title snackbar should be shown
        val reminderDatabase = remindersLocalRepository.remindersServiceData[newReminder.id]
        val snackbarTitleText: Int =  saveRemindersViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(reminderDatabase, `is`(nullValue()))
        assertThat(snackbarTitleText, `is`(R.string.err_enter_title))
    }

    @Test
    fun validateAndSaveNullLocationReminder_dataSourceNotUpdatedAndSnackbarShown() {
        // Given a new invalid reminder to be saved - with a null location
        val newReminder = ReminderDataItem("Title", "Description", null, 3.4, 5.6)

        // When the new reminder is saved
        saveRemindersViewModel.validateAndSaveReminder(newReminder)

        // Then the data source NOT should be updated + the Location snackbar should be shown
        val reminderDatabase = remindersLocalRepository.remindersServiceData[newReminder.id]
        val snackbarLocationText: Int =  saveRemindersViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(reminderDatabase, `is`(nullValue()))
        assertThat(snackbarLocationText, `is`(R.string.err_select_location))
    }

    @Test
    fun clearLiveDataObjects_allLiveDataObjectsNull() {
        // When the LiveData objects are cleared
        saveRemindersViewModel.onClear()

        // Then the LiveData objects should all be null
        assertThat(saveRemindersViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }
}