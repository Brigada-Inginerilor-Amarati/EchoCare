package ro.echocare

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.http.GET
import retrofit2.http.Query

// An installed APK keeps talking to a newer API: ignore fields this build does not know (ADR-001 §2.3).
internal val json = Json { ignoreUnknownKeys = true }

// GET /profile (auth-layer §3.3). Only the fields the app reads; roles stay strings (ADR-001 §2.3).
@Serializable
data class Me(@SerialName("display_name") val displayName: String, val grants: List<Grant>)

@Serializable
data class Grant(val role: String)

// One row of GET /seniors (seniors §3.3). The state stays a string: a later server may add one.
// [code], [reason] and [since] are the latest result of an active senior, else null (03 §8).
@Serializable
data class SeniorRow(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("form_of_address") val formOfAddress: String,
    @SerialName("phone_e164") val phone: String,
    @SerialName("phone_is_landline") val landline: Boolean,
    @SerialName("enrollment_state") val state: String,
    val code: String? = null,
    val reason: String? = null,
    val since: String? = null
)

// The FastAPI contract (04-interfaces.md). One function per endpoint, added as features need them.
interface EchoCareApi {
    @GET("profile")
    suspend fun me(): Me

    /** The seniors the caller reaches as [role]; the server narrows the list (seniors SE6). */
    @GET("seniors")
    suspend fun seniors(@Query("role") role: String): List<SeniorRow>
}
