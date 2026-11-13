package ro.echocare.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ro.echocare.CallRow
import ro.echocare.EchoCareApi
import ro.echocare.SeniorPage

/** One senior's page: GET /seniors/{id} (seniors §3.3). */
@HiltViewModel
class SeniorViewModel @Inject constructor(private val api: EchoCareApi) : ViewModel() {
    var page by mutableStateOf<Load<SeniorPage>>(Load.Loading)
        private set

    /** The short call history (UC-12); null until read, and on the senior's own home. */
    var calls by mutableStateOf<List<CallRow>?>(null)
        private set

    private var reading: Job? = null

    /** [id] is null on the senior's own home: the one senior their grant names. */
    fun reread(id: String?) {
        // A read still under way is out of date; left running, it could land after this one
        reading?.cancel()
        reading = viewModelScope.launch {
            page = page.after(read(id))
            // A failed read keeps the last list, as the page keeps its last answer
            if (id != null) (answer { api.calls(id) } as? Answer.Ok)?.let { calls = it.value }
        }
    }

    private suspend fun read(id: String?): Answer<SeniorPage> {
        val seniorId = id ?: when (val own = answer { api.seniors("senior") }) {
            is Answer.No -> return own
            is Answer.Ok -> own.value.firstOrNull()?.id ?: return Answer.No(Failure.NotFound)
        }
        return answer { api.senior(seniorId) }
    }
}
