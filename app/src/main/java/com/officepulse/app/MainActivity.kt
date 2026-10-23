package com.officepulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.officepulse.app.ui.theme.OfficePulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OfficePulseTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavHost(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel()
) {
    val state by authViewModel.uiState.collectAsStateWithLifecycle()
    val isSignedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

    // Reset navigation when the user signs in or logs out.
    key(isSignedIn) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = if (isSignedIn) "home" else "login",
            modifier = modifier
        ) {
            composable("login") {
                LoginScreen(
                    state = state,
                    onEmailChange = authViewModel::updateEmail,
                    onPasswordChange = authViewModel::updatePassword,
                    onLogin = authViewModel::login,
                    onOpenRegister = {
                        authViewModel.clearErrors()
                        navController.navigate("register") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    state = state,
                    onEmailChange = authViewModel::updateEmail,
                    onPasswordChange = authViewModel::updatePassword,
                    onConfirmationChange = authViewModel::updateConfirmation,
                    onRegister = authViewModel::register,
                    onOpenLogin = {
                        authViewModel.clearErrors()
                        navController.popBackStack()
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    email = state.userEmail.orEmpty(),
                    onLogout = authViewModel::logout
                )
            }
        }
    }
}
