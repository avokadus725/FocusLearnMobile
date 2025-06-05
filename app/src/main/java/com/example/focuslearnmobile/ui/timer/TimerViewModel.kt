// app/src/main/java/com/example/focuslearnmobile/ui/timer/TimerViewModel.kt
package com.example.focuslearnmobile.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslearnmobile.data.model.TimerState
import com.example.focuslearnmobile.data.model.TimerPhase
import com.focuslearn.mobile.data.model.ConcentrationMethod
import com.focuslearn.mobile.data.repository.FocusLearnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    val repository: FocusLearnRepository
) : ViewModel() {

    // Стан таймера
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    // Стан для UI
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // Job для локального тікера (тільки для візуалізації)
    private var localTickerJob: Job? = null

    init {
        // Перевіряємо активну сесію при старті
        checkActiveSession()
    }

    // Перевірка активної сесії
    private fun checkActiveSession() {
        viewModelScope.launch {
            try {
                when (val result = repository.getTimerStatus()) {
                    is FocusLearnRepository.Result.Success -> {
                        result.data?.let { session ->
                            // Є активна сесія, відновлюємо стан
                            println("TimerViewModel: Found active session: ${session.methodTitle}")
                            updateTimerStateFromSession(session)
                            startLocalTicker()
                        }
                    }
                    is FocusLearnRepository.Result.Error -> {
                        println("TimerViewModel: Error checking session: ${result.message}")
                    }
                    else -> {
                        println("TimerViewModel: No active session found")
                    }
                }
            } catch (e: Exception) {
                println("TimerViewModel: Exception checking session: ${e.message}")
            }
        }
    }

    // Запуск сесії (перевіряємо активну сесію спочатку)
    fun startSession(method: ConcentrationMethod) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                println("TimerViewModel: Starting session with method: ${method.title}, ID: ${method.methodId}")

                // Спочатку перевіряємо чи немає активної сесії
                when (val statusResult = repository.getTimerStatus()) {
                    is FocusLearnRepository.Result.Success -> {
                        if (statusResult.data != null) {
                            // Вже є активна сесія - відновлюємо її
                            println("TimerViewModel: Active session found, restoring...")
                            updateTimerStateFromSession(statusResult.data)
                            startLocalTicker()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                iotMessage = "Відновлено активну сесію: ${statusResult.data.methodTitle}"
                            )
                            return@launch
                        }
                    }
                    is FocusLearnRepository.Result.Error -> {
                        // Помилка перевірки - спробуємо створити нову
                        println("TimerViewModel: Error checking status: ${statusResult.message}")
                    }
                    else -> {
                        println("TimerViewModel: No active session, creating new one...")
                    }
                }

                // Немає активної сесії - створюємо нову
                when (val result = repository.startTimerSession(method.methodId)) {
                    is FocusLearnRepository.Result.Success -> {
                        println("TimerViewModel: Session started successfully")

                        // Сесія успішно створена на сервері, IoT отримав сигнал
                        val workDurationSeconds = method.workDuration * 60

                        _timerState.value = TimerState(
                            isActive = true,
                            isPaused = false,
                            currentPhase = TimerPhase.WORK,
                            methodTitle = method.title,
                            remainingSeconds = workDurationSeconds,
                            totalPhaseSeconds = workDurationSeconds,
                            currentCycle = 1,
                            isLoading = false,
                            error = null
                        )

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            selectedMethod = method,
                            iotMessage = "Сесію розпочато! IoT пристрій отримав сигнал і розпочав запис даних."
                        )

                        startLocalTicker()
                    }
                    is FocusLearnRepository.Result.Error -> {
                        println("TimerViewModel: Session start failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Не вдалося розпочати сесію: ${result.message}"
                        )
                    }
                    else -> {
                        println("TimerViewModel: Session start unknown result")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Помилка запуску сесії"
                        )
                    }
                }
            } catch (e: Exception) {
                println("TimerViewModel: Exception in startSession: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Помилка: ${e.message}"
                )
            }
        }
    }

    // Зупинка сесії (повідомляємо сервер про завершення)
    fun stopSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Повідомляємо сервер про зупинку, щоб IoT також зупинився
            when (val result = repository.stopTimerSession()) {
                is FocusLearnRepository.Result.Success -> {
                    _timerState.value = TimerState()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedMethod = null,
                        iotMessage = "Сесію зупинено. IoT пристрій отримав сигнал і завершив запис."
                    )
                    stopLocalTicker()
                }
                is FocusLearnRepository.Result.Error -> {
                    // Навіть при помилці API, зупиняємо локально
                    _timerState.value = TimerState()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedMethod = null,
                        iotMessage = "Сесію зупинено локально. Можливі проблеми з IoT зв'язком."
                    )
                    stopLocalTicker()
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    // Автоматичне перемикання фаз для візуалізації (тільки Work → Break → Завершення)
    private fun switchToNextPhase() {
        val currentState = _timerState.value
        val currentMethod = _uiState.value.selectedMethod ?: return

        if (currentState.currentPhase == TimerPhase.WORK) {
            // Робоча фаза закінчилась → починається перерва
            val breakDurationSeconds = currentMethod.breakDuration * 60

            _timerState.value = currentState.copy(
                currentPhase = TimerPhase.BREAK,
                remainingSeconds = breakDurationSeconds,
                totalPhaseSeconds = breakDurationSeconds
            )

            _uiState.value = _uiState.value.copy(
                iotMessage = "Робочу фазу завершено! Починається перерва."
            )
        } else {
            // Перерва закінчилась → сесія завершена
            _timerState.value = TimerState()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                selectedMethod = null,
                iotMessage = "Сесію повністю завершено! Робота + перерва виконані."
            )
            stopLocalTicker()

            // Також повідомляємо сервер про завершення
            viewModelScope.launch {
                repository.stopTimerSession()
            }
        }
    }

    // Очистка повідомлень
    fun clearIoTMessage() {
        _uiState.value = _uiState.value.copy(iotMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Локальний тікер для візуалізації (кожну секунду)
    private fun startLocalTicker() {
        localTickerJob?.cancel()
        localTickerJob = viewModelScope.launch {
            while (true) {
                delay(1_000) // 1 секунда
                val currentState = timerState.value

                if (currentState.isActive) {
                    val newRemainingSeconds = maxOf(0, currentState.remainingSeconds - 1)

                    _timerState.value = currentState.copy(
                        remainingSeconds = newRemainingSeconds
                    )

                    // Коли час закінчується, автоматично переключаємо фазу для візуалізації
                    if (newRemainingSeconds <= 0) {
                        switchToNextPhase()
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun stopLocalTicker() {
        localTickerJob?.cancel()
        localTickerJob = null
    }

    // Оновлення локального стану з серверної сесії
    private fun updateTimerStateFromSession(session: com.focuslearn.mobile.data.model.ActiveSessionDTO) {
        val phase = when (session.currentPhase?.lowercase()) {
            "work" -> TimerPhase.WORK
            "break" -> TimerPhase.BREAK
            else -> TimerPhase.WORK
        }

        // Знаходимо методику для отримання повної інформації
        val selectedMethod = _uiState.value.selectedMethod

        _timerState.value = TimerState(
            isActive = session.isActive,
            isPaused = session.isPaused,
            currentPhase = phase,
            methodTitle = session.methodTitle.ifEmpty { "Unknown Method" },
            remainingSeconds = session.remainingSeconds,
            totalPhaseSeconds = session.phaseDurationMinutes * 60,
            currentCycle = session.currentCycle,
            isLoading = false,
            error = null
        )

        // Зберігаємо інформацію про методику для UI
        if (selectedMethod == null) {
            // Спробуємо створити об'єкт методики з даних сесії
            val reconstructedMethod = com.focuslearn.mobile.data.model.ConcentrationMethod(
                methodId = session.methodId,
                title = session.methodTitle,
                description = null,
                workDuration = session.workDurationMinutes,
                breakDuration = session.breakDurationMinutes,
                createdAt = null
            )

            _uiState.value = _uiState.value.copy(
                selectedMethod = reconstructedMethod
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocalTicker()
    }
}

data class TimerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMethod: ConcentrationMethod? = null,
    val iotMessage: String? = null
)