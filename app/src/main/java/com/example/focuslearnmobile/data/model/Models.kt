package com.focuslearn.mobile.data.model

import com.google.gson.annotations.SerializedName

// Базова відповідь API сервера
data class ApiResponse<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("language")
    val language: String?
)

// Користувач (відповідає UserDTO з сервера)
data class UserDTO(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("profilePhoto")
    val profilePhoto: String?,
    @SerializedName("language")
    val language: String?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("profileStatus")
    val profileStatus: String?
)

// Методика концентрації
data class ConcentrationMethod(
    @SerializedName("methodId")
    val methodId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("workDuration")
    val workDuration: Int,
    @SerializedName("breakDuration")
    val breakDuration: Int,
    @SerializedName("createdAt")
    val createdAt: String?
)

// Завдання
data class AssignmentDTO(
    @SerializedName("assignmentId")
    val assignmentId: Int?,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("fileLink")
    val fileLink: String?,
    @SerializedName("studentId")
    val studentId: Int?,
    @SerializedName("tutorId")
    val tutorId: Int,
    @SerializedName("taskId")
    val taskId: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("dueDate")
    val dueDate: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("rating")
    val rating: Int?,
    @SerializedName("tutorName")
    val tutorName: String?
)

// Навчальні матеріали
data class LearningMaterialDTO(
    @SerializedName("materialId")
    val materialId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("fileLink")
    val fileLink: String?,
    @SerializedName("creatorId")
    val creatorId: Int,
    @SerializedName("addedAt")
    val addedAt: String?,
    @SerializedName("tutorName")
    val tutorName: String?
)

// Активна сесія таймера
data class ActiveSessionDTO(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("methodId")
    val methodId: Int,
    @SerializedName("methodTitle")
    val methodTitle: String,
    @SerializedName("currentPhase")
    val currentPhase: String,
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

// Статистика користувача
data class UserStatisticsDTO(
    @SerializedName("totalConcentrationTime")
    val totalConcentrationTime: Int,
    @SerializedName("breakCount")
    val breakCount: Int,
    @SerializedName("missedBreaks")
    val missedBreaks: Int
)

// Запити
data class StartSessionRequest(
    @SerializedName("methodId")
    val methodId: Int
)

data class SubmitAssignmentRequest(
    @SerializedName("fileLink")
    val fileLink: String
)

data class UpdateProfileDTO(
    @SerializedName("userName")
    val userName: String?,
    @SerializedName("profilePhoto")
    val profilePhoto: String?,
    @SerializedName("language")
    val language: String?
)

// Відповіді для статистики
data class ProductivityCoefficientResponse(
    @SerializedName("productivityCoefficient")
    val productivityCoefficient: Double
)

data class MostEffectiveMethodResponse(
    @SerializedName("message")
    val message: String
)