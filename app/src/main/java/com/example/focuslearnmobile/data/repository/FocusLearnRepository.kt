package com.focuslearn.mobile.data.repository

import com.focuslearn.mobile.data.api.FocusLearnApi
import com.focuslearn.mobile.data.model.*
import com.example.focuslearnmobile.data.local.TokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusLearnRepository @Inject constructor(
    private val api: FocusLearnApi,
    private val tokenStorage: TokenStorage
) {

    // Результат операції
    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val message: String) : Result<T>()
        data class Loading<T>(val isLoading: Boolean) : Result<T>()
    }

    // Приватний метод для отримання токена з Bearer префіксом
    private suspend fun getAuthToken(): String? {
        val token = tokenStorage.token.first()
        return if (!token.isNullOrEmpty()) "Bearer $token" else null
    }

    // === КОРИСТУВАЧІ ===
    suspend fun getMyProfile(): Result<UserDTO> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<UserDTO>("No authentication token")

            val response = api.getMyProfile(authToken)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to get profile")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun updateMyProfile(updateProfile: UpdateProfileDTO): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<Unit>("No authentication token")

            val response = api.updateMyProfile(authToken, updateProfile)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to update profile: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getAllUsers(): Result<List<UserDTO>> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<List<UserDTO>>("No authentication token")

            val response = api.getAllUsers(authToken)
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

    suspend fun getAllTutors(): Result<List<UserDTO>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllTutors()
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
    suspend fun startSession(methodId: Int): Result<ActiveSessionDTO> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<ActiveSessionDTO>("No authentication token")

            val response = api.startSession(authToken, StartSessionRequest(methodId))
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

    suspend fun getSessionStatus(): Result<ActiveSessionDTO?> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<ActiveSessionDTO?>("No authentication token")

            val response = api.getSessionStatus(authToken)
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

    suspend fun pauseSession(): Result<ActiveSessionDTO> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<ActiveSessionDTO>("No authentication token")

            val response = api.pauseSession(authToken)
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

    suspend fun stopSession(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<Unit>("No authentication token")

            val response = api.stopSession(authToken)
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

    suspend fun completeCurrentPhase(): Result<ActiveSessionDTO> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<ActiveSessionDTO>("No authentication token")

            val response = api.completeCurrentPhase(authToken)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to complete phase")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to complete phase: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    // === ЗАВДАННЯ ===
    suspend fun getAvailableAssignments(): Result<List<AssignmentDTO>> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<List<AssignmentDTO>>("No authentication token")

            val response = api.getAvailableAssignments(authToken)
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

    suspend fun getMyAssignments(): Result<List<AssignmentDTO>> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<List<AssignmentDTO>>("No authentication token")

            val response = api.getMyAssignments(authToken)
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

    suspend fun getAssignmentById(assignmentId: Int): Result<AssignmentDTO> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<AssignmentDTO>("No authentication token")

            val response = api.getAssignmentById(assignmentId, authToken)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Assignment not found")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun takeAssignment(assignmentId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<Unit>("No authentication token")

            val response = api.takeAssignment(assignmentId, authToken)
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

    suspend fun submitAssignment(assignmentId: Int, fileLink: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<Unit>("No authentication token")

            val request = SubmitAssignmentRequest(fileLink)
            val response = api.submitAssignment(assignmentId, authToken, request)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to submit assignment")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to submit assignment: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun completeAssignment(assignmentId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<Unit>("No authentication token")

            val response = api.completeAssignment(assignmentId, authToken)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        Result.Success(Unit)
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to complete assignment")
                    }
                } ?: Result.Error("Empty response")
            } else {
                Result.Error("Failed to complete assignment: ${response.code()}")
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

    suspend fun getMyMaterials(): Result<List<LearningMaterialDTO>> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<List<LearningMaterialDTO>>("No authentication token")

            val response = api.getMyMaterials(authToken)
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

    suspend fun getMaterialById(materialId: Int): Result<LearningMaterialDTO> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMaterialById(materialId)
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        Result.Success(apiResponse.data)
                    } else {
                        Result.Error(apiResponse.message ?: "Material not found")
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
    suspend fun getUserStatistics(periodStartDate: String, periodType: String): Result<UserStatisticsDTO> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<UserStatisticsDTO>("No authentication token")

            val response = api.getUserStatistics(authToken, periodStartDate, periodType)
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

    suspend fun getProductivityCoefficient(periodStartDate: String, periodType: String): Result<ProductivityCoefficientResponse> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<ProductivityCoefficientResponse>("No authentication token")

            val response = api.getProductivityCoefficient(authToken, periodStartDate, periodType)
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

    suspend fun getMostEffectiveMethod(periodStartDate: String, periodType: String): Result<MostEffectiveMethodResponse> = withContext(Dispatchers.IO) {
        try {
            val authToken = getAuthToken()
                ?: return@withContext Result.Error<MostEffectiveMethodResponse>("No authentication token")

            val response = api.getMostEffectiveMethod(authToken, periodStartDate, periodType)
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