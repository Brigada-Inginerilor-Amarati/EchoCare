package ro.echocare

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

// GET /seniors/{id} (seniors §3.3): the row, the address with its access details (G13), the
// medic and the caregivers. The same for every role (seniors SE7).
@Serializable
data class SeniorPage(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("form_of_address") val formOfAddress: String,
    @SerialName("phone_e164") val phone: String,
    @SerialName("phone_is_landline") val landline: Boolean,
    @SerialName("enrollment_state") val state: String,
    val code: String? = null,
    val reason: String? = null,
    val since: String? = null,
    val street: String? = null,
    @SerialName("street_number") val streetNumber: String? = null,
    val block: String? = null,
    val staircase: String? = null,
    val floor: String? = null,
    val apartment: String? = null,
    @SerialName("intercom_code") val intercomCode: String? = null,
    @SerialName("key_holder") val keyHolder: String? = null,
    val landmark: String? = null,
    @SerialName("call_window") val callWindow: String? = null,
    val medic: Medic,
    val caregivers: List<Caregiver> = emptyList(),
    // One of her calls is ringing or live: the "call in progress" badge (UC-12). Null from an
    // older API, which reads as no.
    @SerialName("call_in_progress") val callInProgress: Boolean? = null,
    // When the consent was signed on the tablet (G7, 04 §1); null before, or from an older API.
    @SerialName("signed_at") val signedAt: String? = null
)

@Serializable
data class Medic(@SerialName("display_name") val displayName: String, val practice: String? = null)

@Serializable
data class Caregiver(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    val relation: String? = null,
    @SerialName("phone_e164") val phone: String
)

// POST /seniors and PUT /seniors/{id}'s body: the whole registration (seniors §3.5). No defaults,
// so every field is sent, an empty one as null: PUT replaces every field.
@Serializable
data class SeniorForm(
    @SerialName("full_name") val fullName: String,
    @SerialName("form_of_address") val formOfAddress: String,
    val phone: String,
    @SerialName("call_window") val callWindow: String?,
    val street: String?,
    @SerialName("street_number") val streetNumber: String?,
    val block: String?,
    val staircase: String?,
    val floor: String?,
    val apartment: String?,
    @SerialName("intercom_code") val intercomCode: String?,
    @SerialName("key_holder") val keyHolder: String?,
    val landmark: String?
)

// POST /seniors/{id}/caregivers's body (seniors §3.5). The phone must be a mobile: caregivers
// sign in by SMS.
@Serializable
data class CaregiverForm(
    @SerialName("display_name") val displayName: String,
    val relation: String,
    val phone: String
)

// POST /seniors/{id}/signature's body (04 §2): the consent text's version and the strokes of the
// signature, each a list of [x, y] points.
@Serializable
data class SignatureForm(
    @SerialName("text_version") val textVersion: String,
    val strokes: List<List<List<Float>>>
)

// One row of GET /seniors/{id}/calls (04 §1), newest first. What a caregiver may see: no turns,
// no transcript (01 §6). [trigger] and [state] stay strings (ADR-001 §2.3).
@Serializable
data class CallRow(
    val id: String,
    val trigger: String,
    val state: String,
    @SerialName("due_at") val dueAt: String,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("final_severity") val finalSeverity: String? = null,
    val reason: String? = null
)

// One row of GET /incidents (04 §1). [status] stays a string: a later server may add one.
@Serializable
data class IncidentRow(
    val id: String,
    @SerialName("senior_full_name") val seniorName: String,
    @SerialName("senior_form_of_address") val formOfAddress: String,
    val status: String,
    val reason: String,
    @SerialName("opened_at") val openedAt: String,
    @SerialName("taken_by_name") val takenByName: String? = null,
    @SerialName("taken_at") val takenAt: String? = null,
    @SerialName("handover_answered") val handoverAnswered: Boolean = false
)

// GET /incidents/{id} (04 §1): the senior with access details, the caregivers, the triggering
// event and every call from the first attempt until the incident was resolved.
@Serializable
data class IncidentPage(
    val id: String,
    val status: String,
    val reason: String,
    @SerialName("opened_at") val openedAt: String,
    @SerialName("taken_by_name") val takenByName: String? = null,
    @SerialName("handover_answered") val handoverAnswered: Boolean = false,
    val senior: IncidentSenior,
    val caregivers: List<IncidentCaregiver> = emptyList(),
    val event: TriggerEvent? = null,
    val calls: List<IncidentCall> = emptyList()
)

@Serializable
data class IncidentSenior(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("form_of_address") val formOfAddress: String,
    @SerialName("phone_e164") val phone: String,
    @SerialName("phone_is_landline") val landline: Boolean,
    val street: String? = null,
    @SerialName("street_number") val streetNumber: String? = null,
    val block: String? = null,
    val staircase: String? = null,
    val floor: String? = null,
    val apartment: String? = null,
    @SerialName("intercom_code") val intercomCode: String? = null,
    @SerialName("key_holder") val keyHolder: String? = null,
    val landmark: String? = null
)

// No user id here, unlike GET /seniors/{id}'s Caregiver.
@Serializable
data class IncidentCaregiver(
    @SerialName("display_name") val displayName: String,
    @SerialName("phone_e164") val phone: String,
    val relation: String? = null
)

@Serializable
data class TriggerEvent(
    val type: String,
    @SerialName("measured_at") val measuredAt: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("location_accuracy_m") val accuracy: Double? = null
)

// [handoverAnswered] is null when no hand-over was tried on this call.
@Serializable
data class IncidentCall(
    val id: String,
    val trigger: String,
    val attempt: Int,
    val state: String,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("final_severity") val finalSeverity: String? = null,
    @SerialName("handover_answered") val handoverAnswered: Boolean? = null,
    val reports: List<CallReport> = emptyList(),
    val turns: List<CallTurn> = emptyList()
)

@Serializable
data class CallReport(
    val severity: String,
    val reason: String,
    val quote: String? = null,
    val at: String
)

// [kind] is `speech` or `keys`, a key press whose [content] is the key.
@Serializable
data class CallTurn(
    val speaker: String,
    val content: String,
    val at: String,
    val kind: String = "speech"
)

// POST /incidents/{id}/resolve's body (04 §2): both required, the note 1 to 1000 characters.
@Serializable
data class Resolution(val outcome: String, val note: String)

// The FastAPI contract (04-interfaces.md). One function per endpoint, added as features need them.
interface EchoCareApi {
    @GET("profile")
    suspend fun me(): Me

    /** The seniors the caller reaches as [role]; the server narrows the list (seniors SE6). */
    @GET("seniors")
    suspend fun seniors(@Query("role") role: String): List<SeniorRow>

    @GET("seniors/{id}")
    suspend fun senior(@Path("id") id: String): SeniorPage

    /** The senior's calls, newest first, queued ones included (04 §1). */
    @GET("seniors/{id}/calls")
    suspend fun calls(@Path("id") id: String): List<CallRow>

    /** A new senior in `draft`, registered by the calling medic (04 §2). Not repeat-safe (SE14). */
    @POST("seniors")
    suspend fun createSenior(@Body body: SeniorForm): SeniorPage

    /** The whole registration again, in any state (04 §2, SE9). */
    @PUT("seniors/{id}")
    suspend fun updateSenior(@Path("id") id: String, @Body body: SeniorForm): SeniorPage

    @POST("seniors/{id}/caregivers")
    suspend fun addCaregiver(@Path("id") id: String, @Body body: CaregiverForm): Caregiver

    /** 204, also on a repeat (04 §2). A Response, because a 204 has no body to decode. */
    @DELETE("seniors/{id}/caregivers/{userId}")
    suspend fun removeCaregiver(
        @Path("id") id: String,
        @Path("userId") userId: String
    ): Response<Unit>

    /** `draft` → `submitted`: DAS sees the senior from now on (04 §2). Answers one row. */
    @POST("seniors/{id}/submit")
    suspend fun submit(@Path("id") id: String): SeniorRow

    /** Stores the signed consent, 204 (04 §2). A Response, because a 204 has no body. */
    @POST("seniors/{id}/signature")
    suspend fun sign(@Path("id") id: String, @Body body: SignatureForm): Response<Unit>

    /** DAS only. [status] is comma-separated; the DAS app asks for `open,in_progress` (04 §1). */
    @GET("incidents")
    suspend fun incidents(@Query("status") status: String): List<IncidentRow>

    @GET("incidents/{id}")
    suspend fun incident(@Path("id") id: String): IncidentPage

    /** `open` → `in_progress` (UC-11). 409 `already_done` names who has it in `taken_by_name`. */
    @POST("incidents/{id}/take")
    suspend fun take(@Path("id") id: String): IncidentRow

    /** `in_progress` → `resolved`, by any DAS employee (UC-11). 409 `wrong_state` while open. */
    @POST("incidents/{id}/resolve")
    suspend fun resolve(@Path("id") id: String, @Body body: Resolution): IncidentRow
}
