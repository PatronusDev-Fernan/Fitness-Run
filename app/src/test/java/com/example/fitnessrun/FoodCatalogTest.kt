package com.example.fitnessrun

import org.junit.Assert.*
import org.junit.Test

class FoodCatalogTest {
    @Test fun almondExclusionAlsoRemovesAlmondBeverage() {
        val result = FoodCatalog.filter(mapOf(RegistrationKeys.ALLERGY to "almendra"))
        assertTrue("Almendra" in result.excluded)
        assertTrue("Bebida de almendra" in result.excluded)
    }
    @Test fun catalogIsLargeAndUnique() {
        assertTrue(FoodCatalog.foods.size >= 100)
        assertEquals(FoodCatalog.foods.size, FoodCatalog.foods.map { it.name }.toSet().size)
        assertEquals(30, FoodCatalog.foods.count { it.group == FoodCatalog.Group.FRUITS })
        assertEquals(30, FoodCatalog.foods.count { it.group == FoodCatalog.Group.VEGETABLES })
    }
    @Test fun accentsAliasesAndPluralExclusionsAreApplied() {
        val result = FoodCatalog.filter(mapOf(RegistrationKeys.ALLERGY to "maní y nueces", RegistrationKeys.DISLIKES to "plátanos, champinion"))
        assertTrue(result.unresolved.toString(), result.unresolved.isEmpty())
        listOf("Cacahuate", "Nuez", "Almendra", "Bebida de almendra", "Plátano", "Champiñón").forEach { assertTrue(it, it in result.excluded) }
    }
    @Test fun dairyAndGlutenRestrictionsRemoveIngredientGroups() {
        val result = FoodCatalog.filter(mapOf(RegistrationKeys.RESTRICTIONS to "sin lactosa, sin gluten"))
        assertTrue(result.unresolved.isEmpty())
        assertTrue(result.available.none { "lacteos" in it.tags || "gluten" in it.tags })
        assertTrue(result.available.any { it.name == "Arroz integral" })
    }
    @Test fun dietAndExclusionsCombineWithoutErasingReason() {
        val result = FoodCatalog.filter(mapOf(RegistrationKeys.DIET_TYPE to "Vegana", RegistrationKeys.ALLERGY to "leche"))
        assertTrue(result.available.none { "animal" in it.tags })
        assertEquals(2, result.excluded.getValue("Leche").size)
    }
    @Test fun partialWordDoesNotExcludeUnrelatedFood() {
        val result = FoodCatalog.filter(mapOf(RegistrationKeys.DISLIKES to "col"))
        assertTrue("Col" in result.excluded)
        assertTrue(result.available.any { it.name == "Brócoli" })
        assertTrue(result.available.any { it.name == "Coliflor" })
    }
    @Test fun unknownClauseIsNotHiddenByKnownAllergen() {
        val result = FoodCatalog.filter(mapOf(RegistrationKeys.ALLERGY to "leche y aditivo desconocido"))
        assertTrue("Leche" in result.excluded)
        assertTrue(result.unresolved.any { "aditivo" in it })
    }
    @Test fun freeTextDoesNotAddUncataloguedIngredients() {
        val profile = mapOf(RegistrationKeys.FRUITS to "Ingrediente inventado")
        val options = FoodCatalog.preferred(FoodCatalog.filter(profile).available, profile, FoodCatalog.Group.FRUITS)
        assertTrue(options.all { it in FoodCatalog.foods })
    }
}
