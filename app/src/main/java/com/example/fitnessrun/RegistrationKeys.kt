package com.example.fitnessrun

object RegistrationKeys {
    // Only registration fields may travel to the local profile; never credentials.
    fun copyTo(source: android.content.Intent, destination: android.content.Intent): android.content.Intent {
        fields.forEach { key -> source.getStringExtra(key)?.let { destination.putExtra(key, it) } }
        source.getStringExtra(DemoSession.CLOUD_UID)?.let { destination.putExtra(DemoSession.CLOUD_UID, it) }
        return destination
    }

    private val fields get() = listOf(NAME, LAST_NAME, BIRTH_DATE, EMAIL, HEIGHT, WEIGHT,
        BLOOD_TYPE, CONDITION, INJURY, ALLERGY, PAIN, VEGETABLES, FRUITS, FAVORITES,
        DISLIKES, RESTRICTIONS, DIET_TYPE, FOOD_GOAL, ProfileIdentity.TYPE, ProfileIdentity.BIO, ProfileIdentity.PHOTO)
    const val NAME = "registration.name"
    const val LAST_NAME = "registration.last_name"
    const val BIRTH_DATE = "registration.birth_date"
    const val EMAIL = "registration.email"
    const val HEIGHT = "registration.height"
    const val WEIGHT = "registration.weight"
    const val BLOOD_TYPE = "registration.blood_type"
    const val CONDITION = "registration.condition"
    const val INJURY = "registration.injury"
    const val ALLERGY = "registration.allergy"
    const val PAIN = "registration.pain"
    const val VEGETABLES = "registration.vegetables"
    const val FRUITS = "registration.fruits"
    const val FAVORITES = "registration.favorites"
    const val DISLIKES = "registration.dislikes"
    const val RESTRICTIONS = "registration.restrictions"
    const val DIET_TYPE = "registration.diet_type"
    const val FOOD_GOAL = "registration.food_goal"
}
