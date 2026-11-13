package ro.echocare.auth

import org.junit.Assert.assertEquals
import org.junit.Test

// auth-layer §7.1: what is sent to Firebase for what the person typed.
class ScreenTest {
    @Test
    fun phoneToE164() {
        assertEquals("+40722123456", toE164("0722 123 456"))
        assertEquals("+40722123456", toE164("(0722) 123-456"))
        assertEquals("+40722123456", toE164("0040.722.123.456"))
        assertEquals("+40722123456", toE164("722123456"))
        assertEquals("+40700000001", toE164("+40700000001"))
        assertEquals("+16505550101", toE164("+1 650 555 0101"))
    }
}
