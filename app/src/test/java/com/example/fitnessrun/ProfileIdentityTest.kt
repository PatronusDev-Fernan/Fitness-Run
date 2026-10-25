package com.example.fitnessrun

import org.junit.Assert.*
import org.junit.Test

class ProfileIdentityTest {
    @Test fun legacyProfilesRemainUsers() {
        assertEquals(ProfileIdentity.USER, ProfileIdentity.type(emptyMap()))
        assertTrue(ProfileIdentity.matches(emptyMap(), ProfileIdentity.USER))
        assertFalse(ProfileIdentity.matches(emptyMap(), ProfileIdentity.COACH))
    }
    @Test fun coachBadgeDoesNotChangeLoginEmail() {
        val profile = mapOf(ProfileIdentity.TYPE to ProfileIdentity.COACH, RegistrationKeys.EMAIL to "ana@example.com")
        assertEquals("@coach", ProfileIdentity.badge(profile))
        assertTrue(ProfileIdentity.matches(profile, ProfileIdentity.COACH))
        assertEquals("ana@example.com", profile[RegistrationKeys.EMAIL])
    }
    @Test fun editingBioCannotEscalateAccountType() {
        val original = mapOf(ProfileIdentity.TYPE to ProfileIdentity.USER)
        val updated = ProfileFields.editIdentity(original, mapOf(ProfileIdentity.TYPE to ProfileIdentity.COACH, ProfileIdentity.BIO to " Mi descripción "))
        assertEquals(ProfileIdentity.USER, updated[ProfileIdentity.TYPE])
        assertEquals("Mi descripción", updated[ProfileIdentity.BIO])
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsLongBio() {
        ProfileFields.editIdentity(emptyMap(), mapOf(ProfileIdentity.BIO to "x".repeat(241)))
    }
    @Test fun acceptsEmptyPhotoToRemoveIt() {
        val updated = ProfileFields.editIdentity(mapOf(ProfileIdentity.PHOTO to "/9j/AAAA"), mapOf(ProfileIdentity.PHOTO to ""))
        assertEquals("", updated[ProfileIdentity.PHOTO])
    }
    @Test fun rejectsExternalPhotoLocationsAndOversizedPayloads() {
        assertFalse(ProfileIdentity.validPhoto("https://example.com/photo.jpg"))
        assertFalse(ProfileIdentity.validPhoto("content://private/file"))
        assertFalse(ProfileIdentity.validPhoto("/9j/" + "A".repeat(60000)))
        assertFalse(ProfileIdentity.validPhoto("/9j/AAA\n"))
    }
    @Test fun onlyApprovedFieldsSurviveSanitizing() {
        val clean = ProfileFields.sanitize(mapOf(ProfileIdentity.TYPE to "coach", ProfileIdentity.BIO to "Entrenamiento", "verified" to "true", "admin" to "true"))
        assertEquals(setOf(ProfileIdentity.TYPE, ProfileIdentity.BIO), clean.keys)
    }
}
