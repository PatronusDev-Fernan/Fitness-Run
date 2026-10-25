package com.example.fitnessrun

/** Account presentation only. Never grants access to another person's records. */
object ProfileIdentity {
    const val TYPE = "registration.account_type"
    const val BIO = "registration.bio"
    const val PHOTO = "registration.photo"
    const val USER = "user"
    const val COACH = "coach"
    const val MAX_BIO = 240
    const val MAX_PHOTO = 60000
    fun type(fields: Map<String, String>) = if (fields[TYPE] == COACH) COACH else USER
    fun badge(fields: Map<String, String>) = if (type(fields) == COACH) "@coach" else "Usuario"
    fun matches(fields: Map<String, String>, selected: String) = selected in setOf(USER, COACH) && type(fields) == selected
    fun validPhoto(value: String) = value.isEmpty() ||
        (value.length <= MAX_PHOTO && value.startsWith("/9j/") && Regex("[A-Za-z0-9+/]+={0,2}").matches(value))
}
