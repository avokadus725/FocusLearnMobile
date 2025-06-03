package com.example.focuslearnmobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.focuslearn.mobile.data.model.UserDTO
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

@Singleton
class TokenStorage @Inject constructor(
    private val context: Context
) {
    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_PHOTO_KEY = stringPreferencesKey("user_photo")
        private val USER_LANGUAGE_KEY = stringPreferencesKey("user_language")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_STATUS_KEY = stringPreferencesKey("user_status")
    }

    // Збереження токена
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    // Збереження даних користувача
    suspend fun saveUserData(user: UserDTO) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.userId.toString()
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_NAME_KEY] = user.userName
            user.profilePhoto?.let { preferences[USER_PHOTO_KEY] = it }
            user.language?.let { preferences[USER_LANGUAGE_KEY] = it }
            user.role?.let { preferences[USER_ROLE_KEY] = it }
            user.profileStatus?.let { preferences[USER_STATUS_KEY] = it }
        }
    }

    // Отримання токена
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    // Отримання ID користувача
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    // Отримання email користувача
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    // Отримання імені користувача
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    // Отримання даних користувача
    val userData: Flow<UserDTO?> = context.dataStore.data.map { preferences ->
        val userId = preferences[USER_ID_KEY]?.toIntOrNull()
        val email = preferences[USER_EMAIL_KEY]
        val name = preferences[USER_NAME_KEY]

        if (userId != null && email != null && name != null) {
            UserDTO(
                userId = userId,
                userName = name,
                email = email,
                profilePhoto = preferences[USER_PHOTO_KEY],
                language = preferences[USER_LANGUAGE_KEY],
                role = preferences[USER_ROLE_KEY],
                profileStatus = preferences[USER_STATUS_KEY]
            )
        } else null
    }

    // Очищення даних (logout)
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Перевірка чи токен існує
    suspend fun hasToken(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[JWT_TOKEN_KEY] != null
        }.first()
    }
}