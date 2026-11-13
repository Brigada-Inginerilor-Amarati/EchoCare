package ro.echocare

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.http.GET

// An installed APK keeps talking to a newer API: ignore fields this build does not know (ADR-001 §2.3).
internal val json = Json { ignoreUnknownKeys = true }

// GET /health: [env] is the server's own name for where it runs: local, staging or production.
@Serializable
data class Health(val status: String, val env: String)

// The FastAPI contract (04-interfaces.md). One function per endpoint, added as features need them.
interface EchoCareApi {
    @GET("health")
    suspend fun health(): Health
}
