package com.example.fitnessrun

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationNavigationTest {
    @Test
    fun profileDataSurvivesNavigationWithoutForwardingCredentials() {
        val registration = Intent()
            .putExtra(RegistrationKeys.NAME, "Ana")
            .putExtra(RegistrationKeys.EMAIL, "ana@example.com")
            .putExtra(RegistrationKeys.ALLERGY, "Nueces")
            .putExtra(RegistrationKeys.FRUITS, "Manzana")
            .putExtra("password", "must-not-travel")
        val community = RegistrationKeys.copyTo(registration, Intent())
        val routine = RegistrationKeys.copyTo(community, Intent())
            .putExtra(SectionActivity.EXTRA_SECTION, "routine")
        val profile = RegistrationKeys.copyTo(routine, Intent())
        assertEquals("Ana", profile.getStringExtra(RegistrationKeys.NAME))
        assertEquals("ana@example.com", profile.getStringExtra(RegistrationKeys.EMAIL))
        assertEquals("Nueces", profile.getStringExtra(RegistrationKeys.ALLERGY))
        assertEquals("Manzana", profile.getStringExtra(RegistrationKeys.FRUITS))
        assertFalse(profile.hasExtra("password"))
        assertFalse(profile.hasExtra(SectionActivity.EXTRA_SECTION))
    }

    @Test
    fun entryWithoutRegistrationDoesNotInventProfileData() {
        val profile = RegistrationKeys.copyTo(Intent(), Intent())
        assertFalse(profile.hasExtra(RegistrationKeys.NAME))
        assertFalse(profile.hasExtra(RegistrationKeys.EMAIL))
    }
}
