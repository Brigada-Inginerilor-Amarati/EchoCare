package ro.echocare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

// The skeleton's one screen: does this build reach the API it was built for? Replaced by sign-in later.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                // null while the call is out
                val result by produceState<Result<String>?>(null) { value = checkApi() }
                val status = result?.fold(
                    { stringResource(R.string.api_ok, it) },
                    { stringResource(R.string.api_unreachable, it.message.orEmpty()) }
                ) ?: stringResource(R.string.api_checking)
                Scaffold { padding ->
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(status, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}

// The server's environment name, or why it did not answer. Cancellation is not caught, so a left
// screen stops the call.
private suspend fun checkApi(): Result<String> = try {
    Result.success(echoCareApi.health().env)
} catch (e: HttpException) {
    Result.failure(IOException("HTTP ${e.code()}"))
} catch (e: IOException) {
    Result.failure(e)
} catch (e: SerializationException) {
    // A 200 that is not our API, such as a captive portal's page
    Result.failure(e)
}
