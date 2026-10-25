package com.example.fitnessrun

import org.junit.Assert.*
import org.junit.Test

class DemoProgressTest {
    @Test fun rejectsUnknownRoutineAndWrongIndices() {
        val progress = DemoProgress()
        progress.exerciseChecks[0] = mutableSetOf(1, 2, 3)
        assertFalse(progress.completeRoutine(0))
        progress.exerciseChecks[2] = mutableSetOf(0, 1, 2, 3)
        assertFalse(progress.completeRoutine(2))
    }
    @Test fun incompleteRoutineDoesNotUnlockAchievement() {
        val progress = DemoProgress()
        progress.exerciseChecks[0] = mutableSetOf(0, 1)
        assertFalse(progress.completeRoutine(0))
        assertEquals(0, progress.routinePercent)
    }

    @Test fun completingSameRoutineTwiceDoesNotDuplicateProgress() {
        val progress = DemoProgress()
        progress.exerciseChecks[0] = mutableSetOf(0, 1, 2)
        assertTrue(progress.completeRoutine(0))
        assertFalse(progress.completeRoutine(0))
        assertEquals(1, progress.completedRoutines.size)
    }

    @Test fun allRoutinesReachOneHundredPercent() {
        val progress = DemoProgress()
        for (id in 0 until DemoProgress.ROUTINE_COUNT) {
            progress.exerciseChecks[id] = (0 until DemoProgress.requiredSteps(id)).toMutableSet()
            assertTrue(progress.completeRoutine(id))
        }
        assertEquals(100, progress.routinePercent)
    }

    @Test fun uncheckingMealUpdatesProgress() {
        val progress = DemoProgress()
        progress.completedMeals.addAll(0..3)
        assertEquals(100, progress.mealPercent)
        progress.completedMeals.remove(1)
        assertEquals(75, progress.mealPercent)
    }
}

