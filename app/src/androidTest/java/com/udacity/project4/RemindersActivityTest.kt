package com.udacity.project4

import android.app.Application
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() { // Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { ServiceLocator.provideRemindersRepository(appContext) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addAndSaveNewValidReminder_newReminderItemDisplayingInList() {
        // Set initial state
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the "Add Reminder" FAB
        onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Fill out Title + Description
        onView(ViewMatchers.withId(R.id.reminderTitle)).perform(clearText(), typeText("TITLE1"))
        onView(ViewMatchers.withId(R.id.reminderDescription)).perform(clearText(), typeText("DESCRIPTION1"))

        // Click on the Select Location TextView
        onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())

        // Go back to the previous screen and manually save the location as we can't test Google Maps with Espresso
        onView(isRoot()).perform(ViewActions.pressBack())
        onView(ViewMatchers.withId(R.id.selectedLocation)).perform(setTextInTextView("LOCATION1"))

        // Click on Save Reminder
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Verify reminder is displayed on screen in the reminder list
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("DESCRIPTION1")).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun addAndSaveNewReminderWithoutLocation_toastErrorMessageDisplaying() {
        // Set initial state
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the "Add Reminder" FAB
        onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Fill out Title + Description
        onView(ViewMatchers.withId(R.id.reminderTitle)).perform(clearText(), typeText("TITLE1"))
        onView(ViewMatchers.withId(R.id.reminderDescription)).perform(clearText(), typeText("DESCRIPTION1"))

        // Click on Save Reminder without a Location
        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Verify that the Location error toast message appears
        onView(withText(R.string.select_location)).inRoot(withDecorView(not(getActivity(appContext)?.window?.decorView))).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun addAndSaveNewReminderWithoutTitle_toastErrorMessageDisplaying() {
        // Set initial state
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the "Add Reminder" FAB
        onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Fill location but not Title/Description
        onView(ViewMatchers.withId(R.id.selectedLocation)).perform(setTextInTextView("LOCATION1"))

        // Click on Save Reminder without a Location
        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Verify that the Location Snackbar error message appears
        onView(withText(R.string.err_enter_title)).inRoot(withDecorView(not(getActivity(appContext)?.window?.decorView))).check(matches(isDisplayed()))

        activityScenario.close()
    }

    private fun setTextInTextView(value: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return CoreMatchers.allOf(isDisplayed(), ViewMatchers.isAssignableFrom(
                    TextView::class.java))
            }

            override fun perform(uiController: UiController, view: View) {
                (view as TextView).text = value
            }

            override fun getDescription(): String {
                return "replace text"
            }
        }
    }

}
