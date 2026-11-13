package ro.echocare.home

import kotlinx.serialization.Serializable

// The destinations of the signed-in app (seniors §8.2), as type-safe routes.

/** The seniors the caller reaches as [role]: the medic's and the caregiver's home. */
@Serializable
data class SeniorsRoute(val role: String)

/** The senior's home: their own page. */
@Serializable
data object OwnRoute

/** The DAS home: open and in-progress incidents (UC-12). */
@Serializable
data object IncidentsRoute

/** Where [role] lands after sign-in. [role] is one of auth.ROLES. */
fun homeFor(role: String): Any = when (role) {
    "medic", "caregiver" -> SeniorsRoute(role)
    "senior" -> OwnRoute
    else -> IncidentsRoute
}
