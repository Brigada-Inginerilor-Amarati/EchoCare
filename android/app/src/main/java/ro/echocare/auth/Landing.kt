package ro.echocare.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ro.echocare.R

// The signed-out first screen: what EchoCare does (UC-09, UC-07, UC-11). *Intrați* leads on to the
// phone step.
@Composable
fun Landing(onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            stringResource(R.string.landing_title_start) + " " +
                stringResource(R.string.landing_title_end),
            Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge
        )
        Text(stringResource(R.string.landing_lead), style = MaterialTheme.typography.bodyLarge)
        Button(onSignIn, Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.landing_sign_in))
        }
        Text(
            stringResource(R.string.landing_accounts),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
