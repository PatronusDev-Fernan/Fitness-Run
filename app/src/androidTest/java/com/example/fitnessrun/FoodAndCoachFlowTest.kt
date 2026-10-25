package com.example.fitnessrun

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodAndCoachFlowTest {
    @Test fun registeredAllergyFiltersFruitAndSelectionSurvivesRecreation() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), FoodPreferencesActivity::class.java)
            .putExtra(RegistrationKeys.ALLERGY, "Manzana")
        ActivityScenario.launch<FoodPreferencesActivity>(intent).use { scenario ->
            onView(withId(R.id.selectFruitsButton)).perform(scrollTo(), click())
            onView(withText("Manzana")).check(doesNotExist())
            onData(equalTo("Pera")).perform(click())
            onView(withId(android.R.id.button1)).perform(click())
            scenario.recreate()
            onView(withId(R.id.fruitChipGroup)).check(matches(hasDescendant(withText("Pera"))))
            onView(withId(R.id.dislikesEditText)).perform(scrollTo(), replaceText("Pera"), closeSoftKeyboard())
            onView(withId(R.id.fruitChipGroup)).check(matches(not(hasDescendant(withText("Pera")))))
        }
    }

    @Test fun coachOpensFromCommunityAndFollowCanBeReversed() {
        ActivityScenario.launch(LoginActivity::class.java).use {
            onView(withId(R.id.googleLoginButton)).perform(scrollTo(), click())
            onView(withId(R.id.viewCoachButton)).perform(scrollTo(), click())
            onView(withId(R.id.categoryText)).check(matches(withText(R.string.coach_badge)))
            onView(withId(R.id.primaryButton)).perform(click()).check(matches(withText(R.string.coach_unfollow)))
            onView(withId(R.id.primaryButton)).perform(click()).check(matches(withText(R.string.coach_follow)))
        }
    }
}
