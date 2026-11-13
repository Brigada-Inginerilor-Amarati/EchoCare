package ro.echocare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import ro.echocare.auth.AuthViewModel
import ro.echocare.auth.Landing
import ro.echocare.auth.Screen
import ro.echocare.auth.toE164

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AuthViewModel = hiltViewModel()
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (vm.screen == Screen.Landing) {
                        Landing(vm::openSignIn, Modifier.padding(innerPadding))
                        return@Scaffold
                    }
                    SignIn(
                        vm,
                        onSendCode = { sendCode(vm, it, null) },
                        onResend = { sendCode(vm, vm.phone, vm.resendToken) },
                        // The keyboard must not cover the code field or its button
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .imePadding()
                    )
                }
            }
        }
    }

    // PhoneAuthOptions needs an Activity for the reCAPTCHA fallback, so verification starts here
    // and the ViewModel only receives the results (auth-layer §8.2).
    private fun sendCode(
        vm: AuthViewModel,
        phone: String,
        resend: PhoneAuthProvider.ForceResendingToken?
    ) {
        val attempt = vm.sending(phone)

        // Late callbacks from an attempt replaced by a resend change nothing
        fun current() = attempt == vm.attempt
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    // Automatic SMS reading: sign in straight away.
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        if (current()) vm.signIn(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        if (current()) vm.verificationFailed(e)
                    }

                    override fun onCodeSent(
                        id: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        if (current()) vm.codeSent(id, token)
                    }
                }
            )
        resend?.let(options::setForceResendingToken)
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }
}

private val ROLE_LABELS = mapOf(
    "das" to R.string.role_das,
    "medic" to R.string.role_medic,
    "caregiver" to R.string.role_caregiver,
    "senior" to R.string.role_senior
)

@Composable
fun SignIn(
    vm: AuthViewModel,
    onSendCode: (String) -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        val screen = vm.screen
        // Before anyone is signed in, back returns to the landing page
        val beforeSignIn =
            screen is Screen.Phone || screen is Screen.Code || screen == Screen.NotRegistered
        BackHandler(enabled = beforeSignIn, onBack = vm::backToLanding)
        when (screen) {
            // Shown by MainActivity, not here.
            Screen.Landing -> {}
            Screen.Loading -> CircularProgressIndicator(
                Modifier.align(Alignment.CenterHorizontally)
            )
            is Screen.Phone -> PhoneForm(screen.error, onSendCode)
            // Signed out already: the phone form, under the not-registered text (auth-layer §3.2).
            Screen.NotRegistered -> PhoneForm(R.string.not_registered, onSendCode)
            is Screen.Code -> CodeForm(screen.error, vm.canResend, vm::submitCode, onResend)
            Screen.Unavailable -> {
                ErrorText(R.string.unavailable)
                Button(vm::loadMe, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.retry))
                }
                Button(vm::signOut, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.sign_out))
                }
            }
            is Screen.PickRole -> {
                Title(R.string.pick_role_title)
                screen.roles.forEach { role ->
                    Button({ vm.pickRole(role) }, Modifier.fillMaxWidth()) {
                        Text(stringResource(ROLE_LABELS.getValue(role)))
                    }
                }
            }
            // A placeholder: the screens of each role come later.
            is Screen.Home -> {
                Text(
                    stringResource(
                        R.string.home_signed_in,
                        screen.displayName,
                        stringResource(ROLE_LABELS.getValue(screen.role))
                    )
                )
                if (screen.canSwitch) {
                    Button(vm::switchRole, Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.change_role))
                    }
                }
                Button(vm::signOut, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.sign_out))
                }
            }
        }
        if (beforeSignIn) {
            Button(vm::backToLanding, Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.back))
            }
        }
    }
}

@Composable
private fun Title(text: Int) {
    Text(stringResource(text), style = MaterialTheme.typography.headlineSmall)
}

@Composable
private fun ErrorText(text: Int) {
    Text(
        stringResource(text),
        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun PhoneForm(error: Int?, onSendCode: (String) -> Unit) {
    var phone by rememberSaveable { mutableStateOf("") }
    Title(R.string.sign_in_title)
    OutlinedTextField(
        phone,
        { phone = it },
        Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.phone_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true
    )
    error?.let { ErrorText(it) }
    Button(
        { onSendCode(toE164(phone)) },
        Modifier.fillMaxWidth(),
        enabled = phone.isNotBlank()
    ) { Text(stringResource(R.string.send_code)) }
}

@Composable
private fun CodeForm(
    error: Int?,
    canResend: Boolean,
    onConfirm: (String) -> Unit,
    onResend: () -> Unit
) {
    var code by rememberSaveable { mutableStateOf("") }
    Title(R.string.sign_in_title)
    OutlinedTextField(
        code,
        { code = it },
        Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.code_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true
    )
    error?.let { ErrorText(it) }
    Button({ onConfirm(code) }, Modifier.fillMaxWidth(), enabled = code.isNotBlank()) {
        Text(stringResource(R.string.confirm))
    }
    Button(onResend, Modifier.fillMaxWidth(), enabled = canResend) {
        Text(stringResource(R.string.resend_code))
    }
}
