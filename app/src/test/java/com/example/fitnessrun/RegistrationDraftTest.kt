package com.example.fitnessrun

import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class RegistrationDraftTest {
    @After fun cleanup() { RegistrationDraft.clear() }
    @Test fun passwordCanOnlyBeConsumedOnceByItsEmail() {
        RegistrationDraft.set("ana@example.com", "example-password")
        assertEquals("example-password", RegistrationDraft.take("ana@example.com"))
        assertNull(RegistrationDraft.take("ana@example.com"))
    }
    @Test fun differentEmailCannotReceivePreviousCredential() {
        RegistrationDraft.set("ana@example.com", "example-password")
        assertNull(RegistrationDraft.take("other@example.com"))
        assertNull(RegistrationDraft.take("ana@example.com"))
    }
}
