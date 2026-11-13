package ro.echocare

import org.junit.Assert.assertEquals
import org.junit.Test

// The app reads a newer server leniently (ADR-001 §2.3).
class ApiTest {
    @Test
    fun healthIgnoresFieldsALaterServerAdds() {
        val body = """{"status": "ok", "env": "staging", "later": 1}"""
        assertEquals(Health("ok", "staging"), json.decodeFromString<Health>(body))
    }
}
