package com.example.fitnessrun

import java.text.Normalizer
import java.util.Locale

/** Ingredient-level examples, not packaged products or a clinical diet. */
object FoodCatalog {
    enum class Group(val label: String) {
        VEGETABLES("Verduras"), FRUITS("Frutas"), PROTEIN("Proteínas y leguminosas"),
        GRAINS("Cereales y tubérculos"), DAIRY("Lácteos y alternativas"), SEEDS("Semillas y grasas")
    }
    data class Food(val name: String, val group: Group, val tags: Set<String> = emptySet(), val aliases: Set<String> = emptySet())
    data class Result(val available: List<Food>, val excluded: Map<String, List<String>>, val unresolved: List<String>)

    private fun items(group: Group, names: String, vararg tags: String) = names.split('|').map { Food(it, group, tags.toSet()) }
    val foods: List<Food> = buildList {
        addAll(items(Group.VEGETABLES, "Zanahoria|Brócoli|Espinaca|Lechuga|Calabacita|Jitomate|Pepino|Chayote|Coliflor|Rábano|Champiñón|Nopal|Acelga|Apio|Berenjena|Betabel|Col|Col morada|Ejote|Espárrago|Pimiento|Cebolla|Ajo|Poro|Alcachofa|Berro|Arúgula|Jícama|Chile poblano|Tomate verde"))
        addAll(items(Group.FRUITS, "Manzana|Plátano|Naranja|Fresa|Mango|Papaya|Piña|Sandía|Melón|Kiwi|Durazno|Pera|Mandarina|Guayaba|Uva|Ciruela|Toronja|Limón|Lima|Frambuesa|Zarzamora|Arándano|Cereza|Granada|Higo|Tuna|Pitahaya|Maracuyá|Chabacano|Mamey"))
        addAll(items(Group.PROTEIN, "Frijoles|Lentejas|Garbanzos|Habas|Chícharos", "leguminosas"))
        addAll(items(Group.PROTEIN, "Tofu|Edamame", "soya", "leguminosas"))
        addAll(items(Group.PROTEIN, "Pollo|Pavo|Res|Cerdo", "carne", "animal"))
        addAll(items(Group.PROTEIN, "Atún|Salmón|Sardina|Tilapia", "pescado", "animal"))
        addAll(items(Group.PROTEIN, "Camarón", "mariscos", "animal"))
        addAll(items(Group.PROTEIN, "Huevo", "huevo", "animal"))
        addAll(items(Group.GRAINS, "Arroz integral|Arroz blanco|Quinoa|Amaranto|Papa|Camote|Elote|Tortilla de maíz"))
        addAll(items(Group.GRAINS, "Avena", "avena", "gluten"))
        addAll(items(Group.GRAINS, "Pan integral|Pasta de trigo|Cuscús|Tortilla de harina", "trigo", "gluten"))
        addAll(items(Group.GRAINS, "Cebada|Centeno", "gluten"))
        addAll(items(Group.DAIRY, "Leche|Yogur natural|Queso panela|Requesón", "lacteos", "lactosa", "animal"))
        addAll(items(Group.DAIRY, "Bebida de soya", "soya"))
        addAll(items(Group.DAIRY, "Bebida de almendra", "frutos secos", "almendra"))
        addAll(items(Group.SEEDS, "Nuez|Almendra|Avellana|Pistache|Anacardo", "frutos secos"))
        addAll(items(Group.SEEDS, "Cacahuate", "cacahuate", "leguminosas"))
        addAll(items(Group.SEEDS, "Ajonjolí", "sesamo", "semillas"))
        addAll(items(Group.SEEDS, "Chía|Linaza|Pepita|Semilla de girasol", "semillas"))
        addAll(items(Group.SEEDS, "Aguacate|Aceite de oliva"))
    }.map { food ->
        food.copy(aliases = food.aliases + when (food.name) {
            "Calabacita" -> setOf("calabaza", "calabacin")
            "Jitomate" -> setOf("tomate")
            "Tomate verde" -> setOf("tomate")
            "Col morada" -> setOf("col")
            "Champiñón" -> setOf("champinion", "hongos")
            "Frijoles" -> setOf("frijol", "alubias")
            "Plátano" -> setOf("banana", "banano")
            "Chícharos" -> setOf("chicharo", "guisantes")
            "Nuez" -> setOf("nueces")
            "Cacahuate" -> setOf("mani", "cacahuetes")
            "Ajonjolí" -> setOf("sesamo")
            "Anacardo" -> setOf("nuez de la india")
            "Yogur natural" -> setOf("yogur", "yogurt")
            else -> emptySet()
        })
    }

    fun normalize(value: String): String = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()
    fun meaningful(value: String) = normalize(value) !in setOf("", "no", "ninguna", "ninguno", "sin especificar", "ninguna en particular")
    private val groups = mapOf(
        "lacteos" to setOf("lacteos", "leche", "lactosa", "caseina", "queso", "yogur", "yogurt"),
        "gluten" to setOf("gluten", "celiaquia", "celiaco", "celiaca"),
        "trigo" to setOf("trigo"), "soya" to setOf("soya", "soja"),
        "frutos secos" to setOf("frutos secos", "nueces"),
        "cacahuate" to setOf("cacahuate", "cacahuetes", "mani"),
        "pescado" to setOf("pescado", "pescados"), "mariscos" to setOf("marisco", "mariscos", "crustaceos"),
        "huevo" to setOf("huevo", "huevos"), "carne" to setOf("carne", "carnes"),
        "semillas" to setOf("semilla", "semillas"), "sesamo" to setOf("sesamo", "ajonjoli"),
        "leguminosas" to setOf("leguminosas", "legumbres")
    )
    private fun containsTerm(text: String, term: String): Boolean {
        val normalized = normalize(term)
        return Regex("(?<![a-z0-9])" + Regex.escape(normalized) + "(?:s|es)?(?![a-z0-9])").containsMatchIn(text)
    }

    fun filter(profile: Map<String, String>): Result {
        val reasons = linkedMapOf<String, MutableList<String>>()
        val unresolved = mutableListOf<String>()
        fun exclude(food: Food, reason: String) { reasons.getOrPut(food.name) { mutableListOf() }.add(reason) }
        val diet = normalize(profile[RegistrationKeys.DIET_TYPE].orEmpty())
        foods.forEach { food ->
            if ((diet == "vegana" && "animal" in food.tags) ||
                (diet == "vegetariana" && food.tags.any { it in setOf("carne", "pescado", "mariscos") })) exclude(food, "Tipo de alimentación")
        }
        if (diet !in setOf("", "ninguna en particular", "vegana", "vegetariana")) unresolved.add("Tipo de alimentación: ${profile[RegistrationKeys.DIET_TYPE]}")
        listOf(RegistrationKeys.ALLERGY to "Alergia", RegistrationKeys.RESTRICTIONS to "Restricción", RegistrationKeys.DISLIKES to "No deseado").forEach { (key, label) ->
            val raw = profile[key].orEmpty()
            if (!meaningful(raw)) return@forEach
            // Every unrecognised fragment remains visible for review; it is never assumed safe.
            raw.split(Regex("[,;\\n]+|\\s+y\\s+", RegexOption.IGNORE_CASE)).filter { meaningful(it) }.forEach { part ->
                val text = normalize(part)
                val matchedGroups = groups.filterValues { aliases -> aliases.any { containsTerm(text, it) } }.keys
                val matched = foods.filter { food ->
                    (food.aliases + food.name).any { containsTerm(text, it) } || food.tags.any { it in matchedGroups || containsTerm(text, it) }
                }
                matched.forEach { exclude(it, "$label: ${part.trim()}") }
                // Remove recognised words and common connecting words. Unknown qualifiers fail closed.
                var rest = text
                val terms = groups.values.flatten() + foods.flatMap { it.aliases + it.name }
                terms.sortedByDescending { it.length }.forEach { term ->
                    rest = rest.replace(Regex("(?<![a-z0-9])" + Regex.escape(normalize(term)) + "(?:s|es)?(?![a-z0-9])"), " ")
                }
                val filler = setOf("alergia", "alergico", "alergica", "intolerancia", "intolerante", "a", "al", "de", "del", "la", "el", "los", "las", "sin", "no", "como", "consumo", "me", "gusta", "gustan", "evitar", "ni", "y")
                if (matched.isEmpty() || rest.split(Regex("\\s+")).any { it.isNotBlank() && it !in filler }) unresolved.add("$label: ${part.trim()}")
            }
        }
        return Result(foods.filter { it.name !in reasons }, reasons.mapValues { it.value.distinct() }, unresolved.distinct())
    }

    fun preferred(available: List<Food>, profile: Map<String, String>, group: Group): List<Food> {
        val preferences = listOf(RegistrationKeys.FRUITS, RegistrationKeys.VEGETABLES, RegistrationKeys.FAVORITES)
            .joinToString(" ") { normalize(profile[it].orEmpty()) }
        return available.filter { it.group == group }.sortedByDescending { food ->
            (food.aliases + food.name).any { containsTerm(preferences, it) }
        }
    }
}
