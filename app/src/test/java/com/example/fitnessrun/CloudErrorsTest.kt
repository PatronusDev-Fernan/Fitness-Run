package com.example.fitnessrun

import java.util.concurrent.TimeoutException
import org.junit.Assert.assertEquals
import org.junit.Test

class CloudErrorsTest {
    @Test fun readTimeoutHasRecoverableMessage() {
        assertEquals(R.string.cloud_read_timeout, CloudErrors.message(TimeoutException()))
    }

    @Test fun wrappedReadTimeoutKeepsRecoverableMessage() {
        assertEquals(R.string.cloud_read_timeout, CloudErrors.message(Exception(TimeoutException())))
    }
}
