package com.example.fitnessrun

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileEditorTest {
    @After fun clear() { DemoSession.clear() }

    @Test fun coachCanSaveBioAndKeepsBadge() {
        DemoSession.beginExample()
        DemoSession.profile[ProfileIdentity.TYPE] = ProfileIdentity.COACH
        val intent = DemoSession.fill(Intent(ApplicationProvider.getApplicationContext(), ProfileEditorActivity::class.java))
        ActivityScenario.launch<ProfileEditorActivity>(intent).use { scenario ->
            onView(withId(R.id.accountBadge)).check(matches(withText("@coach")))
            onView(withId(R.id.editName)).perform(scrollTo(), replaceText("Ana"), closeSoftKeyboard())
            onView(withId(R.id.editLastName)).perform(scrollTo(), replaceText("Pérez"), closeSoftKeyboard())
            onView(withId(R.id.editBio)).perform(scrollTo(), replaceText("Entrenamiento y hábitos"), closeSoftKeyboard())
            scenario.recreate()
            onView(withId(R.id.editBio)).check(matches(withText("Entrenamiento y hábitos")))
            onView(withId(R.id.saveButton)).perform(scrollTo(), click())
            assertEquals("Entrenamiento y hábitos", DemoSession.profile[ProfileIdentity.BIO])
            assertEquals(ProfileIdentity.COACH, ProfileIdentity.type(DemoSession.profile))
        }
    }

    @Test fun registrationReceivesAccountType() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), RegisterActivity::class.java)
            .putExtra(ProfileIdentity.TYPE, ProfileIdentity.COACH)
        ActivityScenario.launch<RegisterActivity>(intent).use {
            onView(withId(R.id.registerSubtitle)).check(matches(withText(R.string.register_coach_subtitle)))
        }
    }
}
