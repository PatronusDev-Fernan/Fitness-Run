package com.example.fitnessrun

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DemoSessionTest {
    @After fun tearDown() { DemoSession.clear() }

    @Test fun startingNewDemoClearsPreviousUsersContentAndProgress() {
        DemoSession.beginExample()
        DemoSession.profile[RegistrationKeys.NAME] = "Otro usuario"
        DemoSession.posts.add(DemoSession.Post("Privado", "Solo yo", null, ""))
        DemoSession.progress.completedMeals.add(0)
        DemoSession.saved = true
        DemoSession.beginExample()
        assertEquals("Invitado", DemoSession.name)
        assertFalse(DemoSession.profile.containsKey(RegistrationKeys.EMAIL))
        assertFalse(DemoSession.profile.containsKey(RegistrationKeys.HEIGHT))
        assertTrue(DemoSession.posts.isEmpty())
        assertTrue(DemoSession.progress.completedMeals.isEmpty())
        assertFalse(DemoSession.saved)
    }

    @Test fun logoutClearsAllSessionData() {
        DemoSession.beginExample()
        DemoSession.clear()
        assertFalse(DemoSession.active)
        assertTrue(DemoSession.profile.isEmpty())
        assertTrue(DemoSession.posts.isEmpty())
    }
}

