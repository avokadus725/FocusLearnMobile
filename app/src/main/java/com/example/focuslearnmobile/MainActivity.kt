package com.example.focuslearnmobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslearnmobile.R
import com.example.focuslearnmobile.data.local.LanguageManager
import com.example.focuslearnmobile.ui.auth.AuthScreen
import com.example.focuslearnmobile.ui.auth.AuthViewModel
import com.focuslearn.mobile.data.repository.FocusLearnRepository
import com.example.focuslearnmobile.ui.theme.FocusLearnMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnMobileTheme {
                MainApp(
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
fun MainApp(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    if (authState.isAuthenticated) {
        MainScreen(
            viewModel = mainViewModel,
            onLogout = { authViewModel.logout() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Language button
                    IconButton(
                        onClick = { showLanguageDialog = true }
                    ) {

                    }

                    // Logout button
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.api_endpoints_testing),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.testGetMethods() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.test_methods))
                }

                Button(
                    onClick = { viewModel.testGetMaterials() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.test_materials))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { languageCode ->
                viewModel.changeLanguage(languageCode)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        LanguageManager.LANGUAGE_ENGLISH to "English",
        LanguageManager.LANGUAGE_UKRAINIAN to "Українська"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = { onLanguageSelected(code) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back))
            }
        }
    )
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FocusLearnRepository,
    private val languageManager: LanguageManager
) : ViewModel() {

    private val _message = MutableStateFlow("Press button to test")
    val message: StateFlow<String> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val currentLanguage: StateFlow<String> = languageManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LanguageManager.LANGUAGE_ENGLISH
    )

    fun testGetMethods() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = "Loading methods…"

            when (val result = repository.getAllMethods()) {
                is FocusLearnRepository.Result.Success -> {
                    _message.value = "✅ Success! Loaded ${result.data.size} methods:\n" +
                            result.data.joinToString("\n") { "• ${it.title} (${it.workDuration}/${it.breakDuration} min)" }
                    Log.d("API_TEST", "Methods loaded: ${result.data.size}")
                }
                is FocusLearnRepository.Result.Error -> {
                    _message.value = "❌ Error: ${result.message}"
                    Log.e("API_TEST", "Methods loading error: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun testGetMaterials() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = "Loading materials…"

            when (val result = repository.getAllMaterials()) {
                is FocusLearnRepository.Result.Success -> {
                    _message.value = "✅ Success! Loaded ${result.data.size} learning materials:\n" +
                            result.data.joinToString("\n") { "• ${it.title}" }
                    Log.d("API_TEST", "Materials loaded: ${result.data.size}")
                }
                is FocusLearnRepository.Result.Error -> {
                    _message.value = "❌ Error: ${result.message}"
                    Log.e("API_TEST", "Materials loading error: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            languageManager.setLanguage(languageCode)
        }
    }
}