package ro.echocare.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ro.echocare.R
import ro.echocare.ROLE_LABELS
import ro.echocare.auth.Screen
import ro.echocare.ui.Kind
import ro.echocare.ui.SnButton
import ro.echocare.ui.theme.Palette

/**
 * The signed-in app: the role's home and what opens from it (seniors §8.2). Permissions are
 * FastAPI's; this only picks screens (G3).
 */
@Composable
fun HomeNav(home: Screen.Home, onSwitchRole: () -> Unit, onSignOut: () -> Unit) {
    // A new graph for a new role, so that role's home is the start destination.
    key(home.role) {
        val nav = rememberNavController()
        NavHost(nav, startDestination = homeFor(home.role)) {
            composable<SeniorsRoute> { RoleHome(home, onSwitchRole, onSignOut) }
            composable<OwnRoute> { RoleHome(home, onSwitchRole, onSignOut) }
            composable<IncidentsRoute> { RoleHome(home, onSwitchRole, onSignOut) }
        }
    }
}

// A placeholder: the screens of each role come later.
@Composable
private fun RoleHome(home: Screen.Home, onSwitchRole: () -> Unit, onSignOut: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Palette.Canvas) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(
                stringResource(
                    R.string.home_signed_in,
                    home.displayName,
                    stringResource(ROLE_LABELS.getValue(home.role))
                ),
                color = Palette.Fg,
                style = MaterialTheme.typography.bodyLarge
            )
            if (home.canSwitch) {
                SnButton(
                    stringResource(R.string.change_role),
                    onSwitchRole,
                    Modifier.fillMaxWidth()
                )
            }
            SnButton(
                stringResource(R.string.sign_out),
                onSignOut,
                Modifier.fillMaxWidth(),
                kind = Kind.Outline
            )
        }
    }
}
