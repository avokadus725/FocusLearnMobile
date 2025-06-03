package com.focuslearn.mobile.data.repository

import com.example.focuslearnmobile.data.api.FocusLearnApi
import com.focuslearn.mobile.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusLearnRepository @Inject constructor(
    private val api: FocusLearnApi
) {

    // Результат операції
    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val message: String) : Result<T>()
        data class Loading<T>(val isLoading: Boolean) : Result<T>()
    }

    // === КОРИСТУВАЧІ ===
    suspend fun getMyProfile(token: String): Result<UserDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyProfile("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun updateMyProfile(token: String, updateProfile: UpdateProfileDTO): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateMyProfile("Bearer $token", updateProfile)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to update profile: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    // === МЕТОДИКИ КОНЦЕНТРАЦІЇ ===
    suspend fun getAllMethods(): Result<List<ConcentrationMethod>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllMethods()
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to load methods")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getMethodById(methodId: Int): Result<ConcentrationMethod> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMethodById(methodId)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Method not found")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    // === ТАЙМЕР ===
    suspend fun startSession(token: String, methodId: Int): Result<ActiveSessionDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.startSession("Bearer $token", StartSessionRequest(methodId))
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to start session")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to start session: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getSessionStatus(token: String): Result<ActiveSessionDTO?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getSessionStatus("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to get session status")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to get session status: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun pauseSession(token: String): Result<ActiveSessionDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.pauseSession("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to pause session")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to pause session: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun stopSession(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.stopSession("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to stop session")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to stop session: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    // === ЗАВДАННЯ ===
    suspend fun getAvailableAssignments(token: String): Result<List<AssignmentDTO>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAvailableAssignments("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to load assignments")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getMyAssignments(token: String): Result<List<AssignmentDTO>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyAssignments("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to load assignments")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun takeAssignment(token: String, assignmentId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.takeAssignment(assignmentId, "Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to take assignment")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to take assignment: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    // === НАВЧАЛЬНІ МАТЕРІАЛИ ===
    suspend fun getAllMaterials(): Result<List<LearningMaterialDTO>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllMaterials()
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to load materials")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    // === СТАТИСТИКА ===
    suspend fun getUserStatistics(token: String, periodStartDate: String, periodType: String): Result<UserStatisticsDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserStatistics("Bearer $token", periodStartDate, periodType)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }
}