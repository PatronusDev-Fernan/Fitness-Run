package com.example.fitnessrun

import org.junit.Assert.*
import org.junit.Test

class VideoUrlPolicyTest {
    @Test fun permitsEmbeddedVideo() {
        assertTrue(VideoUrlPolicy.isAllowed("https://www.youtube-nocookie.com/embed/HNORmHn16gY?playsinline=1"))
    }
    @Test fun rejectsUntrustedDestinations() {
        listOf(
            "http://www.youtube-nocookie.com/embed/HNORmHn16gY",
            "https://www.youtube-nocookie.com.evil.test/embed/HNORmHn16gY",
            "https://www.youtube-nocookie.com@evil.test/embed/HNORmHn16gY",
            "https://www.youtube-nocookie.com:444/embed/HNORmHn16gY",
            "https://www.youtube-nocookie.com/watch?v=HNORmHn16gY",
            "file:///data/data/com.example.fitnessrun/secret", "javascript:alert(1)", "not a URI"
        ).forEach { assertFalse(it, VideoUrlPolicy.isAllowed(it)) }
    }
    @Test fun rejectsVideoIdInjection() {
        assertFalse(VideoUrlPolicy.isValidId("../watch?v=x"))
        assertFalse(VideoUrlPolicy.isValidId("HNORmHn16gY?x=1"))
    }
}
