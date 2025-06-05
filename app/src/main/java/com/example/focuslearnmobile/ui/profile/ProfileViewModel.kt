// app/src/main/java/com/example/focuslearnmobile/ui/profile/ProfileViewModel.kt
package com.example.focuslearnmobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focuslearn.mobile.data.model.UpdateProfileDTO
import com.focuslearn.mobile.data.model.UserDTO
import com.focuslearn.mobile.data.repository.FocusLearnRepository
import com.example.focuslearnmobile.data.local.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val user: UserDTO? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: FocusLearnRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = repository.getMyProfile()) {
                is FocusLearnRepository.Result.Success -> {
                    // Зберігаємо оновлені дані користувача
                    tokenStorage.saveUserData(result.data)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = result.data
                    )
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

    fun refreshProfile() {
        loadProfile()
    }

    fun updateProfile(userName: String, profilePhoto: String?, language: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

            val updateRequest = UpdateProfileDTO(
                userName = userName.takeIf { it.isNotBlank() },
                profilePhoto = profilePhoto,
                language = language
            )

            when (val result = repository.updateMyProfile(updateRequest)) {
                is FocusLearnRepository.Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        successMessage = "Профіль успішно оновлено"
                    )
                    // Перезавантажуємо профіль для отримання оновлених даних
                    loadProfile()
                }
                is FocusLearnRepository.Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isUpdating = false)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}