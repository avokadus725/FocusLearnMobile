package com.example.focuslearnmobile.data.model

import com.focuslearn.mobile.data.model.UserDTO
import com.google.gson.annotations.SerializedName

// Запит для прямої автентифікації (замість редиректів)
data class DirectAuthRequest(
    @SerializedName("provider")
    val provider: String, // "Google" або "Facebook"
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("photoUrl")
    val photoUrl: String?,
    @SerializedName("providerId")
    val providerId: String
)

// Відповідь з JWT токеном
data class AuthResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserDTO
)

// Стан авторизації
data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: UserDTO? = null,
    val token: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Результат OAuth операції
sealed class AuthResult {
    data class Success(val token: String, val user: UserDTO) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}