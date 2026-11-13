package ro.echocare.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun greetsByName() {
        assertEquals("Hello from EchoCare, Ana", greeting("Ana"))
    }
}
