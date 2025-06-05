// Оновіть app/src/main/java/com/example/focuslearnmobile/data/local/LanguageManager.kt

package com.example.focuslearnmobile.data.local

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

@Singleton
class LanguageManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")

        // Підтримувані мови
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_UKRAINIAN = "uk"

        val SUPPORTED_LANGUAGES = listOf(LANGUAGE_ENGLISH, LANGUAGE_UKRAINIAN)
    }

    // Отримання поточної збереженої мови
    val currentLanguage: Flow<String> = context.languageDataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: getSystemDefaultLanguage()
    }

    // Збереження мови з застосуванням
    suspend fun setLanguage(languageCode: String) {
        if (languageCode in SUPPORTED_LANGUAGES) {
            context.languageDataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = languageCode
            }

            // Застосовуємо мову негайно
            applyLanguageToContext(context, languageCode)
        }
    }

    // Отримання мови синхронно (для негайного використання)
    suspend fun getCurrentLanguage(): String {
        return currentLanguage.first()
    }

    // Отримання системної мови за замовчуванням
    private fun getSystemDefaultLanguage(): String {
        val systemLanguage = Locale.getDefault().language
        return if (systemLanguage in SUPPORTED_LANGUAGES) {
            systemLanguage
        } else {
            LANGUAGE_ENGLISH // За замовчуванням англійська
        }
    }

    // Застосування мови до контексту
    fun applyLanguageToContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    // Новий метод для застосування мови до поточної Activity
    fun applyLanguageToActivity(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    // Перезапуск Activity для повного застосування мови
    fun restartActivity(activity: Activity) {
        activity.recreate()
    }

    // Отримання назви мови для відображення
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_UKRAINIAN -> "Українська"
            else -> languageCode
        }
    }

    // Перевірка чи мова підтримується
    fun isLanguageSupported(languageCode: String): Boolean {
        return languageCode in SUPPORTED_LANGUAGES
    }

    // Отримання списку всіх підтримуваних мов
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return SUPPORTED_LANGUAGES.map { code ->
            code to getLanguageDisplayName(code)
        }
    }
}