// app/src/main/java/com/example/focuslearnmobile/data/local/IoTSettingsManager.kt
package com.example.focuslearnmobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.iotDataStore: DataStore<Preferences> by preferencesDataStore(name = "iot_settings")

@Singleton
class IoTSettingsManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val IOT_ENABLED_KEY = booleanPreferencesKey("iot_enabled")
        private val AUTO_SYNC_KEY = booleanPreferencesKey("auto_sync_enabled")
    }

    // Flow для відслідковування стану IoT
    val isIoTEnabled: Flow<Boolean> = context.iotDataStore.data.map { preferences ->
        preferences[IOT_ENABLED_KEY] ?: false
    }

    val isAutoSyncEnabled: Flow<Boolean> = context.iotDataStore.data.map { preferences ->
        preferences[AUTO_SYNC_KEY] ?: true
    }

    // Збереження налаштувань IoT
    suspend fun setIoTEnabled(enabled: Boolean) {
        context.iotDataStore.edit { preferences ->
            preferences[IOT_ENABLED_KEY] = enabled
        }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.iotDataStore.edit { preferences ->
            preferences[AUTO_SYNC_KEY] = enabled
        }
    }

    // Синхронне отримання поточного стану
    suspend fun isIoTCurrentlyEnabled(): Boolean {
        return isIoTEnabled.first()
    }

    suspend fun isAutoSyncCurrentlyEnabled(): Boolean {
        return isAutoSyncEnabled.first()
    }

    // Очистка всіх налаштувань
    suspend fun clearAllSettings() {
        context.iotDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}