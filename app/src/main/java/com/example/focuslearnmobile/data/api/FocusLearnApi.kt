package com.focuslearn.mobile.data.api

import com.example.focuslearnmobile.data.model.StartSessionRequest
import com.focuslearn.mobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface FocusLearnApi {

    // === АВТОРИЗАЦІЯ ===
    @GET("users/my-profile")
    suspend fun getMyProfile(@Header("Authorization") token: String): Response<ApiResponse<UserDTO>>

    @PUT("users/my-profile")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Body updateProfile: UpdateProfileDTO
    ): Response<Unit>

    @GET("users")
    suspend fun getAllUsers(@Header("Authorization") token: String): Response<List<UserDTO>>

    @GET("users/tutors")
    suspend fun getAllTutors(): Response<List<UserDTO>>

    // === МЕТОДИКИ КОНЦЕНТРАЦІЇ ===
    @GET("concentrationmethods")
    suspend fun getAllMethods(): Response<ApiResponse<List<ConcentrationMethod>>>

    @GET("concentrationmethods/{id}")
    suspend fun getMethodById(@Path("id") methodId: Int): Response<ApiResponse<ConcentrationMethod>>

    // === ТАЙМЕР ===
    @POST("timer/start")
    suspend fun startSession(
        @Header("Authorization") token: String,
        @Body request: StartSessionRequest
    ): Response<ApiResponse<ActiveSessionDTO>>
    @GET("timer/status")
    suspend fun getSessionStatus(@Header("Authorization") token: String): Response<ApiResponse<ActiveSessionDTO>>

    @POST("timer/pause")
    suspend fun pauseSession(@Header("Authorization") token: String): Response<ApiResponse<ActiveSessionDTO>>

    @POST("timer/stop")
    suspend fun stopSession(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    @POST("timer/complete-phase")
    suspend fun completeCurrentPhase(@Header("Authorization") token: String): Response<ApiResponse<ActiveSessionDTO>>

    // === НАВЧАЛЬНІ МАТЕРІАЛИ ===
    @GET("learningmaterials")
    suspend fun getAllMaterials(): Response<ApiResponse<List<LearningMaterialDTO>>>

    @GET("learningmaterials/my-materials")
    suspend fun getMyMaterials(@Header("Authorization") token: String): Response<ApiResponse<List<LearningMaterialDTO>>>

    @GET("learningmaterials/{id}")
    suspend fun getMaterialById(@Path("id") materialId: Int): Response<ApiResponse<LearningMaterialDTO>>

    // === СТАТИСТИКА ===
    @GET("businesslogic/user-statistics")
    suspend fun getUserStatistics(
        @Header("Authorization") token: String,
        @Query("periodStartDate") periodStartDate: String,
        @Query("periodType") periodType: String
    ): Response<ApiResponse<UserStatisticsDTO>>

    @GET("businesslogic/productivity-coefficient")
    suspend fun getProductivityCoefficient(
        @Header("Authorization") token: String,
        @Query("periodStartDate") periodStartDate: String,
        @Query("periodType") periodType: String
    ): Response<ApiResponse<ProductivityCoefficientResponse>>

    @GET("businesslogic/most-effective-method")
    suspend fun getMostEffectiveMethod(
        @Header("Authorization") token: String,
        @Query("periodStartDate") periodStartDate: String,
        @Query("periodType") periodType: String
    ): Response<ApiResponse<MostEffectiveMethodResponse>>
}