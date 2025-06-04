// app/src/main/java/com/example/focuslearnmobile/data/model/TimerModels.kt
package com.example.focuslearnmobile.data.model

import com.focuslearn.mobile.data.model.ApiResponse
import com.google.gson.annotations.SerializedName

// Запит для старту сесії
data class StartSessionRequest(
    @SerializedName("methodId")
    val methodId: Int
)

// Активна сесія (з сервера)
data class ActiveSessionDTO(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("methodId")
    val methodId: Int,
    @SerializedName("methodTitle")
    val methodTitle: String,
    @SerializedName("currentPhase")
    val currentPhase: String, // "Work" або "Break"
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("pauseStartTime")
    val pauseStartTime: String?,
    @SerializedName("totalPausedSeconds")
    val totalPausedSeconds: Int,
    @SerializedName("phaseDurationMinutes")
    val phaseDurationMinutes: Int,
    @SerializedName("workDurationMinutes")
    val workDurationMinutes: Int,
    @SerializedName("breakDurationMinutes")
    val breakDurationMinutes: Int,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("isPaused")
    val isPaused: Boolean,
    @SerializedName("remainingSeconds")
    val remainingSeconds: Int,
    @SerializedName("elapsedSeconds")
    val elapsedSeconds: Int,
    @SerializedName("phaseStartTime")
    val phaseStartTime: String?,
    @SerializedName("currentCycle")
    val currentCycle: Int
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

// Відповідь статусу сесії
data class SessionStatusResponse(
    @SerializedName("isActive")
    val isActive: Boolean = false,
    @SerializedName("userId")
    val userId: Int? = null,
    @SerializedName("methodId")
    val methodId: Int? = null,
    @SerializedName("methodTitle")
    val methodTitle: String? = null,
    @SerializedName("currentPhase")
    val currentPhase: String? = null,
    @SerializedName("startTime")
    val startTime: String? = null,
    @SerializedName("pauseStartTime")
    val pauseStartTime: String? = null,
    @SerializedName("totalPausedSeconds")
    val totalPausedSeconds: Int? = null,
    @SerializedName("phaseDurationMinutes")
    val phaseDurationMinutes: Int? = null,
    @SerializedName("workDurationMinutes")
    val workDurationMinutes: Int? = null,
    @SerializedName("breakDurationMinutes")
    val breakDurationMinutes: Int? = null,
    @SerializedName("isPaused")
    val isPaused: Boolean? = null,
    @SerializedName("remainingSeconds")
    val remainingSeconds: Int? = null,
    @SerializedName("elapsedSeconds")
    val elapsedSeconds: Int? = null,
    @SerializedName("phaseStartTime")
    val phaseStartTime: String? = null,
    @SerializedName("currentCycle")
    val currentCycle: Int? = null
)

// API Response wrapper для Timer
data class TimerApiResponse<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("success")
    val success: Boolean = true
)