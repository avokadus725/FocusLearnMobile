package com.example.focuslearnmobile.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslearnmobile.data.local.TokenStorage
import com.example.focuslearnmobile.data.model.AuthState
import com.focuslearn.mobile.data.api.FocusLearnApi
import com.focuslearn.mobile.data.model.UserDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val api: FocusLearnApi
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Перевіряємо чи користувач вже авторизований
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val hasToken = tokenStorage.hasToken()
                if (hasToken) {
                    // Перевіряємо валідність токена через запит профілю
                    val token = tokenStorage.token.first()
                    if (!token.isNullOrEmpty()) {
                        validateToken(token)
                    } else {
                        _authState.value = AuthState(isAuthenticated = false)
                    }
                } else {
                    _authState.value = AuthState(isAuthenticated = false)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error checking auth status", e)
                _authState.value = AuthState(
                    isAuthenticated = false,
                    error = "Failed to check authentication status"
                )
            }
        }
    }

    private suspend fun validateToken(token: String) {
        try {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val response = api.getMyProfile("Bearer $token")
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    // Токен валідний, оновлюємо дані користувача
                    tokenStorage.saveUserData(userData)
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        user = userData,
                        token = token,
                        isLoading = false
                    )
                } else {
                    // Токен не валідний
                    clearAuthData()
                }
            } else {
                // Токен не валідний
                clearAuthData()
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error validating token", e)
            clearAuthData()
        }
    }

    fun handleAuthSuccess(token: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true, error = null)

                Log.d("AuthViewModel", "Handling auth success with token: ${token.take(20)}...")

                // Зберігаємо токен
                tokenStorage.saveToken(token)

                // Отримуємо дані користувача
                val response = api.getMyProfile("Bearer $token")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val userData = apiResponse.data

                        // Зберігаємо дані користувача
                        tokenStorage.saveUserData(userData)

                        _authState.value = AuthState(
                            isAuthenticated = true,
                            user = userData,
                            token = token,
                            isLoading = false
                        )

                        Log.d("AuthViewModel", "Auth success: User ${userData.userName} logged in")
                    } else {
                        handleAuthError("Failed to get user profile: ${apiResponse?.message}")
                    }
                } else {
                    handleAuthError("Failed to get user profile: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error handling auth success", e)
                handleAuthError("Authentication failed: ${e.message}")
            }
        }
    }

    fun handleAuthError(error: String) {
        Log.e("AuthViewModel", "Auth error: $error")
        _authState.value = AuthState(
            isAuthenticated = false,
            isLoading = false,
            error = error
        )
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                clearAuthData()
                Log.d("AuthViewModel", "User logged out")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout", e)
            }
        }
    }

    private suspend fun clearAuthData() {
        tokenStorage.clearAll()
        _authState.value = AuthState(
            isAuthenticated = false,
            isLoading = false
        )
    }

    // Методи для отримання даних користувача
    fun getUserData(): Flow<UserDTO?> = tokenStorage.userData

    fun getToken(): Flow<String?> = tokenStorage.token

    fun getUserId(): Flow<String?> = tokenStorage.userId
}