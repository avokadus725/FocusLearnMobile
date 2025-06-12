// app/src/main/java/com/example/focuslearnmobile/data/model/TimerModels.kt
package com.example.focuslearnmobile.data.model

import com.focuslearn.mobile.data.model.ApiResponse
import com.google.gson.annotations.SerializedName

// Запит для старту сесії
data class StartSessionRequest(
    @SerializedName("methodId")
    val methodId: Int
)

// Локальний стан таймера для UI
data class TimerState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val currentPhase: TimerPhase = TimerPhase.WORK,
    val methodTitle: String = "",
    val remainingSeconds: Int = 0,
    val totalPhaseSeconds: Int = 0,
    val currentCycle: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class TimerPhase {
    WORK, BREAK
}

// API Response wrapper для Timer
data class TimerApiResponse<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("success")
    val success: Boolean = true
)