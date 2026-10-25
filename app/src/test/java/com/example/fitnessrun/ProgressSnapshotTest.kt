package com.example.fitnessrun

import org.junit.Assert.*
import org.junit.Test

class ProgressSnapshotTest {
    private fun sample() = ProgressSnapshot("2026-10-06", setOf(0), setOf(0, 2), mapOf(0 to setOf(0, 1, 2)), "Descanso", "Sin seleccionar")
    @Test fun restoresIndependentCopies() {
        val snapshot = sample().validate()
        val progress = DemoProgress()
        progress.restore(snapshot)
        progress.exerciseChecks[0]!!.clear()
        assertEquals(setOf(0, 1, 2), snapshot.exerciseChecks[0])
        assertEquals(50, progress.mealPercent)
    }
    @Test fun restorationReplacesPreviousAccountState() {
        val progress = DemoProgress()
        progress.restore(sample())
        progress.restore(sample().copy(completedRoutines = emptySet(), completedMeals = emptySet(), exerciseChecks = emptyMap()))
        assertEquals(0, progress.routinePercent)
        assertEquals(0, progress.mealPercent)
        assertTrue(progress.exerciseChecks.isEmpty())
    }
    @Test fun rejectsIncompleteRewardAndUnknownMeal() {
        for (snapshot in listOf(sample().copy(exerciseChecks = emptyMap()), sample().copy(completedMeals = setOf(4)))) {
            assertThrows(IllegalArgumentException::class.java) { snapshot.validate() }
        }
    }
    @Test fun rejectsUnknownSelectionsAndDatePaths() {
        for (snapshot in listOf(sample().copy(weekendFood = "unknown"), sample().copy(date = "../other"))) {
            assertThrows(IllegalArgumentException::class.java) { snapshot.validate() }
        }
    }
    @Test fun nextDayKeepsWeeklyRoutineButResetsMeals() {
        val next = sample().forDate("2026-10-07")
        assertEquals(setOf(0), next.completedRoutines)
        assertEquals(sample().exerciseChecks, next.exerciseChecks)
        assertTrue(next.completedMeals.isEmpty())
    }
    @Test fun mondayStartsNewWeek() {
        val next = sample().forDate("2026-10-12")
        assertTrue(next.completedRoutines.isEmpty())
        assertTrue(next.exerciseChecks.isEmpty())
        assertEquals("Sin seleccionar", next.weekendRoutine)
    }
    @Test fun handlesYearBoundaryAndInvalidCalendarDate() {
        assertEquals("2025-12-29", ProgressSnapshot.weekStart("2026-01-01"))
        assertThrows(IllegalArgumentException::class.java) { sample().copy(date = "2026-02-30").validate() }
    }
}
