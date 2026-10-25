package com.example.fitnessrun

import java.net.URI

/** Only embedded HTTPS videos may navigate inside the player. */
object VideoUrlPolicy {
    fun isValidId(id: String) = Regex("[A-Za-z0-9_-]{11}").matches(id)

    fun isAllowed(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.host == "www.youtube-nocookie.com" &&
            uri.rawUserInfo == null && uri.port in listOf(-1, 443) &&
            uri.path.startsWith("/embed/") && isValidId(uri.path.removePrefix("/embed/"))
    }.getOrDefault(false)
}
