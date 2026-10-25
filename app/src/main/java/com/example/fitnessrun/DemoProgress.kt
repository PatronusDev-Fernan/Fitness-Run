package com.example.fitnessrun

/** Local demo progress. Sets prevent reopening a screen from duplicating rewards. */
class DemoProgress {
    val completedRoutines = mutableSetOf<Int>()
    val completedMeals = mutableSetOf<Int>()
    val exerciseChecks = mutableMapOf<Int, MutableSet<Int>>()

    fun completeRoutine(id: Int): Boolean {
        val required = requiredSteps(id)
        if (required == 0 || exerciseChecks[id] != (0 until required).toSet()) return false
        return completedRoutines.add(id)
    }

    fun restore(snapshot: ProgressSnapshot) {
        completedRoutines.clear()
        completedRoutines.addAll(snapshot.completedRoutines)
        completedMeals.clear()
        completedMeals.addAll(snapshot.completedMeals)
        exerciseChecks.clear()
        snapshot.exerciseChecks.forEach { (id, checks) -> exerciseChecks[id] = checks.toMutableSet() }
    }

    val routinePercent get() = completedRoutines.count { it in 0 until ROUTINE_COUNT } * 100 / ROUTINE_COUNT
    val mealPercent get() = completedMeals.size * 100 / 4

    companion object {
        const val ROUTINE_COUNT = 2
        fun requiredSteps(id: Int): Int = when (id) { 0 -> 3; 1 -> 2; else -> 0 }
    }
}
