package ro.echocare.home

import org.junit.Assert.assertEquals
import org.junit.Test
import ro.echocare.auth.ROLES

// seniors §8.2: which home each role gets.
class RoutesTest {
    @Test
    fun eachRoleHasItsHome() {
        assertEquals(SeniorsRoute("medic"), homeFor("medic"))
        assertEquals(SeniorsRoute("caregiver"), homeFor("caregiver"))
        assertEquals(OwnRoute, homeFor("senior"))
        assertEquals(IncidentsRoute, homeFor("das"))
    }

    @Test
    fun everyRoleTheAppKnowsHasAHome() {
        // The medic and the caregiver share the list, each reading it as their own role.
        assertEquals(4, ROLES.map(::homeFor).toSet().size)
    }
}
