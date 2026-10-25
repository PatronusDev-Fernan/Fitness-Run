package com.example.fitnessrun

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoFlowTest {
    @Test fun demoConnectsPostsRoutineAchievementsProfileAndLogout() {
        ActivityScenario.launch(LoginActivity::class.java).use {
            onView(withId(R.id.googleLoginButton)).perform(scrollTo(), click())
            onView(withId(R.id.greetingText)).check(matches(withText("¡Hola, Invitado!")))
            onView(withId(R.id.progressButton)).perform(scrollTo(), click())
            onView(withId(R.id.postEditText)).perform(replaceText("Mi avance de prueba"), closeSoftKeyboard())
            onView(withId(R.id.publishButton)).perform(scrollTo(), click())
            onView(withText("Mi avance de prueba")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.nav_routine)).perform(click())
            onView(withId(R.id.cardOneButton)).perform(scrollTo(), click())
            listOf("Lunes ·", "Martes ·", "Miércoles ·").forEach { step ->
                onView(withText(startsWith(step))).perform(scrollTo(), click())
            }
            onView(withId(R.id.primaryButton)).perform(click()).check(matches(not(isEnabled())))
            onView(withId(R.id.backButton)).perform(scrollTo(), click())
            onView(withId(R.id.summaryDetailText)).check(matches(withText("1 de 2 bloques semanales completados")))
            onView(withId(R.id.nav_achievements)).perform(click())
            onView(withId(R.id.cardOneButton)).check(matches(withText(R.string.demo_completed)))
            onView(withId(R.id.summaryButton)).perform(scrollTo(), click())
            onView(withId(R.id.titleText)).check(matches(withText(R.string.demo_achievements_title)))
            onView(withId(R.id.backButton)).perform(scrollTo(), click())

            onView(withId(R.id.nav_food)).perform(click())
            onView(withId(R.id.summaryButton)).perform(scrollTo(), click())
            onView(withText(startsWith("Desayuno ·"))).perform(scrollTo(), click())
            onView(withId(R.id.primaryButton)).perform(click())
            onView(withId(R.id.summaryDetailText)).check(matches(withText("1 de 4 comidas marcadas")))

            onView(withId(R.id.nav_profile)).perform(click())
            onView(withId(R.id.summaryButton)).perform(scrollTo(), click())
            onView(withId(R.id.editName)).perform(scrollTo(), replaceText("Ana"), closeSoftKeyboard())
            onView(withId(R.id.editLastName)).perform(scrollTo(), replaceText("Pérez"), closeSoftKeyboard())
            onView(withId(R.id.saveButton)).perform(scrollTo(), click())
            onView(withId(R.id.summaryTitleText)).check(matches(withText("Ana Pérez")))
            onView(withId(R.id.nav_community)).perform(click())
            onView(withId(R.id.greetingText)).check(matches(withText("¡Hola, Ana!")))
            onView(withId(R.id.searchEditText)).perform(replaceText("Mi avance de prueba"), closeSoftKeyboard())
            onView(withId(R.id.samplePostCard)).check(matches(not(isDisplayed())))
            onView(withId(R.id.nav_profile)).perform(click())
            onView(withId(R.id.exitDemoButton)).perform(scrollTo(), click())
            onView(withId(android.R.id.button1)).perform(click())
            onView(withId(R.id.googleLoginButton)).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }
}





