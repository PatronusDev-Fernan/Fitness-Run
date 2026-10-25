package com.example.fitnessrun

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ProgressSnapshot(
    val date: String,
    val completedRoutines: Set<Int>,
    val completedMeals: Set<Int>,
    val exerciseChecks: Map<Int, Set<Int>>,
    val weekendRoutine: String,
    val weekendFood: String
) {
    fun validate(): ProgressSnapshot {
        require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(date))
        weekStart(date)
        require(completedRoutines.all { it in 0..1 })
        require(completedMeals.all { it in 0..3 })
        require(exerciseChecks.all { (id, checks) -> id in 0..1 && checks.all { it in 0 until DemoProgress.requiredSteps(id) } })
        require(completedRoutines.all { exerciseChecks[it] == (0 until DemoProgress.requiredSteps(it)).toSet() })
        require(weekendRoutine in routineOptions && weekendFood in foodOptions)
        return this
    }

    fun forDate(target: String): ProgressSnapshot = when {
        target == date -> this
        target > date && weekStart(target) == weekStart(date) -> copy(date = target, completedMeals = emptySet())
        else -> ProgressSnapshot(target, emptySet(), emptySet(), emptyMap(), "Sin seleccionar", "Sin seleccionar")
    }

    companion object {
        fun weekStart(date: String): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val parsed = try { format.parse(date) } catch (error: java.text.ParseException) { throw IllegalArgumentException("Invalid date", error) }
            require(parsed != null && format.format(parsed) == date)
            val calendar = Calendar.getInstance().apply { time = parsed }
            calendar.add(Calendar.DAY_OF_MONTH, -((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7))
            return format.format(calendar.time)
        }
        val routineOptions = setOf("Sin seleccionar", "Descanso", "Caminata", "Estiramiento", "Actividad libre")
        val foodOptions = setOf("Sin seleccionar", "Mantener el menú semanal", "Comida flexible planificada", "Elegir alternativas seguras")
    }
}
