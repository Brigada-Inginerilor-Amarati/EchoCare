package ro.echocare

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.http.GET

// An installed APK keeps talking to a newer API: ignore fields this build does not know (ADR-001 §2.3).
internal val json = Json { ignoreUnknownKeys = true }

// GET /profile (auth-layer §3.3). Only the fields the app reads; roles stay strings (ADR-001 §2.3).
@Serializable
data class Me(@SerialName("display_name") val displayName: String, val grants: List<Grant>)

@Serializable
data class Grant(val role: String)

// The FastAPI contract (04-interfaces.md). One function per endpoint, added as features need them.
interface EchoCareApi {
    @GET("profile")
    suspend fun me(): Me
}
