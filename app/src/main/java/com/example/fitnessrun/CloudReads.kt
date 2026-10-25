package com.example.fitnessrun

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit

/** Bound reads only: a timeout must never imply that a pending write was cancelled. */
object CloudReads {
    fun <T> bounded(task: Task<T>): Task<T> = withDeadline(task, 20_000)

    internal fun <T> withDeadline(task: Task<T>, milliseconds: Long): Task<T> =
        Tasks.withTimeout(task, milliseconds, TimeUnit.MILLISECONDS)
}
