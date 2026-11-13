package ro.echocare.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ro.echocare.R

private const val TAG = "EchoCareAuth"

/**
 * Firebase's own reason for a failed sign-in step, for `adb logcat -s EchoCareAuth`. The screen
 * shows only a short Romanian text (auth-layer §3.2), which hides codes such as `Error code:39`.
 */
private fun logFailure(step: String, e: FirebaseException) {
    val code = (e as? FirebaseAuthException)?.errorCode
    Log.w(TAG, "$step failed: ${e.javaClass.simpleName} ${code.orEmpty()} ${e.message}", e)
}

/**
 * Phone sign-in (auth-layer §8.2, UC-02). The Activity starts phone verification, because
 * PhoneAuthOptions needs an Activity, and passes the callbacks' results in here.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: FirebaseAuth) : ViewModel() {
    // Firebase keeps the session across restarts: someone already signed in skips the phone step.
    var screen by mutableStateOf<Screen>(
        if (auth.currentUser != null) Screen.SignedIn else Screen.Phone()
    )
        private set

    /** *Retrimite codul* opens 30 seconds after a code is sent (auth-layer §8.2). */
    var canResend by mutableStateOf(false)
        private set

    /** The E.164 number the code went to, and Firebase's token for resending to it. */
    var phone = ""
        private set
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
        private set

    /**
     * Firebase can't cancel a verification, so each code sent starts a new attempt: the Activity
     * drops callbacks from an older one.
     */
    var attempt = 0
        private set

    private lateinit var verificationId: String
    private var resendTimer: Job? = null

    /** Returns the new attempt, for the Activity to compare its callbacks against. */
    fun sending(phone: String): Int {
        this.phone = phone
        screen = Screen.Loading
        return ++attempt
    }

    fun codeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
        verificationId = id
        resendToken = token
        screen = Screen.Code()
        canResend = false
        resendTimer?.cancel()
        resendTimer = viewModelScope.launch {
            delay(30.seconds)
            canResend = true
        }
    }

    fun verificationFailed(e: FirebaseException) {
        logFailure("Sending the code", e)
        screen = Screen.Phone(
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> R.string.invalid_phone
                is FirebaseTooManyRequestsException -> R.string.too_many_attempts
                else -> R.string.unavailable
            }
        )
    }

    fun submitCode(code: String) = signIn(PhoneAuthProvider.getCredential(verificationId, code))

    /** From the typed code, or from `onVerificationCompleted` when the SMS was read automatically. */
    fun signIn(credential: PhoneAuthCredential) {
        screen = Screen.Loading
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
            } catch (e: FirebaseException) {
                logFailure("Signing in with the code", e)
                val wrong = e is FirebaseAuthInvalidCredentialsException
                screen = Screen.Code(if (wrong) R.string.wrong_code else R.string.unavailable)
                return@launch
            }
            screen = Screen.SignedIn
        }
    }

    /** *Ieșire*. */
    fun signOut() {
        auth.signOut()
        screen = Screen.Phone()
    }
}
