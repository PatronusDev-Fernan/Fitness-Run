package com.example.fitnessrun

import org.junit.Assert.*
import org.junit.Test

class PersonalizationEngineTest {
    @Test fun veganMenuDoesNotContainAnimalIngredients() {
        val menu = PersonalizationEngine.safeMeals(mapOf(RegistrationKeys.DIET_TYPE to "Vegana"))
        val text = menu.joinToString { it.title }.lowercase()
        assertFalse(text.contains("pollo"))
        assertFalse(text.contains("yogur"))
        assertTrue(text.contains("frijoles"))
        assertTrue(menu.all { it.selectable })
    }
    @Test fun unknownRestrictionPreventsMarkingMeals() {
        val menu = PersonalizationEngine.safeMeals(mapOf(RegistrationKeys.RESTRICTIONS to "indicación médica especial"))
        assertTrue(menu.all { !it.selectable && it.title == "Menú pendiente de revisión" })
    }
    @Test fun preferencesComeFromProvidedProfile() {
        val menu = PersonalizationEngine.safeMeals(mapOf(RegistrationKeys.FRUITS to "Pera", RegistrationKeys.ALLERGY to " Ninguna "))
        assertTrue(menu.first().title.contains("Pera"))
    }
    @Test fun exclusionWinsOverFavoriteAndDoesNotReappearAsFallback() {
        val profile = mapOf(RegistrationKeys.FRUITS to "Manzana", RegistrationKeys.DISLIKES to "manzana")
        assertTrue(PersonalizationEngine.safeMeals(profile).none { it.title.contains("Manzana") })
    }
    @Test fun missingCategoryDoesNotProduceFakeCompletedMeal() {
        val profile = mapOf(RegistrationKeys.DISLIKES to FoodCatalog.foods.filter { it.group == FoodCatalog.Group.FRUITS }.joinToString { it.name })
        assertFalse(PersonalizationEngine.safeMeals(profile).first().selectable)
    }
}
