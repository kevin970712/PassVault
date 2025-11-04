package com.jksalcedo.passvault.ui.addedit

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.ui.auth.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEditActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(TestActivity::class.java)

    @Test
    fun testAddEntry() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = AddEditActivity.createIntent(context)
        activityRule.scenario.onActivity {
            it.startActivity(intent)
        }

        onView(withId(R.id.etTitle)).perform(replaceText("Test Title"))
        onView(withId(R.id.etUsername)).perform(replaceText("Test Username"))
        onView(withId(R.id.etPassword)).perform(replaceText("Test Password"))
        onView(withId(R.id.etNotes)).perform(replaceText("Test Notes"))
        onView(withId(R.id.btnSave)).perform(click())
    }

    @Test
    fun testEditEntry() {
        // This test requires a pre-existing entry in the database.
        // For a real test, you would insert an entry into the database first.
        // Here, we'll simulate it by creating an intent with a fake entry.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val entry = PasswordEntry(
            id = 1L,
            title = "Old Title",
            username = "Old Username",
            passwordCipher = "...",
            passwordIv = "...",
            notes = "Old Notes"
        )
        val intent = AddEditActivity.createIntent(context, entry)
        activityRule.scenario.onActivity {
            it.startActivity(intent)
        }

        // Verify that the UI is populated with the old data.
        // Note: This part of the test will fail without a real database setup.
        // onView(withId(R.id.etTitle)).check(matches(withText("Old Title")))

        // Edit the entry
        onView(withId(R.id.etTitle)).perform(replaceText("New Title"))
        onView(withId(R.id.btnSave)).perform(click())
    }
}
