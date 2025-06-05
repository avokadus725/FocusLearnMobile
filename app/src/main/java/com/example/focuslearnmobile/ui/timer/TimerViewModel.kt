// app/src/main/java/com/example/focuslearnmobile/ui/timer/TimerViewModel.kt
package com.example.focuslearnmobile.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslearnmobile.data.model.TimerState
import com.example.focuslearnmobile.data.model.TimerPhase
import com.focuslearn.mobile.data.model.ActiveSessionDTO
import com.focuslearn.mobile.data.model.ConcentrationMethod
import com.focuslearn.mobile.data.repository.FocusLearnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.focuslearnmobile.data.local.IoTSettingsManager

@HiltViewModel
class TimerViewModel @Inject constructor(
    val repository: FocusLearnRepository,
    private val iotSettingsManager: IoTSettingsManager
) : ViewModel() {

    // Стан таймера
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    // Стан для UI
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // Job для періодичного оновлення
    private var updateJob: Job? = null
    private var localTickerJob: Job? = null

    val isIoTEnabled: StateFlow<Boolean> = iotSettingsManager.isIoTEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Перевіряємо активну сесію при старті
        checkActiveSession()
    }

    // Перевірка активної сесії
    private fun checkActiveSession() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                when (val result = repository.getTimerStatus()) {
                    is FocusLearnRepository.Result.Success -> {
                        result.data?.let { session ->
                            // Є активна сесія, оновлюємо стан і запускаємо оновлення
                            updateTimerStateFromSession(session)
                            startPeriodicUpdates()
                            startLocalTicker()
                        }
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    is FocusLearnRepository.Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error checking session: ${e.localizedMessage}"
                )
            }
        }
    }

    // Запуск сесії з обраною методикою
    fun startSession(method: ConcentrationMethod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = repository.startTimerSession(method.methodId)) {
                is FocusLearnRepository.Result.Success -> {
                    updateTimerStateFromSession(result.data)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedMethod = method
                    )
                    startPeriodicUpdates()
                    startLocalTicker()
                }
                is FocusLearnRepository.Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    // Пауза/відновлення сесії
    fun togglePause() {
        viewModelScope.launch {
            when (val result = repository.pauseTimerSession()) {
                is FocusLearnRepository.Result.Success -> {
                    updateTimerStateFromSession(result.data)
                    if (result.data.isPaused) {
                        stopLocalTicker()
                    } else {
                        startLocalTicker()
                    }
                }
                is FocusLearnRepository.Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    // Зупинка сесії
    fun stopSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val iotEnabled = iotSettingsManager.isIoTCurrentlyEnabled()

            if (iotEnabled) {
                // Якщо IoT увімкнений - просто скидаємо локальний стан
                _timerState.value = TimerState()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedMethod = null,
                    iotMessage = "Сесію зупинено локально. IoT пристрій продовжує запис автоматично."
                )
                stopPeriodicUpdates()
                stopLocalTicker()
            } else {
                // Якщо IoT вимкнений - зупиняємо через API
                when (val result = repository.stopTimerSession()) {
                    is FocusLearnRepository.Result.Success -> {
                        _timerState.value = TimerState()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            selectedMethod = null
                        )
                        stopPeriodicUpdates()
                        stopLocalTicker()
                    }
                    is FocusLearnRepository.Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }
    fun clearIoTMessage() {
        _uiState.value = _uiState.value.copy(iotMessage = null)
    }

    // Завершення поточної фази
    fun completeCurrentPhase() {
        viewModelScope.launch {
            val iotEnabled = iotSettingsManager.isIoTCurrentlyEnabled()

            if (iotEnabled) {
                // Якщо IoT увімкнений - тільки оновлюємо UI, не записуємо в БД
                completePhaseUIOnly()
            } else {
                // Якщо IoT вимкнений - записуємо через API як зазвичай
                completePhaseWithAPI()
            }
        }
    }

    private suspend fun completePhaseUIOnly() {
        val currentState = _timerState.value

        // Переключаємо фазу локально
        val newPhase = if (currentState.currentPhase == TimerPhase.WORK) {
            TimerPhase.BREAK
        } else {
            TimerPhase.WORK
        }

        // Оновлюємо стан без API виклику
        _timerState.value = currentState.copy(
            currentPhase = newPhase,
            remainingSeconds = if (newPhase == TimerPhase.WORK) {
                currentState.totalPhaseSeconds // Повертаємося до робочого часу
            } else {
                // Час перерви (можна додати логіку отримання з методики)
                300 // 5 хвилин за замовчуванням
            },
            currentCycle = if (newPhase == TimerPhase.WORK) {
                currentState.currentCycle + 1
            } else {
                currentState.currentCycle
            }
        )

        // Показуємо повідомлення користувачу
        _uiState.value = _uiState.value.copy(
            iotMessage = if (newPhase == TimerPhase.BREAK) {
                "Фазу роботи завершено! Дані автоматично записані через IoT пристрій."
            } else {
                "Перерва завершена! Почніть нову робочу фазу."
            }
        )
    }

    private suspend fun completePhaseWithAPI() {
        when (val result = repository.completeCurrentPhase()) {
            is FocusLearnRepository.Result.Success -> {
                updateTimerStateFromSession(result.data)
            }
            is FocusLearnRepository.Result.Error -> {
                _uiState.value = _uiState.value.copy(error = result.message)
            }
            else -> {}
        }
    }

    // Очистка помилки
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Оновлення локального стану з серверної сесії
    private fun updateTimerStateFromSession(session: ActiveSessionDTO) {
        val phase = when (session.currentPhase?.lowercase()) {
            "work" -> TimerPhase.WORK
            "break" -> TimerPhase.BREAK
            else -> TimerPhase.WORK // За замовчуванням Work, якщо currentPhase null або невідомий
        }

        _timerState.value = TimerState(
            isActive = session.isActive,
            isPaused = session.isPaused,
            currentPhase = phase,
            methodTitle = session.methodTitle.ifEmpty { "Unknown Method" }, // Захист від пустої назви
            remainingSeconds = session.remainingSeconds,
            totalPhaseSeconds = session.phaseDurationMinutes * 60,
            currentCycle = session.currentCycle,
            isLoading = false,
            error = null
        )
    }

    // Періодичне оновлення з сервера (кожні 30 секунд)
    private fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                delay(30_000) // 30 секунд
                if (timerState.value.isActive) {
                    repository.getTimerStatus()
                } else {
                    break
                }
            }
        }
    }

    // Локальний тікер для плавного відображення (кожну секунду)
    private fun startLocalTicker() {
        localTickerJob?.cancel()
        localTickerJob = viewModelScope.launch {
            while (true) {
                delay(1_000) // 1 секунда
                val currentState = timerState.value
                if (currentState.isActive && !currentState.isPaused) {
                    _timerState.value = currentState.copy(
                        remainingSeconds = maxOf(0, currentState.remainingSeconds - 1)
                    )

                    // Перевіряємо чи час не закінчився
                    if (currentState.remainingSeconds <= 1) {
                        // Час фази закінчився, завершуємо автоматично
                        completeCurrentPhase()
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun stopPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    private fun stopLocalTicker() {
        localTickerJob?.cancel()
        localTickerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPeriodicUpdates()
        stopLocalTicker()
    }
}

data class TimerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMethod: ConcentrationMethod? = null,
    val iotMessage: String? = null
)