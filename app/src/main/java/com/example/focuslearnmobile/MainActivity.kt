package com.example.focuslearnmobile

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslearn.mobile.data.repository.FocusLearnRepository
import com.example.focuslearnmobile.ui.theme.FocusLearnMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FocusLearnRepository
) : ViewModel() {

    private val _message = MutableStateFlow("Натисніть кнопку для тестування")
    val message: StateFlow<String> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun testGetMethods() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = "Завантаження методик..."

            when (val result = repository.getAllMethods()) {
                is FocusLearnRepository.Result.Success -> {
                    _message.value = "✅ Успіх! Завантажено ${result.data.size} методик:\n" +
                            result.data.joinToString("\n") { "• ${it.title} (${it.workDuration}/${it.breakDuration} хв)" }
                    Log.d("API_TEST", "Методики завантажено: ${result.data.size}")
                }
                is FocusLearnRepository.Result.Error -> {
                    _message.value = "❌ Помилка: ${result.message}"
                    Log.e("API_TEST", "Помилка завантаження методик: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun testGetMaterials() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = "Завантаження матеріалів..."

            when (val result = repository.getAllMaterials()) {
                is FocusLearnRepository.Result.Success -> {
                    _message.value = "✅ Успіх! Завантажено ${result.data.size} навчальних матеріалів:\n" +
                            result.data.joinToString("\n") { "• ${it.title}" }
                    Log.d("API_TEST", "Матеріали завантажено: ${result.data.size}")
                }
                is FocusLearnRepository.Result.Error -> {
                    _message.value = "❌ Помилка: ${result.message}"
                    Log.e("API_TEST", "Помилка завантаження матеріалів: ${result.message}")
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FocusLearn Mobile",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Тестування API endpoints",
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
                Text("Тест Методики")
            }

            Button(
                onClick = { viewModel.testGetMaterials() },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Тест Матеріали")
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