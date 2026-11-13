package ro.echocare

import org.junit.Assert.assertEquals
import org.junit.Test

// The app reads a newer server leniently (ADR-001 §2.3).
class ApiTest {
    @Test
    fun aGrantKeepsOnlyItsRole() {
        val me = json.decodeFromString<Me>(
            """{"display_name": "Ana Pop", "phone_e164": "+40700000003", "grants": [
                {"role": "caregiver", "senior_id": "s1", "senior_form_of_address": "doamna Maria",
                 "relation": "fiică"}]}"""
        )
        assertEquals(Me("Ana Pop", listOf(Grant("caregiver"))), me)
    }

    @Test
    fun aRoleALaterServerAddsStillDecodes() {
        val me = json.decodeFromString<Me>(
            """{"display_name": "Ana Pop", "grants": [{"role": "nurse"}]}"""
        )
        assertEquals("nurse", me.grants.single().role)
    }
}
