package com.example.fitnessrun

import org.junit.Assert.*
import org.junit.Test

class ProfileFieldsTest {
    @Test fun excludesCredentialsAndUnexpectedServerFields() {
        val clean = ProfileFields.sanitize(mapOf(
            RegistrationKeys.NAME to "Ana", "password" to "secret", "role" to "admin",
            RegistrationKeys.WEIGHT to 70, RegistrationKeys.EMAIL to "ana@example.com"
        ))
        assertEquals(mapOf(RegistrationKeys.NAME to "Ana", RegistrationKeys.EMAIL to "ana@example.com"), clean)
    }
    @Test fun editingIdentityPreservesHealthAndCannotChangeAccountEmail() {
        val original = mapOf(RegistrationKeys.NAME to "Ana", RegistrationKeys.ALLERGY to "Nueces",
            RegistrationKeys.EMAIL to "ana@example.com")
        val updated = ProfileFields.editIdentity(original, mapOf(RegistrationKeys.NAME to " Ana María ",
            RegistrationKeys.EMAIL to "other@example.com", RegistrationKeys.ALLERGY to "Ninguna"))
        assertEquals("Ana María", updated[RegistrationKeys.NAME])
        assertEquals("Nueces", updated[RegistrationKeys.ALLERGY])
        assertEquals("ana@example.com", updated[RegistrationKeys.EMAIL])
    }
}
