package com.example.fitnessrun

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CloudReadsTest {
    @Test fun completedReadReturnsItsValue() {
        assertEquals("profile", Tasks.await(CloudReads.bounded(Tasks.forResult("profile")), 2, TimeUnit.SECONDS))
    }

    @Test fun timeoutDiscardsLateReadWithoutCancellingUnderlyingTask() {
        val source = TaskCompletionSource<String>()
        val read = CloudReads.withDeadline(source.task, 50)
        try {
            Tasks.await(read, 5, TimeUnit.SECONDS)
            fail("An unresolved read must time out")
        } catch (error: ExecutionException) {
            assertTrue(error.cause is TimeoutException)
        }
        source.setResult("late profile")
        assertTrue(source.task.isSuccessful)
        assertFalse(read.isSuccessful)
        assertTrue(read.exception is TimeoutException)
    }
}
