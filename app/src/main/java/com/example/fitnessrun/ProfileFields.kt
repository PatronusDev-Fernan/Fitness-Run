package com.example.fitnessrun

object ProfileFields {
    val allowed = setOf(
        RegistrationKeys.NAME, RegistrationKeys.LAST_NAME, RegistrationKeys.BIRTH_DATE, RegistrationKeys.EMAIL,
        RegistrationKeys.HEIGHT, RegistrationKeys.WEIGHT, RegistrationKeys.BLOOD_TYPE, RegistrationKeys.CONDITION,
        RegistrationKeys.INJURY, RegistrationKeys.ALLERGY, RegistrationKeys.PAIN, RegistrationKeys.VEGETABLES,
        RegistrationKeys.FRUITS, RegistrationKeys.FAVORITES, RegistrationKeys.DISLIKES, RegistrationKeys.RESTRICTIONS,
        RegistrationKeys.DIET_TYPE, RegistrationKeys.FOOD_GOAL, ProfileIdentity.TYPE, ProfileIdentity.BIO, ProfileIdentity.PHOTO
    )

    fun sanitize(raw: Map<*, *>): Map<String, String> = raw.entries.mapNotNull { (key, value) ->
        if (key is String && key in allowed && value is String) key to value else null
    }.toMap()

    fun editIdentity(original: Map<String, String>, changes: Map<String, String>): Map<String, String> {
        val edits = changes.filterKeys {
            it in setOf(RegistrationKeys.NAME, RegistrationKeys.LAST_NAME, RegistrationKeys.FOOD_GOAL, ProfileIdentity.BIO, ProfileIdentity.PHOTO)
        }.mapValues { it.value.trim() }
        require(edits[ProfileIdentity.BIO].orEmpty().length <= ProfileIdentity.MAX_BIO)
        require(ProfileIdentity.validPhoto(edits[ProfileIdentity.PHOTO].orEmpty()))
        return sanitize(original) + edits
    }
}
