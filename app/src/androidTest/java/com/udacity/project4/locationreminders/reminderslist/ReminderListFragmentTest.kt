package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeRemindersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    private lateinit var localDataSource: ReminderDataSource

    @Before
    fun initRepository() {
        localDataSource = FakeRemindersRepository()
        ServiceLocator.remindersDataSource = localDataSource
    }

    @After
    fun cleanupDb() = runTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun clickAddReminder_navigateToSaveReminderFragment() = runTest {
        // GIVEN - On the reminders list fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the Add Reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the Save Reminder fragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun clickReminder_navigateToDetailActivity() = runTest {
        // GIVEN - On the reminders list fragment with two reminders
        localDataSource.saveReminder(ReminderDTO("TITLE1", "DESCRIPTION1", "LOCATION1", 0.0, 0.0))
        localDataSource.saveReminder(ReminderDTO("TITLE2", "DESCRIPTION2", "LOCATION2", 1.0, 2.0))

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the first list item
        onView(withId(R.id.reminderssRecyclerView))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("TITLE1")), click()
            ))

        // THEN - Verify that we navigate to the Detail activity by checking if we see the Reminder and Location titles
        onView(withText("Reminder")).check(matches(isDisplayed()))
        onView(withText("Location")).check(matches(isDisplayed()))
    }

    @Test
    fun addReminders_remindersShowOnUI() = runTest {
        // GIVEN - On the reminders list fragment with two reminders
        localDataSource.saveReminder(ReminderDTO("TITLE1", "DESCRIPTION1", "LOCATION1", 0.0, 0.0))
        localDataSource.saveReminder(ReminderDTO("TITLE2", "DESCRIPTION2", "LOCATION2", 1.0, 2.0))

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - User should see the 2 reminders in the UI
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("DESCRIPTION1")).check(matches(isDisplayed()))
        onView(withText("LOCATION1")).check(matches(isDisplayed()))

        onView(withText("TITLE2")).check(matches(isDisplayed()))
        onView(withText("DESCRIPTION2")).check(matches(isDisplayed()))
        onView(withText("LOCATION2")).check(matches(isDisplayed()))

        // The "No Data" should also NOT be displayed
        onView(withText("No Data")).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun addNoReminders_showNoDataText() = runTest {
        // GIVEN - On the reminders list fragment with no reminders added yet
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // THEN - The No Data text should be shown
        onView(withText("No Data")).check(matches(isDisplayed()))
    }
}