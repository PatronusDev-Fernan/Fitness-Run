package com.example.fitnessrun

object PersonalizationEngine {
    data class DayPlan(val day: String, val title: String, val detail: String, val selectable: Boolean = true)

    fun weekdayRoutines(): List<DayPlan> {
        val health = listOf(RegistrationKeys.CONDITION, RegistrationKeys.INJURY, RegistrationKeys.PAIN)
            .joinToString(" ") { DemoSession.profile[it].orEmpty() }.lowercase()
        val goal = DemoSession.profile[RegistrationKeys.FOOD_GOAL].orEmpty().lowercase()
        val needsLowImpact = health.isMeaningful() && listOf("hernia", "dolor", "lesión", "fractura", "rodilla", "espalda")
            .any { it in health }
        return when {
            needsLowImpact -> listOf(
                DayPlan("Lunes", "Movilidad guiada", "25 min · Bajo impacto"), DayPlan("Martes", "Tren superior adaptado", "30 min · Movilidad"),
                DayPlan("Miércoles", "Caminata y respiración", "25 min · Ritmo moderado"), DayPlan("Jueves", "Fuerza funcional adaptada", "30 min · Técnica controlada"),
                DayPlan("Viernes", "Movilidad y recuperación", "20 min · Recuperación")
            )
            "masa" in goal -> listOf(
                DayPlan("Lunes", "Pierna y glúteo", "40 min · Fuerza"), DayPlan("Martes", "Pecho y tríceps", "35 min · Fuerza"),
                DayPlan("Miércoles", "Movilidad activa", "25 min · Recuperación"), DayPlan("Jueves", "Espalda y bíceps", "35 min · Fuerza"),
                DayPlan("Viernes", "Cuerpo completo", "40 min · Progresión")
            )
            else -> listOf(
                DayPlan("Lunes", "Pierna y movilidad", "30 min · Principiante"), DayPlan("Martes", "Tren superior", "30 min · Principiante"),
                DayPlan("Miércoles", "Cardio moderado", "25 min · Resistencia"), DayPlan("Jueves", "Fuerza funcional", "30 min · Cuerpo completo"),
                DayPlan("Viernes", "Movilidad y core", "25 min · Control")
            )
        }
    }

    fun safeMeals(profile: Map<String, String> = DemoSession.profile): List<DayPlan> {
        val filtered = FoodCatalog.filter(profile)
        val slots = listOf("Desayuno", "Colación", "Comida", "Cena")
        if (filtered.unresolved.isNotEmpty()) return slots.map {
            DayPlan(it, "Menú pendiente de revisión", "Hay restricciones que no se pudieron interpretar", false)
        }
        fun options(group: FoodCatalog.Group) = FoodCatalog.preferred(filtered.available, profile, group)
        fun pick(group: FoodCatalog.Group, index: Int = 0): String? = options(group).let {
            if (it.isEmpty()) null else it[index % it.size].name
        }
        val fruit = pick(FoodCatalog.Group.FRUITS)
        val vegetable = pick(FoodCatalog.Group.VEGETABLES)
        val grain = pick(FoodCatalog.Group.GRAINS)
        val protein = pick(FoodCatalog.Group.PROTEIN)
        val ingredients = listOf(
            listOf(grain, fruit), listOf(pick(FoodCatalog.Group.FRUITS, 1), pick(FoodCatalog.Group.SEEDS)),
            listOf(protein, grain, vegetable),
            listOf(pick(FoodCatalog.Group.PROTEIN, 1), pick(FoodCatalog.Group.VEGETABLES, 1))
        )
        return slots.mapIndexed { index, slot ->
            val parts = ingredients[index]
            if (parts.any { it == null }) DayPlan(slot, "Sin opciones suficientes", "Revisa las exclusiones registradas", false)
            else DayPlan(slot, parts.filterNotNull().joinToString(", "), "Ingredientes filtrados; revisa etiquetas y preparación")
        }
    }

    fun exclusionsLabel(): String {
        val filtered = FoodCatalog.filter(DemoSession.profile)
        val summary = "${filtered.available.size} ingredientes disponibles · ${filtered.excluded.size} excluidos"
        return if (filtered.unresolved.isEmpty()) "$summary. El filtro no evalúa condiciones médicas ni contaminación cruzada."
        else "$summary. Pendiente de revisión: ${filtered.unresolved.joinToString()}"
    }

    private fun String.isMeaningful() = FoodCatalog.meaningful(this)
}

