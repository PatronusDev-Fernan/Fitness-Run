package com.example.fitnessrun

/** Short-lived credential handoff. Never written to Intents, saved state or disk. */
object RegistrationDraft {
    private var email: String? = null
    private var password: CharArray? = null
    private var createdAt = 0L
    fun set(email: String, password: String) {
        clear()
        this.email = email
        this.password = password.toCharArray()
        createdAt = System.nanoTime()
    }
    fun take(email: String): String? {
        val result = if (this.email == email && System.nanoTime() - createdAt < 30L * 60 * 1_000_000_000)
            password?.concatToString() else null
        clear()
        return result
    }
    fun clear() { password?.fill('\u0000'); password = null; email = null; createdAt = 0L }
}
