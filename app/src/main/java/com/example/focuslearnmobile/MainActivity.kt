package com.example.focuslearnmobile

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.focuslearnmobile.data.local.LanguageManager
import com.example.focuslearnmobile.ui.auth.AuthScreen
import com.example.focuslearnmobile.ui.auth.AuthViewModel
import com.example.focuslearnmobile.ui.theme.FocusLearnMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.focuslearnmobile.ui.navigation.MainNavigationScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var languageManager: LanguageManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Застосовуємо збережену мову при старті
        lifecycleScope.launch {
            val savedLanguage = languageManager.getCurrentLanguage()
            languageManager.applyLanguageToActivity(this@MainActivity, savedLanguage)
        }

        setContent {
            FocusLearnMobileTheme {
                MainApp(
                    authViewModel = authViewModel,
                    onLanguageChanged = {
                        recreate()
                    }
                )
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        newBase?.let { context ->
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(
    authViewModel: AuthViewModel,
    onLanguageChanged: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()

    if (authState.isAuthenticated) {
        MainNavigationScreen(
            onLogout = { authViewModel.logout() },
            onLanguageChanged = onLanguageChanged
        )
    } else {
        AuthScreen(
            viewModel = authViewModel,
            onAuthSuccess = {
                // Навігація відбудеться автоматично через authState
            }
        )
    }
}